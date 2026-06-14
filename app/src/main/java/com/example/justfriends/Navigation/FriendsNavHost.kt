package com.example.justfriends.Navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.compose.NavHost
import com.example.justfriends.Features.ChatFeature.ChatView
import com.example.justfriends.Features.ChatFeature.ChatViewModel
import com.example.justfriends.Features.FriendProfileFeature.FriendProfileView
import com.example.justfriends.Features.FriendProfileFeature.FriendProfileViewModel
import com.example.justfriends.Features.FriendsFeature.FriendsView
import com.example.justfriends.Features.FriendsFeature.FriendsViewModel


@Composable
fun FriendsNavHost(
    navController: NavHostController,
    padding: PaddingValues,
    friendsViewModel: FriendsViewModel,
    chatViewModel: ChatViewModel,
    friendProfileViewModel: FriendProfileViewModel
) {

    val friendsViewState by friendsViewModel.navigateTo.collectAsState()
    val chatViewState by chatViewModel.navigateTo.collectAsState()

    LaunchedEffect(friendsViewState) {
        friendsViewState?.let { destination ->
            navController.navigate(destination)
            friendsViewModel.onNavigationComplete()
        }
    }

    LaunchedEffect(chatViewState) {
        chatViewState?.let { destination ->
            navController.navigate(destination)
            chatViewModel.onNavigationComplete()
        }
    }

    NavHost(navController = navController, startDestination = NavigationItem.Friends.route) {
        composable(NavigationItem.Friends.route) { FriendsView(friendsViewModel) }
        composable(NavigationItem.FriendProfile.route) { FriendProfileView(friendProfileViewModel) }
        composable(NavigationItem.Chat.route) { ChatView(chatViewModel) }
    }
}
