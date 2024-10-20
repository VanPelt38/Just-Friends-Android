package com.example.justfriends

import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import com.example.justfriends.Features.LoginFeature.LoginViewModel
import com.example.justfriends.Features.ProfileSetUpFeature.ProfileSetUpViewModel
import com.example.justfriends.Navigation.NavHost
import com.example.justfriends.Utils.DataStoreManager
import com.example.justfriends.ui.theme.JustFriendsTheme
import androidx.compose.ui.platform.LocalContext
import com.example.justfriends.Utils.DataStoreKeys
import com.example.justfriends.Navigation.NavigationItem
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.material3.Text
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.*



class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContent {
            JustFriendsTheme {
                   JustFriends(applicationContext as Application)
            }
        }
    }
}

@Composable
fun JustFriends(justFriends: Application) {

     val dataStoreManager = DataStoreManager(LocalContext.current)
    var startScreen by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        val loggedInHome = dataStoreManager.read(DataStoreKeys.loggedInHome)
        val loggedInProfile = dataStoreManager.read(DataStoreKeys.loggedInProfile)

        startScreen = when {
            loggedInHome == "true" -> NavigationItem.Main.route
            loggedInProfile == "true" -> NavigationItem.ProfileSetUp.route
            else -> NavigationItem.Login.route
        }
    }

    if (startScreen == null) {
         LoadingScreen()
    } else {
        NavHost(
            navController = rememberNavController(),
            startDestination = startScreen!!,
            loginViewModel = LoginViewModel(justFriends, dataStoreManager = dataStoreManager),
            profileSetUpViewModel = ProfileSetUpViewModel(
                justFriends,
                dataStoreManager = dataStoreManager
            ),
            dataStoreManager = dataStoreManager
        )
    }

}

@Composable
fun LoadingScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text("Loading...")
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    JustFriendsTheme {
       JustFriends(LocalContext.current.applicationContext as Application)
    }
}