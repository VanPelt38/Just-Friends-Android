package com.example.justfriends.ReusableViews

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import com.example.justfriends.ui.theme.JustFriendsTheme
import androidx.compose.runtime.remember
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.compose.rememberNavController
import com.example.justfriends.Navigation.HomeNavHost
import com.example.justfriends.Utils.DataStoreManager
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.unit.sp

@Composable
fun MainView(dataStoreManager: DataStoreManager) {

    var selectedTab by remember { mutableStateOf(0) }
    val homeNavController = rememberNavController()
    val topBarTitle = remember { mutableStateOf("") }

    JustFriendsTheme {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = topBarTitle.value,
                            color = Color.White,
                            modifier = Modifier
                                .fillMaxWidth()
                                .wrapContentWidth(Alignment.CenterHorizontally),
                            fontSize = 20.sp
                        )

                            },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(red = 19, green = 0, blue = 142))
                )
            },
            bottomBar = {
                TabBar(selectedTab) { newIndex ->
                    selectedTab = newIndex
                }
            },
            content = { paddingValues ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    when (selectedTab) {
                        0 -> HomeNavHost(navController = homeNavController,
                            padding = paddingValues,
                            dataStoreManager = dataStoreManager,
                            navBarTitle = topBarTitle
                        )
                        // ADD MORE NAVIGATION OPTIONS HERE
                    }
                }

            }
        )
    }
}