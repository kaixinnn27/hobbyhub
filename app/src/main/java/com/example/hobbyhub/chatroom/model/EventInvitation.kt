package com.example.hobbyhub.chatroom.model

import com.google.firebase.firestore.Blob
import com.google.firebase.firestore.DocumentId

data class EventInvitation(
    @DocumentId
    val eventId: String = "",
    val eventName: String = "",
    val eventDate: String = "",
    val eventStartTime: String = "",
    val eventEndTime: String = "",
    val senderId: String = "",
    val timestamp: Long = 0,
    val type: String = "",
)