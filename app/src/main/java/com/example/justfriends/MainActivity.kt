package com.example.justfriends

import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import com.example.justfriends.Features.LoginFeature.LoginViewModel
import com.example.justfriends.Features.ProfileSetUpFeature.ProfileSetUpViewModel
import com.example.justfriends.Navigation.NavHost
import com.example.justfriends.Utils.DataStoreManager
import com.example.justfriends.ui.theme.JustFriendsTheme
import androidx.compose.ui.platform.LocalContext


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

    NavHost(navController = rememberNavController(),
        loginViewModel = LoginViewModel(justFriends, dataStoreManager = dataStoreManager),
        profileSetUpViewModel = ProfileSetUpViewModel(justFriends, dataStoreManager = dataStoreManager),
        dataStoreManager = dataStoreManager
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    JustFriendsTheme {
       JustFriends(LocalContext.current.applicationContext as Application)
    }
}