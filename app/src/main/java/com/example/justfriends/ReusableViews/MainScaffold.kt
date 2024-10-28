package com.example.justfriends.ReusableViews

import android.app.Application
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.sp
import com.example.justfriends.Features.DatePlannerFeature.DatePlannerViewModel
import com.example.justfriends.Features.HomeFeature.HomeViewModel
import com.example.justfriends.Features.FriendsFeature.FriendsViewModel
import com.example.justfriends.Features.SettingsFeature.SettingsViewModel
import com.example.justfriends.Navigation.FriendsNavHost
import com.example.justfriends.Navigation.SettingsNavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.compose.runtime.remember

@Composable
fun MainView(dataStoreManager: DataStoreManager,
             homeViewModel: HomeViewModel,
             friendsViewModel: FriendsViewModel,
             datePlannerViewModel: DatePlannerViewModel,
             settingsViewModel: SettingsViewModel,
             topBarTitle: MutableState<String>
             ) {

    var selectedTab by remember { mutableStateOf(0) }
    val homeNavController = rememberNavController()
    val friendsNavController = rememberNavController()
    val settingsNavController = rememberNavController()
    val currentHomeBackStackEntry by homeNavController.currentBackStackEntryAsState()
    val canNavigateBackHome = remember(currentHomeBackStackEntry) {
        homeNavController.previousBackStackEntry != null
    }
    val currentFriendsBackStackEntry by friendsNavController.currentBackStackEntryAsState()
    val canNavigateBackFriends = remember(currentFriendsBackStackEntry) {
        friendsNavController.previousBackStackEntry != null
    }
    val currentSettingsBackStackEntry by settingsNavController.currentBackStackEntryAsState()
    val canNavigateBackSettings = remember(currentSettingsBackStackEntry) {
        settingsNavController.previousBackStackEntry != null
    }

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
                    navigationIcon = {

                        when (selectedTab) {
                            0 ->
                              if (canNavigateBackHome) {
                                  IconButton(onClick = {
                                      homeNavController.popBackStack()
                                  }) {
                                      Icon(Icons.Default.ArrowBack, contentDescription = "Back",
                                          tint = Color.White)
                                  }
                              }
                            1 ->
                                if (canNavigateBackFriends) {
                                    IconButton(onClick = {
                                        friendsNavController.popBackStack()
                                    }) {
                                        Icon(Icons.Default.ArrowBack, contentDescription = "Back",
                                            tint = Color.White)
                                    }
                                }
                            2 ->
                                if (canNavigateBackSettings) {
                                    IconButton(onClick = {
                                        settingsNavController.popBackStack()
                                    }) {
                                        Icon(Icons.Default.ArrowBack, contentDescription = "Back",
                                            tint = Color.White)
                                    }
                                }
                        }
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
                            homeViewModel,
                            datePlannerViewModel
                        )
                        1 -> FriendsNavHost(navController = friendsNavController,
                            padding = paddingValues,
                            friendsViewModel
                            )
                        2 -> SettingsNavHost(navController = settingsNavController,
                            padding = paddingValues,
                            settingsViewModel
                        )
                    }
                }
            }
        )
    }
}