package com.example.justfriends.Navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.compose.NavHost
import com.example.justfriends.Features.FriendsFeature.FriendsViewModel
import com.example.justfriends.Features.FriendsFeature.FriendsView


@Composable
fun FriendsNavHost(navController: NavHostController,
                padding: PaddingValues,
                friendsViewModel: FriendsViewModel
) {

    val friendsViewState by friendsViewModel.navigateTo.collectAsState()

    LaunchedEffect(friendsViewState) {
        friendsViewState?.let { destination ->
            navController.navigate(destination)
            friendsViewModel.onNavigationComplete()
        }
    }

    NavHost(navController = navController, startDestination = NavigationItem.Friends.route) {
        composable(NavigationItem.Friends.route) { FriendsView(friendsViewModel, padding) }
    }
}