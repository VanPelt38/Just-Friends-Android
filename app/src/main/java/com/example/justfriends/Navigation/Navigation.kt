package com.example.justfriends.Navigation

enum class View {
    main,
    login,
    profileSetUp,
    forgotPassword,
    home,
    userProfile,
    mostCompatible,
    datePlanner,
    friends,
    settings,
    availablePeople,
    friendProfile
}

sealed class NavigationItem(val route: String) {
    object Main : NavigationItem(View.main.name)
    object Login : NavigationItem(View.login.name)
    object ProfileSetUp : NavigationItem(View.profileSetUp.name)
    object ForgotPassword : NavigationItem(View.forgotPassword.name)
    object Home : NavigationItem(View.home.name)
    object UserProfile : NavigationItem(View.userProfile.name)
    object MostCompatible : NavigationItem(View.mostCompatible.name)
    object DatePlanner : NavigationItem(View.datePlanner.name)
    object Friends : NavigationItem(View.friends.name)
    object Settings: NavigationItem(View.settings.name)
    object AvailablePeople: NavigationItem(View.availablePeople.name)
    object FriendProfile: NavigationItem(View.friendProfile.name)
}