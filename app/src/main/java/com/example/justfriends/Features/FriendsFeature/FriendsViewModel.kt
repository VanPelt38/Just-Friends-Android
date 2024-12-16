package com.example.justfriends.Features.FriendsFeature

import android.app.Application
import android.app.Notification
import android.net.Uri
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.justfriends.DataModels.MatchModel
import com.example.justfriends.DataModels.User
import com.example.justfriends.Navigation.View
import com.example.justfriends.Utils.DataStoreKeys
import com.example.justfriends.Utils.DataStoreManager
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.functions.HttpsCallableResult
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID


class FriendsViewModel(justFriends: Application,
                    private val dataStoreManager: DataStoreManager,
                    private val navBarTitle: MutableState<String>
): AndroidViewModel(justFriends) {

    private lateinit var auth: FirebaseAuth
    private val _navigateTo = MutableStateFlow<String?>(null)
    val navigateTo: StateFlow<String?> = _navigateTo.asStateFlow()
    var isLoading = mutableStateOf(false)
    var friends = arrayListOf<MatchModel>()
    private val db = FirebaseFirestore.getInstance()
    var myDetails: MatchModel? = null
    private val _errorAlertState = mutableStateOf<String?>(null)
    private val _errorAlertStateAccept = mutableStateOf<String?>(null)
    private val _errorAlertStateDecline = mutableStateOf<String?>(null)
    val errorAlertState = _errorAlertState
    val errorAlertStateAccept = _errorAlertStateAccept
    val errorAlertStateDecline = _errorAlertStateDecline
    var selectedFriendIndex: Int? = null
    var acceptFriend: Boolean? = null

    init {
        auth = FirebaseAuth.getInstance()
    }

    fun loadAll() {

        viewModelScope.launch {

            val loadMatchesJob = async { loadMatches() }
            val deleteNotificationsJob = async { deleteNotifications() }
            val loadMyProfileJob = async { loadMyProfile() }
            val loadMyStatusJob = async { loadMyStatus() }
            loadMatchesJob.await()
            deleteNotificationsJob.await()
            loadMyProfileJob.await()
            loadMyStatusJob.await()
        }
    }

    fun acceptFriendRequest() {

        val chatID = UUID.randomUUID().toString()
        val userID = auth.currentUser?.uid

        val myFunctions = FirebaseFunctions.getInstance()

        val notificationPayload = mapOf(
            "suitor" to friends[selectedFriendIndex ?: 0].ID,
            "suitee" to userID,
            "suiteeName" to myDetails?.name
        )
        myFunctions.getHttpsCallable("confirmMatch")
            .call(notificationPayload)
            .addOnCompleteListener { task: Task<HttpsCallableResult> ->
                if (!task.isSuccessful) {
                    task.exception?.let {  exception ->
                        println("Error confirming new match: ${exception.localizedMessage}")
                    }
                } else {
                    task.result?.data?.let { result ->
                        println("Success confirming new match: ${result}")
                    }
                }
            }

        viewModelScope.launch {

            db.collection("users")
                .document(userID ?: "")
                .collection("matchStatuses")
                .document(friends[selectedFriendIndex ?: 0].ID)
                .update("accepted", true,
                    "chatID", chatID)
                .await()

            db.collection("chats")
                .document(chatID)
                .collection("userDetails")
                .document(chatID)
                .set(
                    mapOf(
                        "userNames" to listOf(friends[selectedFriendIndex ?: 0].name, myDetails?.name),
                        "userIDs" to listOf(friends[selectedFriendIndex ?: 0].ID, userID)
                    )
                )
                .await()

            db.collection("users")
                .document(friends[selectedFriendIndex ?: 0].ID)
                .collection("matchStatuses")
                .document(userID ?: "")
                .set(
                    mapOf(
                        "name" to myDetails?.name,
                        "imageURL" to myDetails?.picture,
                        "activity" to myDetails?.activity,
                        "time" to myDetails?.time,
                        "ID" to myDetails?.ID,
                        "age" to myDetails?.age,
                        "gender" to myDetails?.gender,
                        "accepted" to true,
                        "fcmToken" to myDetails?.fcmToken,
                        "chatID" to chatID,
                        "realmID" to "android",
                        "ownUserID" to friends[selectedFriendIndex ?: 0].ID,
                        "distanceAway" to friends[selectedFriendIndex ?: 0].distanceAway
                    )
                )
                .await()

            val dateMatchNotifications = db.collection("users")
                .document(friends[selectedFriendIndex ?: 0].ID)
                .collection("matchNotifications")
                .get()
                .await()

            var suitorIDs = arrayListOf<String>()

            if (!dateMatchNotifications.isEmpty) {
                for (doc in dateMatchNotifications.documents) {
                    val data = doc.data
                    if (data != null) {
                        val suitorID = data["suitorID"] as? String
                        suitorIDs.add(suitorID ?: "error")
                    }
                }
            }

            if (!suitorIDs.contains(userID)) {

                val newData = mapOf(
                    "suitorID" to userID
                )

                db.collection("users")
                    .document(friends[selectedFriendIndex ?: 0].ID)
                    .collection("matchNotifications")
                    .add(newData)
                    .await()
            }
            selectedFriendIndex = null
            goToChat()
        }
    }

    fun rejectFriendRequest() {

        val userID = auth.currentUser?.uid

        selectedFriendIndex?.let {

            viewModelScope.launch {
                val matchStatus = db.collection("users")
                    .document(userID ?: "")
                    .collection("matchStatuses")
                    .whereEqualTo("ID", friends[selectedFriendIndex ?: 0].ID)
                    .get()
                    .await()

                if (!matchStatus.isEmpty) {
                    for (doc in matchStatus.documents) {
                        db.collection("users")
                            .document(userID ?: "")
                            .collection("matchStatuses")
                            .document(doc.id)
                            .delete()
                            .await()
                    }
                }
                friends.removeAt(selectedFriendIndex ?: 0)
                selectedFriendIndex = null
            }

        }

    }


   fun deleteFriend() {

        val userID = auth.currentUser?.uid

       viewModelScope.launch {

           // delete user copy
           db.collection("users")
               .document(userID ?: "")
               .collection("matchStatuses")
               .document(friends[selectedFriendIndex ?: 0].ID)
               .delete()
               .await()

           // delete match copy
           db.collection("users")
               .document(friends[selectedFriendIndex ?: 0].ID)
               .collection("matchStatuses")
               .document(userID ?: "")
               .delete()
               .await()

           // delete chats
           db.collection("chats")
               .document(friends[selectedFriendIndex ?: 0].chatID)
               .delete()
               .await()

           friends.removeAt(selectedFriendIndex ?: 0)
           selectedFriendIndex = null
       }
    }

    suspend fun loadMyProfile() {

        val userID = auth.currentUser?.uid
        val profile = db.collection("users")
            .document(userID ?: "")
            .collection("profile")
            .whereEqualTo("userID", userID)
            .limit(1)
            .get()
            .await()

        if (!profile.isEmpty) {
            for (doc in profile.documents) {
                val data = doc.data
                if (data != null) {
                    val myProfile = MatchModel(
                        age = (data["age"] as? Number ?: 0).toString(),
                        gender = data["gender"] as? String ?: "error",
                        name = data["name"] as? String ?: "error",
                        picture = data["picture"] as? String ?: "error",
                        ID = data["userID"] as? String ?: "error",
                        chatID = "",
                        accepted = false,
                        activity = "",
                        distanceAway = 0,
                        fcmToken = "",
                        ownUserID = "",
                        time = ""
                    )
                    myDetails = myProfile
                }
            }
        }
    }

    suspend fun loadMyStatus() {

        val userID = auth.currentUser?.uid

        val userStatus = db.collection("statuses")
            .whereEqualTo("userID", userID)
            .limit(1)
            .get()
            .await()

        if (!userStatus.isEmpty) {
            for (doc in userStatus.documents) {
                val data = doc.data
                if (data != null) {
                    myDetails?.activity = data["activity"] as? String ?: ""
                    myDetails?.time = data["time"] as? String ?: ""
                    myDetails?.fcmToken = data["fcmToken"] as? String ?: ""
                }
            }
        }
    }

    suspend fun deleteNotifications() {
        val userID = auth.currentUser?.uid
        val notifications = db.collection("users")
            .document(userID ?: "")
            .collection("matchNotifications")
            .get()
            .await()

        for (notification in notifications) {
            db.collection("users")
                .document(userID ?: "")
                .collection("matchNotifications")
                .document(notification.id)
                .delete()
                .await()
        }
    }

    suspend fun loadMatches() {
        val userID = auth.currentUser?.uid
        friends.clear()

        val matches = db.collection("users")
            .document(userID ?: "")
            .collection("matchStatuses")
            .get()
            .await()

        if (!matches.isEmpty) {
            for (doc in matches.documents) {
                val data = doc.data
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
                    friends.add(match)
                }
            }
        }
        isLoading.value = false
    }

    fun setNavTitle() {
        navBarTitle.value = "Friends"
    }

    fun seeFriendProfile() {
        onNavigate(View.friendProfile.name)
    }

    fun goToChat() {
            onNavigate(View.chatView.name)
    }

    fun onNavigate(destination: String) {
        _navigateTo.value = destination
    }

    fun onNavigationComplete() {
        _navigateTo.value = null
    }
}