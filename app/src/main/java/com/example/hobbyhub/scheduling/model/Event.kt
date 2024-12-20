package com.example.hobbyhub.scheduling.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Event(
    var id: String = "",
    var name: String = "",
    val date: String = "",
    val startTime: String = "",
    val endTime: String = "",
    val location: String = "",
    val participants: List<String> = emptyList(),
    val reminderTime: String = ""
) : Parcelable
