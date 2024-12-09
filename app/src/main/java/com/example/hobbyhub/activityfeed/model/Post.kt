package com.example.hobbyhub.activityfeed.model

import com.google.firebase.firestore.Blob
import com.google.firebase.firestore.DocumentId

data class Post(
    @DocumentId
    val id: String = "",
    val userId: String = "",
    val username: String = "",
    val photo: Blob = Blob.fromBytes(ByteArray(0)),
    val description: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    var likeCount: Int = 0,
    var commentCount: Int = 0,
    val likedBy: MutableList<String> = mutableListOf(),
)
