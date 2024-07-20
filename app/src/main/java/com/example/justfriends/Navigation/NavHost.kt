package com.example.justfriends.Navigation

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.justfriends.Features.ForgotPasswordFeature.ForgotPasswordView
import com.example.justfriends.Features.HomeFeature.HomeView
import com.example.justfriends.Features.LoginFeature.LoginView
import com.example.justfriends.Features.LoginFeature.LoginViewModel
import com.example.justfriends.Features.ProfileSetUpFeature.ProfileSetUpView

@Composable
fun NavHost(
    navController: NavHostController,
    startDestination: String = NavigationItem.Login.route,
    loginViewModel: LoginViewModel
) {

    val loginViewState by loginViewModel.navigateTo.collectAsState()

    LaunchedEffect(loginViewState) {
        loginViewState?.let { destination ->
            navController.navigate(destination)
            loginViewModel.onNavigationComplete()
        }
    }

    NavHost(navController = navController, startDestination = startDestination) {
        composable(NavigationItem.Login.route) { LoginView(loginViewModel) }
        composable(NavigationItem.Home.route) { HomeView() }
        composable(NavigationItem.ProfileSetUp.route) { ProfileSetUpView() }
        composable(NavigationItem.ForgotPassword.route) { ForgotPasswordView() }
    }
}