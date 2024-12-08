package com.example.justfriends.DataModels

import java.util.Date

data class ExpiringMatch(

    val ownUserID: String,
    val timeStamp: Date,
    val userID: String,
)