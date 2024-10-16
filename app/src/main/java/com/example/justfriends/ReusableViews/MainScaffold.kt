package com.example.justfriends.ReusableViews

import androidx.compose.foundation.background
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import com.example.justfriends.ui.theme.JustFriendsTheme
import androidx.compose.runtime.remember
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.compose.rememberNavController
import com.example.justfriends.Navigation.HomeNavHost
import com.example.justfriends.Utils.DataStoreManager

@Composable
fun MainView(dataStoreManager: DataStoreManager) {

    var selectedTab by remember { mutableStateOf(0) }
    val homeNavController = rememberNavController()

    JustFriendsTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Hi Jake") },
                    modifier = Modifier.background(Color(red = 19, green = 0, blue = 142))
                )
            },
            bottomBar = {
                TabBar(selectedTab) { newIndex ->
                    selectedTab = newIndex
                }
            },
            content = { paddingValues ->

               when (selectedTab) {
                   0 -> HomeNavHost(navController = homeNavController,
                       padding = paddingValues,
                       dataStoreManager = dataStoreManager
                   )
                   // ADD MORE NAVIGATION OPTIONS HERE
               }
            }
        )
    }
}