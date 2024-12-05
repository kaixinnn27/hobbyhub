package com.example.hobbyhub.scheduling.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.hobbyhub.scheduling.model.Event

class ScheduleViewModel : ViewModel() {

    private val _events = MutableLiveData<List<Event>>(emptyList())
    val events: LiveData<List<Event>> get() = _events

    // Add an event
    fun addEvent(event: Event) {
        val currentList = _events.value.orEmpty().toMutableList()
        currentList.add(event)
        _events.value = currentList
    }

    // Remove an event
    fun removeEvent(event: Event) {
        val currentList = _events.value.orEmpty().toMutableList()
        currentList.remove(event)
        _events.value = currentList
    }

    // Set all events
    fun setEvents(events: List<Event>) {
        _events.value = events
    }
}
