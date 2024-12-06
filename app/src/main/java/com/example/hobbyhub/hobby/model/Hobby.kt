package com.example.hobbyhub.hobby.model

import com.google.firebase.firestore.DocumentId
import java.io.Serializable

// Hobby Data
// Can insert db manually or write a script to generate it
data class Hobby(
    @DocumentId
    var id: String = "",
    var name: String = "",
    var description: String = "",
    var imageUrl: List<String> = emptyList(),
    var category: HobbyCategory = HobbyCategory.MISCELLANEOUS
): Serializable