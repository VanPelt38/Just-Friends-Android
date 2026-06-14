package com.example.justfriends.Navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.justfriends.Features.AvailablePeopleFeature.AvailablePeopleView
import com.example.justfriends.Features.AvailablePeopleFeature.AvailablePeopleViewModel
import com.example.justfriends.Features.ChatFeature.ChatView
import com.example.justfriends.Features.ChatFeature.ChatViewModel
import com.example.justfriends.Features.DatePlannerFeature.DatePlannerView
import com.example.justfriends.Features.DatePlannerFeature.DatePlannerViewModel
import com.example.justfriends.Features.FriendProfileFeature.FriendProfileView
import com.example.justfriends.Features.FriendProfileFeature.FriendProfileViewModel
import com.example.justfriends.Features.FriendsFeature.FriendsView
import com.example.justfriends.Features.FriendsFeature.FriendsViewModel
import com.example.justfriends.Features.HomeFeature.HomeView
import com.example.justfriends.Features.HomeFeature.HomeViewModel
import com.example.justfriends.Features.MostCompatibleFeature.MostCompatibleView
import com.example.justfriends.Features.MostCompatibleFeature.MostCompatibleViewModel
import com.example.justfriends.Features.UserProfileFeature.EditProfileView
import com.example.justfriends.Features.UserProfileFeature.EditProfileViewModel
import com.example.justfriends.Features.UserProfileFeature.UserProfileView
import com.example.justfriends.Features.UserProfileFeature.UserProfileViewModel


@Composable
fun HomeNavHost(
    navController: NavHostController,
    padding: PaddingValues,
    homeViewModel: HomeViewModel,
    datePlannerViewModel: DatePlannerViewModel,
    availablePeopleViewModel: AvailablePeopleViewModel,
    friendsViewModel: FriendsViewModel,
    chatViewModel: ChatViewModel,
    userProfileViewModel: UserProfileViewModel,
    editProfileViewModel: EditProfileViewModel,
    friendProfileViewModel: FriendProfileViewModel,
    mostCompatibleViewModel: MostCompatibleViewModel
) {

    val homeViewState by homeViewModel.navigateTo.collectAsState()
    val datePlannerViewState by datePlannerViewModel.navigateTo.collectAsState()
    val availablePeopleViewState by availablePeopleViewModel.navigateTo.collectAsState()
    val friendsViewState by friendsViewModel.navigateTo.collectAsState()
    val userProfileViewState by userProfileViewModel.navigateTo.collectAsState()
    val mostCompatibleViewState by mostCompatibleViewModel.navigateTo.collectAsState()

    LaunchedEffect(homeViewState) {
        homeViewState?.let { destination ->
            navController.navigate(destination)
            homeViewModel.onNavigationComplete()
        }
    }

    LaunchedEffect(datePlannerViewState) {
        datePlannerViewState?.let { destination ->
            navController.navigate(destination)
            datePlannerViewModel.onNavigationComplete()
        }
    }

    LaunchedEffect(availablePeopleViewState) {
        availablePeopleViewState?.let { destination ->
            navController.navigate(destination)
            availablePeopleViewModel.onNavigationComplete()
        }
    }

    LaunchedEffect(friendsViewState) {
        friendsViewState?.let { destination ->
            navController.navigate(destination)
            friendsViewModel.onNavigationComplete()
        }
    }

    LaunchedEffect(userProfileViewState) {
        userProfileViewState?.let { destination ->
            navController.navigate(destination)
            userProfileViewModel.onNavigationComplete()
        }
    }

    LaunchedEffect(mostCompatibleViewState) {
        mostCompatibleViewState?.let { destination ->
            navController.navigate(destination)
            mostCompatibleViewModel.onNavigationComplete()
        }
    }

    // Reload user profile when navigating back to it from edit
    val currentBackStack by navController.currentBackStackEntryAsState()
    LaunchedEffect(currentBackStack) {
        if (currentBackStack?.destination?.route == NavigationItem.UserProfile.route) {
            userProfileViewModel.loadProfile()
        }
    }

    NavHost(navController = navController, startDestination = NavigationItem.Home.route) {
        composable(NavigationItem.Home.route) { HomeView(homeViewModel, padding) }
        composable(NavigationItem.UserProfile.route) { UserProfileView(userProfileViewModel) }
        composable(NavigationItem.EditProfile.route) {
            EditProfileView(editProfileViewModel) {
                navController.popBackStack()
            }
        }
        composable(NavigationItem.MostCompatible.route) { MostCompatibleView(mostCompatibleViewModel) }
        composable(NavigationItem.DatePlanner.route) { DatePlannerView(datePlannerViewModel) }
        composable(NavigationItem.AvailablePeople.route) { AvailablePeopleView(availablePeopleViewModel) }
        composable(NavigationItem.Friends.route) { FriendsView(friendsViewModel) }
        composable(NavigationItem.FriendProfile.route) { FriendProfileView(friendProfileViewModel) }
        composable(NavigationItem.Chat.route) { ChatView(chatViewModel) }
    }
}
