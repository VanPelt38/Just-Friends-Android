package com.example.justfriends.Navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.compose.NavHost
import com.example.justfriends.Features.AvailablePeopleFeature.AvailablePeopleView
import com.example.justfriends.Features.HomeFeature.HomeViewModel
import com.example.justfriends.Features.HomeFeature.HomeView
import com.example.justfriends.Features.MostCompatibleFeature.MostCompatibleView
import com.example.justfriends.Features.UserProfileFeature.UserProfileView
import com.example.justfriends.Features.DatePlannerFeature.DatePlannerView
import com.example.justfriends.Features.DatePlannerFeature.DatePlannerViewModel


@Composable
fun HomeNavHost(navController: NavHostController,
                padding: PaddingValues,
                homeViewModel: HomeViewModel,
                datePlannerViewModel: DatePlannerViewModel
) {

    val homeViewState by homeViewModel.navigateTo.collectAsState()
    val datePlannerViewState by datePlannerViewModel.navigateTo.collectAsState()

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

    NavHost(navController = navController, startDestination = NavigationItem.Home.route) {
        composable(NavigationItem.Home.route) { HomeView(homeViewModel, padding) }
        composable(NavigationItem.UserProfile.route) { UserProfileView() }
        composable(NavigationItem.MostCompatible.route) { MostCompatibleView() }
        composable(NavigationItem.DatePlanner.route) { DatePlannerView(datePlannerViewModel) }
        composable(NavigationItem.AvailablePeople.route) { AvailablePeopleView() }
    }
}