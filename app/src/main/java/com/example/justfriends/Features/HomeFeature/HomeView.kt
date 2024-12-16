package com.example.justfriends.Features.HomeFeature

import android.app.Activity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter





@Composable
fun HomeView(viewModel: HomeViewModel, padding: PaddingValues) {

    val activity = LocalContext.current as? Activity

    LaunchedEffect(Unit) {
        activity?.let {
            viewModel.setNavTitle()
            viewModel.setFalseForOnChatView()
            viewModel.loadUserData()
            viewModel.setDistancePreference()
        }
    }

                Column(
                    modifier = Modifier.padding(),
                    horizontalAlignment = Alignment.CenterHorizontally

                ) {
                    Box(
                        contentAlignment = Alignment.BottomCenter
                    ) {
                        ProfilePicture(vm = viewModel)
                        ProfileButton(vm = viewModel)
                    }
                    Spacer(modifier = Modifier.height(125.dp))
                    Button(
                        onClick = {
                           viewModel.navigateToDatePlanner()
                        },
                        modifier = Modifier
                            .width(240.dp)
                            .height(60.dp),
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Black,
                            contentColor = Color.White
                        )
                    ) {
                        Text(
                            text = "make a friend",
                            color = Color.White,
                            style = MaterialTheme.typography.titleLarge,
                            fontSize = 22.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(30.dp))
                    Button(
                        onClick = {
                            viewModel.navigateToMostCompatible()
                        },
                        modifier = Modifier
                            .width(240.dp)
                            .height(60.dp),
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(red = 1f, green = 0.8f, blue = 0.8f, alpha = 1f),
                            contentColor = Color.White
                        )
                    ) {
                        Text(
                            text = "most compatible",
                            color = Color.Black,
                            style = MaterialTheme.typography.titleLarge,
                            fontSize = 22.sp
                        )
                    }
                }
    }

@Composable
fun ProfilePicture(vm: HomeViewModel) {

    val config = LocalConfiguration.current
    val screenHeight = config.screenHeightDp.dp - 112.dp
    val imageHeight = screenHeight / 2

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(imageHeight)
            .background(Color.White)
    ) {
        if (vm.uri == null) {
            Icon(
                imageVector = Icons.Outlined.Person,
                contentDescription = "Person Icon",
                tint = Color(red = 19, green = 0, blue = 142),
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Image(
                painter = rememberAsyncImagePainter(model = vm.uri.toString()),
                contentDescription = "Profile Image",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
fun ProfileButton(vm: HomeViewModel) {

    Box(
        modifier = Modifier
            .size(46.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        IconButton(
            onClick = {
                vm.navigateToProfile()
            },
            modifier = Modifier
                .width(46.dp)
                .height(46.dp)
                .offset(y = 23.dp),
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