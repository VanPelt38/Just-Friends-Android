package com.example.justfriends.DataModels

data class MatchModel (

    val activity: String,
    val time: String,
    val ownUserID: String,
    val age: String,
    val gender: String,
    val accepted: Boolean,
    val fcmToken: String,
    val name: String,
    val distanceAway: Int,
    val chatID: String,
    val picture: String? = null,
    val profilePicRef: String? = null,
    val ID: String
)