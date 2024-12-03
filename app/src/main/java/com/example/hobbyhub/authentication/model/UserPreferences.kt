package com.example.hobbyhub.authentication.model

import com.google.firebase.firestore.DocumentId

data class UserPreferences(
    @DocumentId
    var id: String = "",
    var preferredCategories: List<String> = emptyList()
)