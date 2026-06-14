package com.example.justfriends.Features.ChatFeature

import android.app.Activity
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.justfriends.ui.theme.JustFriendsTheme
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import coil.compose.rememberAsyncImagePainter
import androidx.compose.material3.Surface
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.material3.OutlinedTextField
import androidx.navigation.compose.rememberNavController

@Composable
fun ChatView(viewModel: ChatViewModel) {

    val activity = LocalContext.current as? Activity
    val alertStateView by viewModel.alertState
    val navController = rememberNavController()

    DisposableEffect(Unit) {
        onDispose {
                viewModel.leaveChatView()
        }
    }

    LaunchedEffect(Unit) {
        activity?.let {
            viewModel.isLoading.value = true
            viewModel.loadAllData()
            viewModel.setScaffold()
        }
    }

    JustFriendsTheme {
        Scaffold(
            bottomBar = {
                MessageInputField(vm = viewModel)
            }
        ) { paddingValues ->  
            Box(modifier = Modifier.padding(paddingValues)) {
                ChatMessages(vm = viewModel)
                alertStateView?.let { error ->
                    AlertDialog(onDismissRequest = {

                        viewModel.alertState.value = null
                        viewModel.alertStateTitle.value = null
                        viewModel.alertStateAccept.value = null
                        viewModel.alertStateDecline.value = null
                    },
                        title = {Text(viewModel.alertStateTitle.value ?: "")},
                        text = {Text(viewModel.alertState.value ?: "error")},
                        confirmButton = {
                            if (viewModel.alertStateAccept.value != null) {
                                Button(
                                    onClick = {

                                        if (viewModel.alertStateTitle.value == "Are you sure you'd like to report this user?") {
                                           viewModel.reportOptionsSheetPresented.value = false
                                            viewModel.createUserReport()
                                            viewModel.alertState.value = "Thanks for letting us know - our team will investigate your concern thoroughly and take any appropriate actions."
                                            viewModel.alertStateTitle.value = "Report Sent Successfully"
                                            viewModel.alertStateAccept.value = "Okay"
                                            viewModel.alertStateDecline.value = null
                                        } else if (viewModel.alertStateTitle.value == "Are you sure you'd like to block this user?") {
                                           viewModel.inappropriateBehaviourSheetPresented.value = false
                                            viewModel.blockUser()
                                            viewModel.alertState.value = "This user will no longer be able to see or interact with you."
                                            viewModel.alertStateTitle.value = "Success"
                                            viewModel.alertStateAccept.value = "Okay"
                                            viewModel.alertStateDecline.value = null
                                        } else if (viewModel.alertStateTitle.value == "Report Sent Successfully" || viewModel.alertStateTitle.value == "Success") {
                                            if (viewModel.alertStateTitle.value == "Success") {
                                                navController.popBackStack()
                                            }
                                            viewModel.alertState.value = null
                                            viewModel.alertStateTitle.value = null
                                            viewModel.alertStateAccept.value = null
                                            viewModel.alertStateDecline.value = null
                                        }
                                    }
                                ) {
                                    Text(viewModel.alertStateAccept.value ?: "")
                                }
                            }
                        },
                        dismissButton = {
                            if (viewModel.alertStateDecline.value != null ) {
                                Button(
                                    onClick = {

                                        if (viewModel.alertStateTitle.value == "Are you sure you'd like to report this user?") {
                                            viewModel.reportOptionsSheetPresented.value = false
                                        } else if (viewModel.alertStateTitle.value == "Are you sure you'd like to block this user?") {
                                            viewModel.inappropriateBehaviourSheetPresented.value = false
                                        }

                                        viewModel.alertState.value = null
                                        viewModel.alertStateTitle.value = null
                                        viewModel.alertStateAccept.value = null
                                        viewModel.alertStateDecline.value = null
                                    }
                                ) {
                                    Text(viewModel.alertStateDecline.value ?: "")
                                }
                            }
                        }
                    )
                }
            }
            if (viewModel.inappropriateBehaviourSheetPresented.value) {
                ModalBottomSheet(
                    onDismissRequest = { viewModel.inappropriateBehaviourSheetPresented.value = false }
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text(
                            text = "Is this user behaving inappropriately?",
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text("Report User", modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                viewModel.inappropriateBehaviourSheetPresented.value = false
                                viewModel.reportOptionsSheetPresented.value = true
                            }
                            .padding(12.dp)
                        )
                        Text("Block User", modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                viewModel.alertState.value =
                                    "They will no longer be able to contact you, and you will be hidden from each other's 'Available' feeds."
                                viewModel.alertStateTitle.value =
                                    "Are you sure you'd like to block this user?"
                                viewModel.alertStateAccept.value = "Yes"
                                viewModel.alertStateDecline.value = "No"
                            }
                            .padding(12.dp)
                        )
                    }
                }
            }
            if (viewModel.reportOptionsSheetPresented.value) {
                ModalBottomSheet(
                    onDismissRequest = { viewModel.reportOptionsSheetPresented.value = false }
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text(
                            text = "Don't worry - your report is anonymous.",
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text("Inappropriate Content", modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                viewModel.reportUserSelectedOption.value = "Inappropriate Content"
                                viewModel.alertState.value = ""
                                viewModel.alertStateTitle.value =
                                    "Are you sure you'd like to report this user?"
                                viewModel.alertStateAccept.value = "Yes"
                                viewModel.alertStateDecline.value = "No"
                            }
                            .padding(12.dp)
                        )
                        Text("Harassment", modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                viewModel.reportUserSelectedOption.value = "Harassment"
                                viewModel.alertState.value = ""
                                viewModel.alertStateTitle.value =
                                    "Are you sure you'd like to report this user?"
                                viewModel.alertStateAccept.value = "Yes"
                                viewModel.alertStateDecline.value = "No"
                            }
                            .padding(12.dp)
                        )
                        Text("Criminal Behaviour", modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                viewModel.reportUserSelectedOption.value = "Criminal Behaviour"
                                viewModel.alertState.value = ""
                                viewModel.alertStateTitle.value =
                                    "Are you sure you'd like to report this user?"
                                viewModel.alertStateAccept.value = "Yes"
                                viewModel.alertStateDecline.value = "No"
                            }
                            .padding(12.dp)
                        )
                        Text("User is Underage", modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                viewModel.reportUserSelectedOption.value = "User is Underage"
                                viewModel.alertState.value = ""
                                viewModel.alertStateTitle.value =
                                    "Are you sure you'd like to report this user?"
                                viewModel.alertStateAccept.value = "Yes"
                                viewModel.alertStateDecline.value = "No"
                            }
                            .padding(12.dp)
                        )
                        Text("User is Fake/Spam/Scammer", modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                viewModel.reportUserSelectedOption.value =
                                    "User is Fake/Spam/Scammer"
                                viewModel.alertState.value = ""
                                viewModel.alertStateTitle.value =
                                    "Are you sure you'd like to report this user?"
                                viewModel.alertStateAccept.value = "Yes"
                                viewModel.alertStateDecline.value = "No"
                            }
                            .padding(12.dp)
                        )
                        Text("Cancel", modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                viewModel.reportOptionsSheetPresented.value = false
                            }
                            .padding(12.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ChatMessages(vm: ChatViewModel) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 64.dp),
        reverseLayout = true
    ) {
        items(vm.allMessages.count()) { message ->
            println("count: ${vm.allMessages.count()}")
            ChatBubble(vm, message)
        }
    }
}

@Composable
fun ChatBubble(vm: ChatViewModel, message: Int) {
    val backgroundColour = if (vm.isUserMessage(message)) Color(red = 135, green = 206, blue = 235) else Color(red = 218, green = 247, blue = 166)
    val alignment = if (vm.isUserMessage(message)) Arrangement.End else Arrangement.Start

    Row(
        horizontalArrangement = alignment,
        verticalAlignment = Alignment.Bottom,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        if (!vm.isUserMessage(message)) {
            ProfilePicture(vm, message)
            Spacer(modifier = Modifier.width(8.dp))
        }
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = backgroundColour,
            modifier = Modifier.padding(4.dp)
        ) {
            Text (
                text = vm.allMessages[message].message,
                modifier = Modifier.padding(8.dp)
            )
        }
        if (vm.isUserMessage(message)) {
            Spacer(modifier = Modifier.width(8.dp))
            ProfilePicture(vm, message)
        }
    }
}

@Composable
fun ProfilePicture(vm: ChatViewModel, message: Int) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(Color.White)
    ) {
        if (vm.friend?.picture == null) {
            AvatarPic()
        } else {
            val uri = if (vm.isUserMessage(message)) vm.myProfile?.picture.let { Uri.parse(it) } else vm.friend?.picture.let { Uri.parse(it) }
            Image(
                painter = rememberAsyncImagePainter(model = uri.toString()),
                contentDescription = "Profile Image",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
fun AvatarPic() {
    Icon(
        imageVector = Icons.Outlined.Person,
        contentDescription = "Person Icon",
        tint = Color(red = 19, green = 0, blue = 142),
        modifier = Modifier
            .fillMaxSize()
            .clip(CircleShape)
    )
}

@Composable
fun MessageInputField(vm: ChatViewModel) {
    var messageText by remember { mutableStateOf("") }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = messageText,
            onValueChange = { messageText = it },
        modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(16.dp),
            colors = TextFieldDefaults.textFieldColors(
                containerColor = Color.White
            )
        )
        IconButton(onClick = {
        if (messageText.isNotEmpty()) {
            vm.sendMessage(messageText)
            messageText = ""
        }
        }) {
            Icon(Icons.Default.Send, contentDescription = "Send")
        }
    }
}