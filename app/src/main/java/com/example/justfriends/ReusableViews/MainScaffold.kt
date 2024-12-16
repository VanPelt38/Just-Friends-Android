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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import android.content.Intent
import android.graphics.drawable.Icon
import android.net.Uri
import android.os.Build
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FloatingActionButton
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
import com.google.firebase.auth.FirebaseAuth
import androidx.compose.runtime.remember
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.justfriends.Features.AvailablePeopleFeature.AvailablePeopleViewModel
import com.example.justfriends.R
import com.example.justfriends.Utils.DataStoreKeys
import com.google.firebase.Firebase

@Composable
fun MainView(dataStoreManager: DataStoreManager,
             homeViewModel: HomeViewModel,
             friendsViewModel: FriendsViewModel,
             datePlannerViewModel: DatePlannerViewModel,
             settingsViewModel: SettingsViewModel,
             availablePeopleViewModel: AvailablePeopleViewModel,
             topBarTitle: MutableState<String>,
             topBarIconAction: MutableState<() -> Unit>,
             notificationCount: MutableState<Int>
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
    val context = LocalContext.current
    var auth: FirebaseAuth = FirebaseAuth.getInstance()
    var userEmail: String = "Not found"
    var errorAlertState by remember { mutableStateOf<String?>(null) }
    var errorAlertStateTitle by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(key1 = Unit) {
       userEmail = getUserEmail(dataStoreManager)
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
                    actions = {
                        IconButton(
                            onClick = topBarIconAction.value
                        ) {
                            when (topBarTitle.value) {
                                "Available" ->
                                    Box() {
                                        Icon(
                                            painter = painterResource(id = R.drawable.group_24px),
                                            contentDescription = "friend",
                                            tint = Color.White
                                        )
                                        if (notificationCount.value > 0) {

                                            Box(
                                                contentAlignment = Alignment.Center,
                                                modifier = Modifier
                                                    .size(16.dp)
                                                    .background(Color.Red, CircleShape)
                                                    .align(Alignment.BottomStart)
                                            ) {
                                                Text(
                                                    text = if (notificationCount.value > 10)
                                                        "10+" else notificationCount.value.toString(),
                                                    color = Color.White,
                                                    fontSize = 10.sp,
                                                    modifier = Modifier
                                                        .offset(y = 2.dp)
                                                )
                                            }
                                        }
                                    }

                            }
                        }
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
            floatingActionButton = {
                FloatingActionButton(onClick = {

                    val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                        data = Uri.parse("mailto:")
                        putExtra(Intent.EXTRA_EMAIL, arrayOf("justfriendshelpdesk@gmail.com"))
                        putExtra(Intent.EXTRA_SUBJECT, "Just Friends Support Issue")
                        putExtra(Intent.EXTRA_TEXT, "User ID: ${auth.currentUser?.uid}, " +
                                "Email: ${userEmail}, " +
                                "Version: ${Build.VERSION.RELEASE}")
                    }

                    if (emailIntent.resolveActivity(context.packageManager) != null) {
                        context.startActivity(emailIntent)
                    } else {
                        errorAlertStateTitle = "Uh-oh"
                        errorAlertState = "Looks like your device doesn't have email configured." +
                                "Please set it up and try again." +
                                "You can also email us directly at justfriendshelpdesk@gmail.com ."
                    }

                },
                    shape = CircleShape,
                   containerColor = Color(red = 19, green = 0, blue = 142)
                    ) {
                    Icon(Icons.Filled.Email, contentDescription = "Add item")
                }
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
                            datePlannerViewModel,
                            availablePeopleViewModel,
                            friendsViewModel
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

        errorAlertState?.let { error ->
            AlertDialog(onDismissRequest = {
                errorAlertState = null
                errorAlertStateTitle = null
            },
                title = {Text(errorAlertStateTitle ?: "")},
                text = {Text(errorAlertState ?: "error")},
                confirmButton = {
                    Button(
                        onClick = {
                            errorAlertState = null
                            errorAlertStateTitle = null
                        }
                    ) {
                        Text("Okay")
                    }
                })
        }
    }
}

suspend fun getUserEmail(dataStore: DataStoreManager): String {
    return dataStore.read(DataStoreKeys.email)
}