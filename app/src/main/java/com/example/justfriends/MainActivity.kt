package com.example.justfriends

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.example.justfriends.Features.LoginFeature.LoginViewModel
import com.example.justfriends.Navigation.NavHost
import com.example.justfriends.ui.theme.JustFriendsTheme


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            JustFriendsTheme {
                   JustFriends()
            }
        }
    }
}

@Composable
fun JustFriends() {
    NavHost(navController = rememberNavController(),
        loginViewModel = viewModel<LoginViewModel>()
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    JustFriendsTheme {
       JustFriends()
    }
}