package com.example.hobbyhub.chatroom.model

data class Message(
    val senderId: String,
    val content: String,
    val timestamp: Long
)