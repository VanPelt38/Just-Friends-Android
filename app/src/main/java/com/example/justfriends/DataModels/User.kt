package com.example.justfriends.DataModels

data class User(

    val age: String,
    val gender: String,
    val name: String,
    val interests: Array<String>? = null,
    val occupation: String? = null,
    val picture: String? = null,
    val profilePicRef: String? = null,
    val summary: String? = null,
    val town: String? = null,
    val userID: String
)