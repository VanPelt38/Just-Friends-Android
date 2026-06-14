package com.example.justfriends.Features.MostCompatibleFeature

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.ui.platform.LocalLifecycleOwner
import coil.compose.rememberAsyncImagePainter
import com.example.justfriends.DataModels.Compatible
import com.example.justfriends.R
import com.example.justfriends.ReusableViews.brandPurple
import com.example.justfriends.ui.theme.JustFriendsTheme

@Composable
fun MostCompatibleView(viewModel: MostCompatibleViewModel) {

    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.setScaffold()
                viewModel.loadUserInterests()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val errorAlert by viewModel.errorAlertState

    JustFriendsTheme {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { viewModel.calculate() },
                    modifier = Modifier
                        .width(200.dp)
                        .height(44.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFFCCCC),
                        contentColor = Color.Black
                    )
                ) {
                    Text("Calculate", fontSize = 16.sp)
                }
                Spacer(modifier = Modifier.height(16.dp))

                if (!viewModel.isLoading.value) {
                    if (viewModel.compatibleUsers.isNotEmpty()) {
                        LazyColumn {
                            items(viewModel.compatibleUsers.size) { index ->
                                CompatiblePersonCell(vm = viewModel, index = index)
                            }
                        }
                    } else {
                        Spacer(modifier = Modifier.height(200.dp))
                        Text(
                            "Press calculate to find your most compatible people nearby.",
                            modifier = Modifier.padding(16.dp),
                            textAlign = TextAlign.Center,
                            color = Color.Black,
                            style = MaterialTheme.typography.titleLarge,
                            fontSize = 20.sp
                        )
                    }
                }
            }

            if (viewModel.isLoading.value) {
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

    errorAlert?.let {
        AlertDialog(
            onDismissRequest = { viewModel.errorAlertState.value = null; viewModel.errorAlertStateTitle.value = null },
            title = { Text(viewModel.errorAlertStateTitle.value ?: "") },
            text = { Text(it) },
            confirmButton = {
                Button(onClick = {
                    viewModel.errorAlertState.value = null
                    viewModel.errorAlertStateTitle.value = null
                    viewModel.errorAlertStateAccept.value = null
                }) {
                    Text(viewModel.errorAlertStateAccept.value ?: "OK")
                }
            }
        )
    }
}

@Composable
fun CompatiblePersonCell(vm: MostCompatibleViewModel, index: Int) {
    val person = vm.compatibleUsers[index]
    Box {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(5.dp),
            border = BorderStroke(0.2.dp, Color.Black),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                CompatibleProfilePicture(person = person)
                Spacer(modifier = Modifier.width(20.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .height(70.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        person.name,
                        color = Color.Black,
                        style = MaterialTheme.typography.titleLarge,
                        fontSize = 15.sp
                    )
                    Text(
                        if (person.distanceAway >= 1) "${person.distanceAway} km away" else "< 1 km away",
                        modifier = Modifier.offset(y = 20.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        fontSize = 12.sp
                    )
                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(bottom = 5.dp),
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Text(
                            person.age,
                            color = Color.Black,
                            style = MaterialTheme.typography.bodyMedium,
                            fontSize = 12.sp,
                            modifier = Modifier.offset(y = 2.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Icon(
                            painter = painterResource(id = if (person.gender == "male") R.drawable.male_24px else R.drawable.female_24px),
                            contentDescription = person.gender,
                            tint = Color.Black,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }
        CompatibleProfileButton(
            alignment = Modifier.align(Alignment.BottomStart),
            index = index,
            vm = vm
        )
    }
}

@Composable
fun CompatibleProfilePicture(person: Compatible) {
    Box(
        modifier = Modifier
            .width(70.dp)
            .height(70.dp)
            .background(Color.White)
    ) {
        if (person.picture == null) {
            Icon(
                imageVector = Icons.Outlined.Person,
                contentDescription = "Person Icon",
                tint = brandPurple,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .align(Alignment.BottomStart)
            )
        } else {
            Image(
                painter = rememberAsyncImagePainter(model = Uri.parse(person.picture)),
                contentDescription = "Profile Image",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
fun CompatibleProfileButton(alignment: Modifier, index: Int, vm: MostCompatibleViewModel) {
    Box(modifier = alignment) {
        IconButton(
            onClick = { vm.seeFriendProfileForIndex(index) },
            modifier = Modifier
                .width(15.dp)
                .height(15.dp)
                .offset(x = 65.dp)
                .zIndex(1f),
            colors = IconButtonDefaults.iconButtonColors(
                containerColor = Color.Black,
                contentColor = Color.White
            )
        ) {
            Icon(
                imageVector = Icons.Outlined.AccountCircle,
                contentDescription = "View profile",
                tint = Color.White
            )
        }
    }
}
