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
import com.example.justfriends.Features.AvailablePeopleFeature.AvailablePeopleViewModel
import com.example.justfriends.Features.ChatFeature.ChatViewModel
import com.example.justfriends.Features.DatePlannerFeature.DatePlannerViewModel
import com.example.justfriends.Features.ForgotPasswordFeature.ForgotPasswordView
import com.example.justfriends.Features.ForgotPasswordFeature.ForgotPasswordViewModel
import com.example.justfriends.Features.FriendProfileFeature.FriendProfileViewModel
import com.example.justfriends.Features.FriendsFeature.FriendsViewModel
import com.example.justfriends.Features.HomeFeature.HomeViewModel
import com.example.justfriends.Features.LoginFeature.LoginView
import com.example.justfriends.Features.LoginFeature.LoginViewModel
import com.example.justfriends.Features.MostCompatibleFeature.MostCompatibleViewModel
import com.example.justfriends.Features.ProfileSetUpFeature.ProfileSetUpView
import com.example.justfriends.Features.ProfileSetUpFeature.ProfileSetUpViewModel
import com.example.justfriends.Features.SettingsFeature.SettingsViewModel
import com.example.justfriends.Features.UserProfileFeature.EditProfileViewModel
import com.example.justfriends.Features.UserProfileFeature.UserProfileViewModel
import com.example.justfriends.ReusableViews.MainView
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
    val currentView = remember { mutableStateOf("") }
    val topBarIconAction = remember { mutableStateOf({}) }
    val notificationCount = remember { mutableStateOf(0) }
    val shouldShowFAB = remember { mutableStateOf(true) }

    val app = LocalContext.current.applicationContext as Application

    val homeViewModel = HomeViewModel(app, dataStoreManager, currentView, topBarTitle, shouldShowFAB)
    val friendsViewModel = FriendsViewModel(app, dataStoreManager, currentView, topBarTitle, shouldShowFAB)
    val chatViewModel = ChatViewModel(app, dataStoreManager, topBarTitle, currentView, topBarIconAction, shouldShowFAB)
    val settingsViewModel = SettingsViewModel(app, dataStoreManager, currentView, topBarTitle, shouldShowFAB)
    val datePlannerViewModel = DatePlannerViewModel(app, dataStoreManager, currentView, topBarTitle, shouldShowFAB)
    val availablePeopleViewModel = AvailablePeopleViewModel(app, dataStoreManager, topBarTitle, currentView, topBarIconAction, notificationCount, shouldShowFAB)
    val userProfileViewModel = UserProfileViewModel(app, dataStoreManager, currentView, topBarTitle, shouldShowFAB)
    val editProfileViewModel = EditProfileViewModel(app, dataStoreManager, currentView, topBarTitle, shouldShowFAB)
    val friendProfileViewModel = FriendProfileViewModel(app, dataStoreManager, currentView, topBarTitle, shouldShowFAB)
    val mostCompatibleViewModel = MostCompatibleViewModel(app, dataStoreManager, currentView, topBarTitle, shouldShowFAB)
    val forgotPasswordViewModel = ForgotPasswordViewModel(app, dataStoreManager, currentView, topBarTitle, shouldShowFAB)

    val settingsRootNavState by settingsViewModel.navigateToRoot.collectAsState()

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

    // Handles logout and delete-account navigation from SettingsViewModel back to Login
    LaunchedEffect(settingsRootNavState) {
        settingsRootNavState?.let { destination ->
            navController.navigate(destination) {
                popUpTo(0) { inclusive = true }
            }
            settingsViewModel.onRootNavigationComplete()
        }
    }

    NavHost(navController = navController, startDestination = startDestination) {
        composable(NavigationItem.Login.route) { LoginView(loginViewModel) }
        composable(NavigationItem.Main.route) {
            MainView(
                dataStoreManager,
                homeViewModel,
                friendsViewModel,
                datePlannerViewModel,
                settingsViewModel,
                availablePeopleViewModel,
                chatViewModel,
                topBarTitle,
                currentView,
                topBarIconAction,
                notificationCount,
                shouldShowFAB,
                userProfileViewModel,
                editProfileViewModel,
                friendProfileViewModel,
                mostCompatibleViewModel
            )
        }
        composable(NavigationItem.ProfileSetUp.route) { ProfileSetUpView(profileSetUpViewModel) }
        composable(NavigationItem.ForgotPassword.route) { ForgotPasswordView(forgotPasswordViewModel) }
    }
}
