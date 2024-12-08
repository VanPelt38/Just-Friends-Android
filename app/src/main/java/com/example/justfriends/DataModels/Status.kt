package com.example.justfriends.DataModels

import java.util.Date

data class Status(

    val activity: String,
    val fcmToken: String,
    val latitude: Double,
    val longitude: Double,
    val time: String,
    val timeStamp: Date,
    val suitorID: String = "none",
    val suitorName: String = "none",
    val firebaseDocID: String = "none",
    var distanceAway: Int = 0,
    val daterID: String
)