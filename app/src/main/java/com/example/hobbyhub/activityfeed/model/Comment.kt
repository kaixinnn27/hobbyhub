package com.example.hobbyhub.activityfeed.model

import com.google.firebase.firestore.DocumentId

data class Comment(
    @DocumentId
    val id: String = "",
    val postId: String = "",
    val userId: String = "",
    val userName: String = "",
    val text: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
