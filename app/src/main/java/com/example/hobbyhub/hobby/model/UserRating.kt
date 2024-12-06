package com.example.hobbyhub.hobby.model

data class UserRating(
    var userId: String = "",
    var hobbyId: String = "",
    var rating: Float = 0f,
    var review: String = ""
)