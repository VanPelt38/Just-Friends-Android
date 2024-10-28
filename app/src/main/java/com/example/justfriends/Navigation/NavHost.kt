package com.example.justfriends.Navigation

import android.app.Application
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.justfriends.Features.DatePlannerFeature.DatePlannerViewModel
import com.example.justfriends.Features.ForgotPasswordFeature.ForgotPasswordView
import com.example.justfriends.Features.FriendsFeature.FriendsViewModel
import com.example.justfriends.Features.HomeFeature.HomeViewModel
import com.example.justfriends.ReusableViews.MainView
import com.example.justfriends.Features.LoginFeature.LoginView
import com.example.justfriends.Features.LoginFeature.LoginViewModel
import com.example.justfriends.Features.ProfileSetUpFeature.ProfileSetUpView
import com.example.justfriends.Features.ProfileSetUpFeature.ProfileSetUpViewModel
import com.example.justfriends.Features.SettingsFeature.SettingsViewModel
import com.example.justfriends.Utils.DataStoreManager

@Composable
fun NavHost(
    navController: NavHostController,
    startDestination: String,
    loginViewModel: LoginViewModel,
    profileSetUpViewModel: ProfileSetUpViewModel,
    dataStoreManager: DataStoreManager
) {

    val loginViewState by loginViewModel.navigateTo.collectAsState()
    val profileSetUpViewState by profileSetUpViewModel.navigateTo.collectAsState()

    val topBarTitle = remember { mutableStateOf("") }

    val homeViewModel = HomeViewModel(
        LocalContext.current.applicationContext as Application,
        dataStoreManager = dataStoreManager,
        navBarTitle = topBarTitle
    )
    val friendsViewModel = FriendsViewModel(
        LocalContext.current.applicationContext as Application,
        dataStoreManager = dataStoreManager,
        navBarTitle = topBarTitle
    )
    val settingsViewModel = SettingsViewModel(
        LocalContext.current.applicationContext as Application,
        dataStoreManager = dataStoreManager,
        navBarTitle = topBarTitle
    )
    val datePlannerViewModel = DatePlannerViewModel(
        LocalContext.current.applicationContext as Application,
        dataStoreManager = dataStoreManager,
        navBarTitle = topBarTitle
    )

    LaunchedEffect(loginViewState) {
        loginViewState?.let { destination ->
            navController.navigate(destination)
            loginViewModel.onNavigationComplete()
        }
    }
        LaunchedEffect(profileSetUpViewState) {
            profileSetUpViewState?.let { destination ->
                navController.navigate(destination)
                profileSetUpViewModel.onNavigationComplete()
            }
    }

    NavHost(navController = navController, startDestination = startDestination) {
        composable(NavigationItem.Login.route) { LoginView(loginViewModel) }
        composable(NavigationItem.Main.route) { MainView(
            dataStoreManager,
            homeViewModel,
            friendsViewModel,
            datePlannerViewModel,
            settingsViewModel,
            topBarTitle
            ) }
        composable(NavigationItem.ProfileSetUp.route) { ProfileSetUpView(profileSetUpViewModel) }
        composable(NavigationItem.ForgotPassword.route) { ForgotPasswordView() }
    }
}