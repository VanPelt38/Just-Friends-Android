package com.example.justfriends.Features.ChatFeature

import android.app.Application
import java.util.Date
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.justfriends.Utils.DataStoreKeys
import com.example.justfriends.Utils.DataStoreManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import androidx.compose.runtime.mutableStateListOf
import com.example.justfriends.DataModels.ChatMessage
import com.example.justfriends.DataModels.MatchModel
import com.example.justfriends.DataModels.User
import com.google.android.gms.tasks.Task
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.functions.HttpsCallableResult
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await


class ChatViewModel(private val justFriends: Application,
                               private val dataStoreManager: DataStoreManager,
                               private val navBarTitle: MutableState<String>,
                    private val currentView: MutableState<String>,
                               private val navBarAction: MutableState<() -> Unit>,
                    private val shouldShowFAB: MutableState<Boolean>
): AndroidViewModel(justFriends) {

    private val _navigateTo = MutableStateFlow<String?>(null)
    val navigateTo: StateFlow<String?> = _navigateTo.asStateFlow()

    var friendUserID: String? = null
    var friend: MatchModel? = null
    var myProfile: User? = null
    var allMessages = mutableStateListOf<ChatMessage>()
    private lateinit var auth: FirebaseAuth
    private val db = FirebaseFirestore.getInstance()
    var isLoading = mutableStateOf(false)
    var inappropriateBehaviourSheetPresented = mutableStateOf(false)
    var reportOptionsSheetPresented = mutableStateOf(false)
    private val _alertState = mutableStateOf<String?>(null)
    private val _alertStateTitle = mutableStateOf<String?>(null)
    private val _alertStateAccept = mutableStateOf<String?>(null)
    private val _alertStateDecline = mutableStateOf<String?>(null)
    val alertState = _alertState
    val alertStateTitle = _alertStateTitle
    val alertStateAccept = _alertStateAccept
    val alertStateDecline = _alertStateDecline
    var reportUserSelectedOption = mutableStateOf("")

    init {
        auth = FirebaseAuth.getInstance()
    }

    fun blockUser() {

        val userID = auth.currentUser?.uid

        val blockedUserDoc = mapOf(
            "blockedUserID" to friend?.ID,
            "blockType" to "blocked"
        )
        val blockedUserDoc2 = mapOf(
            "blockedUserID" to userID,
            "blockType" to "blocker"
        )
        viewModelScope.launch {
            db.collection("users")
                .document(userID ?: "")
                .collection("blockedUsers")
                .add(blockedUserDoc)
                .await()

            db.collection("users")
                .document(friend?.ID ?: "")
                .collection("blockedUsers")
                .add(blockedUserDoc2)
                .await()

            db.collection("users")
                .document(userID ?: "")
                .collection("matchStatuses")
                .document(friend?.ID ?: "")
                .delete()
                .await()

            db.collection("users")
                .document(friend?.ID ?: "")
                .collection("matchStatuses")
                .document(userID ?: "")
                .delete()
                .await()

            db.collection("chats")
                .document(friend?.chatID ?: "")
                .delete()
                .await()
        }
    }

    fun createUserReport() {
        val userID = auth.currentUser?.uid

        val userReportData = mapOf(
            "userID" to userID,
            "userName" to myProfile?.name,
            "abuserID" to friend?.ID,
            "abuserName" to friend?.name,
            "reportType" to reportUserSelectedOption.value
        )
        viewModelScope.launch {
            db.collection("userReports")
                .add(userReportData)
                .await()
        }

    }

    fun sendMessage(message: String) {
        viewModelScope.launch {
            saveChatToFirestore(message)
            sendNewMessageNotification(message)
        }
    }

    fun sendNewMessageNotification(message: String) {

            val myFunctions = FirebaseFunctions.getInstance()

            val notificationPayload = mapOf(
                "receiverID" to friend?.fcmToken,
                "senderName" to myProfile?.name,
                "chatText" to message
            )
            myFunctions.getHttpsCallable("sendChatMessageNotification")
                .call(notificationPayload)
                .addOnCompleteListener { task: Task<HttpsCallableResult> ->
                    if (!task.isSuccessful) {
                        task.exception?.let {  exception ->
                            println("Error notifying new match: ${exception.localizedMessage}")
                        }
                    } else {
                        task.result?.data?.let { result ->
                            println("Success notifying new match: ${result}")
                        }
                    }
                }

    }

    suspend fun saveChatToFirestore(message: String) {

        val userID = auth.currentUser?.uid
        val currentTime = Date()

        val newMessage = ChatMessage(
            message = message,
            userID = userID ?: "",
            chatID = friend?.chatID ?: "",
            timeStamp = currentTime
        )
        allMessages.add(0, newMessage)

        val newMessageData = mapOf(
            "message" to message,
            "ID" to userID,
            "chatID" to friend?.chatID,
            "timeStamp" to currentTime,
        )

        db.collection("chats")
            .document(friend?.chatID ?: "")
            .collection("messages")
            .add(newMessageData)
            .await()
    }

    fun isUserMessage(messageIndex: Int): Boolean {
        val currentMessage = allMessages[messageIndex]
        return currentMessage.userID != friendUserID
    }

    fun setScaffold() {
        navBarTitle.value = friend?.name ?: "Chat"
        currentView.value = "Chat"
        navBarAction.value = { showReportIssueMenu() }
        shouldShowFAB.value = false
    }

    fun showReportIssueMenu() {
        inappropriateBehaviourSheetPresented.value = true
    }

    fun leaveChatView() {
        viewModelScope.launch {
            setOnChatViewForIncomingMessageNotifications(false)
        }
    }

    suspend fun setOnChatViewForIncomingMessageNotifications(onChatView: Boolean) {
        dataStoreManager.write(DataStoreKeys.onChatView, "true")
    }

    suspend fun listenForNewMessages() {

        db.collection("chats")
            .document(friend?.chatID ?: "")
            .collection("messages")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    println("Error fetching messages snapshot: $error")
                    return@addSnapshotListener
                }
                snapshot?.let {
                    for (change in it.documentChanges)  {
                        if (change.type == DocumentChange.Type.ADDED) {

                            val message = change.document.data

                            if (message != null) {
                                val timeStamp = message["timeStamp"] as? Timestamp
                                val timeDate = timeStamp?.toDate()

                                val chatMessage = ChatMessage(
                                    message = message["message"] as? String ?: "error",
                                    userID = message["ID"] as? String ?: "error",
                                    chatID = "",
                                    timeStamp = timeDate ?: Date()
                                )
                                if (timeDate ?: Date() > Date()) {
                                    viewModelScope.launch {
                                        loadAllMessages()
                                    }
                                }
                            }



                        }
                    }
                }
            }
    }

    fun loadAllData() {
        viewModelScope.launch {
            setOnChatViewForIncomingMessageNotifications(true)
            val getUserIDJob = async { getUserID() }
            getUserIDJob.await()
            val loadMatchDetailsJob = async { loadMatchDetails() }
            loadMatchDetailsJob.await()
            val loadUserProfileJob = async { loadUserProfile() }
            loadUserProfileJob.await()
            val loadAllMessagesJob = async { loadAllMessages() }
            loadAllMessagesJob.await()
            listenForNewMessages()
            isLoading.value = false
        }
    }

    suspend fun getUserID() {
        friendUserID = dataStoreManager.read(DataStoreKeys.friendIDForChat)
    }

    suspend fun loadMatchDetails() {
        val userID = auth.currentUser?.uid
        val friend = db.collection("users")
            .document(userID ?: "")
            .collection("matchStatuses")
            .document(friendUserID ?: "")
            .get()
            .await()

        if (friend.exists()) {
                val data = friend.data
                if (data != null) {
                    val match = MatchModel(
                        activity = data["activity"] as? String ?: "error",
                        time = data["time"] as? String ?: "error",
                        ownUserID = data["ownUserID"] as? String ?: "error",
                        age = (data["age"] as? Number ?: 0).toString(),
                        gender = data["gender"] as? String ?: "error",
                        accepted = data["accepted"] as? Boolean ?: false,
                        fcmToken = data["fcmToken"] as? String ?: "error",
                        name = data["name"] as? String ?: "error",
                        distanceAway = data["distanceAway"] as? Int ?: 0,
                        chatID = data["chatID"] as? String ?: "error",
                        picture = data["imageURL"] as? String ?: "error",
                        ID = data["ID"] as? String ?: "error"
                    )
                    this.friend = match
                }

        }
    }

    suspend fun loadUserProfile() {

        val userID = auth.currentUser?.uid

        viewModelScope.launch {
            val userProfileQuery = db.collection("users")
                .document(userID ?: "")
                .collection("profile")
                .whereEqualTo("userID", userID)
                .limit(1)

            val userProfile = userProfileQuery.get().await()

            if (!userProfile.isEmpty()) {
                for (doc in userProfile.documents) {
                    val data = doc.data
                    if (data != null) {
                        val user = User(age = "", gender = "", name = "", userID = "", )
                        user.picture = data["picture"] as? String ?: ""
                        myProfile = user
                    }
                }
            }
        }
    }

    fun onNavigate(destination: String) {
        _navigateTo.value = destination
    }

    fun onNavigationComplete() {
        _navigateTo.value = null
    }

    suspend fun loadAllMessages() {

        allMessages.clear()
        val chatMessages = db.collection("chats")
            .document(friend?.chatID ?: "")
            .collection("messages")
            .get()
            .await()

        if (!chatMessages.isEmpty) {
            for (message in chatMessages) {
                val data = message.data
                if (data != null) {
                    val timeStamp = data["timeStamp"] as? Timestamp
                    val timeDate = timeStamp?.toDate()

                    val chatMessage = ChatMessage(
                        message = data["message"] as? String ?: "error",
                        userID = data["ID"] as? String ?: "error",
                        chatID = "",
                        timeStamp = timeDate ?: Date()
                    )
                    allMessages.add(chatMessage)
                }
            }
           allMessages.sortWith(compareByDescending { it.timeStamp })
        }
    }
}