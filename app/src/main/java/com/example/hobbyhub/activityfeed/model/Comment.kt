package com.example.hobbyhub.activityfeed.model

import com.google.firebase.firestore.Blob
import com.google.firebase.firestore.DocumentId

data class Comment(
    @DocumentId
    val id: String = "",
    val postId: String = "",
    val userId: String = "",
    val username: String = "",
    val text: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val userProfile: Blob = Blob.fromBytes(ByteArray(0)),
)
