package com.example.justfriends.Navigation

enum class View {
    main,
    login,
    profileSetUp,
    forgotPassword,
    home,
    userProfile,
    mostCompatible
}

sealed class NavigationItem(val route: String) {
    object Main : NavigationItem(View.main.name)
    object Login : NavigationItem(View.login.name)
    object ProfileSetUp : NavigationItem(View.profileSetUp.name)
    object ForgotPassword : NavigationItem(View.forgotPassword.name)
    object Home : NavigationItem(View.home.name)
    object UserProfile : NavigationItem(View.userProfile.name)
    object MostCompatible : NavigationItem(View.mostCompatible.name)
}