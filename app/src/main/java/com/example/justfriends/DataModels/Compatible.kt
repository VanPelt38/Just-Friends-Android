package com.example.justfriends.DataModels

data class Compatible(
    val name: String,
    val age: String,
    val gender: String,
    val picture: String?,
    val userID: String,
    val town: String? = null,
    val occupation: String? = null,
    val summary: String? = null,
    val interests: List<String> = emptyList(),
    var distanceAway: Int = 0
)
