package com.example.hobbyhub.scheduling.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Event(
    val date: String = "",
    var eventId: String = "",
    val time: String = "",
    val location: String = "",
    val participants: List<String> = emptyList(),
    val reminderTime: String = ""
) : Parcelable
