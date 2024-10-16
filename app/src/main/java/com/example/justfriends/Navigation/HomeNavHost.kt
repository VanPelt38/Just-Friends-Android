package com.example.justfriends.Navigation

import android.app.Application
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.compose.NavHost
import com.example.justfriends.Features.HomeFeature.HomeViewModel
import com.example.justfriends.Features.HomeFeature.HomeView
import com.example.justfriends.Features.MostCompatibleFeature.MostCompatibleView
import com.example.justfriends.Features.UserProfileFeature.UserProfileView
import com.example.justfriends.Utils.DataStoreManager


@Composable
fun HomeNavHost(navController: NavHostController,
                padding: PaddingValues,
                dataStoreManager: DataStoreManager
) {
    val homeViewModel = HomeViewModel(LocalContext.current.applicationContext as Application,
        dataStoreManager = dataStoreManager
    )
    val homeViewState by homeViewModel.navigateTo.collectAsState()

    LaunchedEffect(homeViewState) {
        homeViewState?.let { destination ->
            navController.navigate(destination)
            homeViewModel.onNavigationComplete()
        }
    }

    NavHost(navController = navController, startDestination = NavigationItem.Home.route) {
        composable(NavigationItem.Home.route) { HomeView(homeViewModel, padding) }
        composable(NavigationItem.UserProfile.route) { UserProfileView() }
        composable(NavigationItem.MostCompatible.route) { MostCompatibleView() }
    }
}