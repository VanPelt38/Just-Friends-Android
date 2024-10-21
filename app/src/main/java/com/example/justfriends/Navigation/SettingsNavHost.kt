package com.example.justfriends.Navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.compose.NavHost
import com.example.justfriends.Features.SettingsFeature.SettingsViewModel
import com.example.justfriends.Features.SettingsFeature.SettingsView


@Composable
fun SettingsNavHost(navController: NavHostController,
                   padding: PaddingValues,
                   settingsViewModel: SettingsViewModel
) {

    val settingsViewState by settingsViewModel.navigateTo.collectAsState()

    LaunchedEffect(settingsViewState) {
        settingsViewState?.let { destination ->
            navController.navigate(destination)
            settingsViewModel.onNavigationComplete()
        }
    }

    NavHost(navController = navController, startDestination = NavigationItem.Settings.route) {
        composable(NavigationItem.Settings.route) { SettingsView(settingsViewModel, padding) }
    }
}