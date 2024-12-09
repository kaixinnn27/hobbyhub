package com.example.hobbyhub.chatroom.model

data class Message(
    val senderId: String,
    val content: String,
    val timestamp: Long,
    val type: String = "text", // Default to "text" for regular messages
    val eventId: String? = null, // For event invitations
    val eventDate: String? = null,
    val eventStartTime: String? = null,
    val eventEndTime: String? = null,
    val name: String? = null
)
