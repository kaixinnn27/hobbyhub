package com.example.hobbyhub.chatroom.view.model

import com.google.firebase.firestore.DocumentId

data class Group(
    @DocumentId
    val id: String,
    val name: String,
    val members: List<String>,
    val adminId: String,
    val createdAt: Long
)