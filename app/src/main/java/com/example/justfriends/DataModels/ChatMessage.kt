package com.example.justfriends.DataModels

import java.util.Date

data class ChatMessage (
    val message: String,
    val userID: String,
    val chatID: String,
    val timeStamp: Date
    )