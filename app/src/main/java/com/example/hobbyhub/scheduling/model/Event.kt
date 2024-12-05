package com.example.hobbyhub.scheduling.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Event(
    var eventId: String = "",  // Add eventId as a unique identifier
    val date: String = "",
    val time: String = "",
    val location: String = "",
    val participants: List<String> = listOf()
) : Parcelable
