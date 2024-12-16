package com.example.justfriends.Features.AvailablePeopleFeature

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Snackbar
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.zIndex
import coil.compose.rememberAsyncImagePainter
import com.example.justfriends.R
import kotlinx.coroutines.delay

@Composable
fun AvailablePeopleView(viewModel: AvailablePeopleViewModel) {

    val errorAlertStateView by viewModel.errorAlertState
    val activity = LocalContext.current as? Activity
    val snackBarView by viewModel.snackBarMessage

    LaunchedEffect(true) {
        activity?.let {
            viewModel.isLoading.value = true
            viewModel.snackBarMessage.value = "Your plan will be available for 12 hours"
            viewModel.setNavTitleAndAction()
                viewModel.loadAll()
        }
    }

    JustFriendsTheme {
        Box() {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                if (!viewModel.isLoading.value) {
                    DatePlanLabel(vm = viewModel)
                    if (((viewModel.people?.count() ?: 0) > 0) && ((viewModel.people?.count()
                            ?: 0) == (viewModel.profiles?.count() ?: 0))
                    ) {
                        LazyColumn {
                            items(viewModel.people?.count() ?: 0) { person ->
                                AvailablePersonCell(vm = viewModel, person = person)
                            }
                        }
                    } else {
                        Spacer(modifier = Modifier.height(250.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Sorry - it doesn't seem like anyone's available around you. Expand your distance settings or try again later!"
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
                            viewModel.errorAlertStateTitle.value = null
                            viewModel.errorAlertStateAccept.value = null
                            viewModel.errorAlertStateDecline.value = null
                        },
                            title = {Text(viewModel.errorAlertStateTitle.value ?: "")},
                            text = {Text(viewModel.errorAlertState.value ?: "error")},
                            confirmButton = {
                                if (viewModel.errorAlertStateAccept.value != null) {
                                    Button(
                                        onClick = {

                                            if (viewModel.errorAlertState.value == "Share with friends!") {
                                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                                    type = "text/plain"
                                                    putExtra(
                                                        Intent.EXTRA_TEXT,
                                                        "Join me on Just Friends! https://apps.apple.com/us/app/just-friends/id6462937691"
                                                    )
                                                }
                                                activity?.startActivity(
                                                    Intent.createChooser(
                                                        shareIntent,
                                                        "Share with friends!"
                                                    )
                                                )
                                            } else if (viewModel.errorAlertState.value == "Are you sure you want to connect with this person?") {
                                                viewModel.startMatching()
                                            }

                                            viewModel.errorAlertState.value = null
                                            viewModel.errorAlertStateTitle.value = null
                                            viewModel.errorAlertStateAccept.value = null
                                            viewModel.errorAlertStateDecline.value = null

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
                                            viewModel.errorAlertStateTitle.value = null
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
                    snackBarView?.let {
                        Box {
                            Spacer(modifier = Modifier.height(200.dp))
                            Snackbar(
                                modifier = Modifier
                                    .padding(16.dp)
                                    .graphicsLayer {
                                        alpha = 0.7f
                                    }
                                )
                            {
                                Text(viewModel.snackBarMessage.value ?: "",
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                    )
                            }
                        }
                        LaunchedEffect(Unit) {
                            delay(3000)
                            viewModel.snackBarMessage.value = null
                        }
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
fun DatePlanLabel(vm: AvailablePeopleViewModel) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .background(Color(red = 1f, green = 0.8f, blue = 0.8f, alpha = 1f))
            .fillMaxWidth()
            .height(30.dp)
    ) {
        Text(
            "I want to ${vm.activityPlan.value} ${vm.activityTime.value}",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Black,
            fontSize = 16.sp
        )
    }
}

@Composable
fun AvailablePersonCell(vm: AvailablePeopleViewModel, person: Int) {
    Box {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(5.dp)
                .clickable {
                    vm.selectedFriendIndex = person
                    vm.errorAlertState.value = "Are you sure you want to connect with this person?"
                    vm.errorAlertStateTitle.value = "Great Stuff"
                    vm.errorAlertStateAccept.value = "Yes"
                    vm.errorAlertStateDecline.value = "No"
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
                    ProfilePicture(vm = vm, person = person)
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
                        "${vm.profiles[person].name} wants to ${vm.people[person].activity}",
                        color = Color.Black,
                        style = MaterialTheme.typography.titleLarge,
                        fontSize = 15.sp
                    )
                    Text("${vm.people[person].distanceAway} km away",
                        modifier = Modifier.offset(y = 20.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        fontSize = 12.sp,
                    )
                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(bottom = 5.dp),
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Text(
                            vm.profiles[person].age,
                            color = Color.Black,
                            style = MaterialTheme.typography.bodyMedium,
                            fontSize = 12.sp,
                            modifier = Modifier.offset(y = 2.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Icon(
                            painter = painterResource(id = if (vm.profiles[person].gender == "male") R.drawable.male_24px else R.drawable.female_24px),
                            contentDescription = if (vm.profiles[person].gender == "male") "male" else "female",
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

@Composable
fun ProfilePicture(vm: AvailablePeopleViewModel, person: Int) {
    Box(
        modifier = Modifier
            .width(70.dp)
            .height(70.dp)
            .background(Color.White)
    ) {
        if (vm.profiles[person].picture == null) {
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
            val uri = vm.profiles[person].picture.let { Uri.parse(it) }
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
fun ProfileButton(alignment: Modifier, vm: AvailablePeopleViewModel) {

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