package com.example.justfriends.Features.FriendsFeature

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.unit.sp
import com.example.justfriends.ui.theme.JustFriendsTheme
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.zIndex
import androidx.wear.compose.foundation.SwipeToDismissValue
import androidx.wear.compose.material.swipeable
import coil.compose.rememberAsyncImagePainter
import com.example.justfriends.Features.FriendsFeature.FriendsViewModel
import com.example.justfriends.R
import kotlinx.coroutines.delay

@Composable
fun FriendsView(viewModel: FriendsViewModel) {

    val errorAlertStateView by viewModel.errorAlertState
    val activity = LocalContext.current as? Activity

    LaunchedEffect(true) {
        activity?.let {
            viewModel.isLoading.value = true
            viewModel.setNavTitle()
            viewModel.loadAll()
        }
    }

    JustFriendsTheme {
        Box() {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                if (!viewModel.isLoading.value) {
                    if ((viewModel.friends?.count() ?: 0) > 0) {
                        LazyColumn {
                            items(viewModel.friends?.count() ?: 0) { friend ->
                                FriendCell(vm = viewModel, friend = friend) {
                                    viewModel.deleteFriend()
                                }
                            }
                        }
                    } else {
                        Spacer(modifier = Modifier.height(250.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Sorry - you don't have any connections just yet. Try saying 'hi' to a few people!"
                                , modifier = Modifier.padding(16.dp),
                                textAlign = TextAlign.Center,
                                color = Color.Black,
                                style = MaterialTheme.typography.titleLarge,
                                fontSize = 25.sp
                            )
                        }
                    }
                    errorAlertStateView?.let { error ->
                        AlertDialog(onDismissRequest = {

                            viewModel.errorAlertState.value = null
                            viewModel.errorAlertStateAccept.value = null
                            viewModel.errorAlertStateDecline.value = null
                            viewModel.acceptFriend = null
                        },
                            text = {Text(viewModel.errorAlertState.value ?: "error")},
                            confirmButton = {
                                if (viewModel.errorAlertStateAccept.value != null) {
                                    Button(
                                        onClick = {
                                            viewModel.selectedFriendIndex?.let {
                                                if (!viewModel.friends[viewModel.selectedFriendIndex ?: 0].accepted) {
                                                    if (viewModel.acceptFriend ?: true)  {
                                                        viewModel.acceptFriendRequest()
                                                    } else if (!(viewModel.acceptFriend ?: true)) {
                                                        viewModel.rejectFriendRequest()
                                                    }
                                                } else {
                                                    viewModel.deleteFriend()
                                                }
                                            }
                                            viewModel.errorAlertState.value = null
                                            viewModel.errorAlertStateAccept.value = null
                                            viewModel.errorAlertStateDecline.value = null
                                            viewModel.acceptFriend = null
                                        }
                                    ) {
                                        Text(viewModel.errorAlertStateAccept.value ?: "")
                                    }
                                }
                            },
                            dismissButton = {
                                if (viewModel.errorAlertStateDecline.value != null ) {
                                    Button(
                                        onClick = {
                                            viewModel.errorAlertState.value = null
                                            viewModel.errorAlertStateAccept.value = null
                                            viewModel.errorAlertStateDecline.value = null
                                        }
                                    ) {
                                        Text(viewModel.errorAlertStateDecline.value ?: "")
                                    }
                                }
                            }
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.3f))
                            .clickable { }
                    ) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FriendCell(vm: FriendsViewModel, friend: Int, onDelete: () -> Unit) {

    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = {
            if (it == SwipeToDismissBoxValue.EndToStart) {
                vm.selectedFriendIndex = friend
                onDelete()
                true
            } else {
                false
            }
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            Box(
                modifier = Modifier
                    .size(width = 400.dp, height = 60.dp)
                    .background(Color.Red)
                    .offset(5.dp, 5.dp)
            ) {
                Text(
                    text = "Delete",
                    color = Color.White,
                    modifier = Modifier.align(Alignment.CenterEnd)
                )
            }
        }
    ) {
    Box {
        Card(
            modifier = Modifier
                .size(width = 420.dp, height = 70.dp)
                .padding(5.dp)
                .clickable {
                    if (vm.friends[friend].accepted) {
                        vm.goToChat()
                    }
                },
            border = BorderStroke(0.2.dp, Color.Black),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            )
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box {
                    ProfilePicture(vm = vm, friend = friend)
                }
                Spacer(modifier = Modifier.width(20.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .height(70.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "${vm.friends[friend].name} wants to ${vm.friends[friend].activity}",
                        color = Color.Black,
                        style = MaterialTheme.typography.titleLarge,
                        fontSize = 15.sp
                    )
                    Text(
                        if (vm.friends[friend].distanceAway >= 1) "${vm.friends[friend].distanceAway} km away" else "1 km away",
                        modifier = Modifier.offset(y = 20.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        fontSize = 12.sp,
                    )
                    if (!vm.friends[friend].accepted) {
                        Row(
                            modifier = Modifier.offset(y = 40.dp)
                        ) {
                            Button(
                                onClick = {
                                    vm.selectedFriendIndex = friend
                                    vm.acceptFriend = true
                                    vm.errorAlertState.value = "Are you sure?"
                                    vm.errorAlertStateAccept.value = "Yes"
                                    vm.errorAlertStateDecline.value = "No"
                                },
                                modifier = Modifier
                                    .width(50.dp)
                                    .height(10.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.Black,
                                    contentColor = Color.White
                                )
                            ) {
                                Text(
                                    text = "accept",
                                    color = Color.White,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontSize = 5.sp
                                )
                            }
                            Spacer(modifier = Modifier.width(5.dp))
                            Button(
                                onClick = {
                                    // reject friend request
                                },
                                modifier = Modifier
                                    .width(50.dp)
                                    .height(10.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.Black,
                                    contentColor = Color.White
                                )
                            ) {
                                Text(
                                    text = "no thanks",
                                    color = Color.White,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontSize = 5.sp
                                )
                            }
                        }
                    }
                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(bottom = 5.dp),
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Text(
                            vm.friends[friend].age,
                            color = Color.Black,
                            style = MaterialTheme.typography.bodyMedium,
                            fontSize = 12.sp,
                            modifier = Modifier.offset(y = 2.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Icon(
                            painter = painterResource(id = if (vm.friends[friend].gender == "male") R.drawable.male_24px else R.drawable.female_24px),
                            contentDescription = if (vm.friends[friend].gender == "male") "male" else "female",
                            tint = Color.Black,
                            modifier = Modifier.size(14.dp, 14.dp)
                        )
                    }
                }
            }
        }
        ProfileButton(alignment = Modifier.align(Alignment.BottomStart), vm = vm)
    }
}
}

@Composable
fun ProfilePicture(vm: FriendsViewModel, friend: Int) {
    Box(
        modifier = Modifier
            .width(70.dp)
            .height(70.dp)
            .background(Color.White)
    ) {
        if (vm.friends[friend].picture == null) {
            Icon(
                imageVector = Icons.Outlined.Person,
                contentDescription = "Person Icon",
                tint = Color(red = 19, green = 0, blue = 142),
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .align(Alignment.BottomStart)
            )
        } else {
            val uri = vm.friends[friend].picture.let { Uri.parse(it) }
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
fun ProfileButton(alignment: Modifier, vm: FriendsViewModel) {

    Box(
        modifier = alignment
    ) {
        IconButton(
            onClick = {
                vm.seeFriendProfile()
            },
            modifier = Modifier
                .width(15.dp)
                .height(15.dp)
                .offset(x = 65.dp)
                .zIndex(1f)
            ,
            colors = IconButtonDefaults.iconButtonColors(
                containerColor = Color.Black,
                contentColor = Color.White
            )
        ) {
            Icon(
                imageVector = Icons.Outlined.AccountCircle,
                contentDescription = "account person",
                tint = Color.White
            )
        }
    }
}