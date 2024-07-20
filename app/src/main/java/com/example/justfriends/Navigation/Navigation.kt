package com.example.justfriends.Navigation

enum class View {
    home,
    login,
    profileSetUp,
    forgotPassword
}

sealed class NavigationItem(val route: String) {
    object Home : NavigationItem(View.home.name)
    object Login : NavigationItem(View.login.name)
    object ProfileSetUp : NavigationItem(View.profileSetUp.name)
    object ForgotPassword : NavigationItem(View.forgotPassword.name)
}