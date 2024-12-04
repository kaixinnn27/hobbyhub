package com.example.hobbyhub.hobby.model

import com.google.firebase.firestore.DocumentId

data class UserHobby(
    @DocumentId
    var id: String = "",
    var preferredCategories: List<HobbyCategory> = emptyList(),
    var savedHobbies: List<String> = emptyList(),
    var completedHobbies: List<String> = emptyList(),
    var hobbyRecommendations: List<String> = emptyList()
)
