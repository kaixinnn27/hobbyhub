package com.example.hobbyhub.scheduling.model

import android.os.Parcelable
import com.google.firebase.firestore.DocumentId
import kotlinx.parcelize.Parcelize

@Parcelize
data class Event(
    @DocumentId
    var id: String = "",
    var name: String = "",
    val date: String = "",
    val startTime: String = "",
    val endTime: String = "",
    val location: String = "",
    val participants: List<String> = emptyList(),
    val reminderTime: String = ""
) : Parcelable
