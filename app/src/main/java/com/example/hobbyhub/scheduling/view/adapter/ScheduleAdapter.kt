package com.example.hobbyhub.scheduling.view.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.hobbyhub.R
import com.example.hobbyhub.scheduling.model.Event
import java.text.SimpleDateFormat
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

class ScheduleAdapter(
    private val onItemClick: (Event) -> Unit // Lambda to handle item clicks
) : ListAdapter<Event, ScheduleAdapter.EventViewHolder>(DiffCallback) {

    inner class EventViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val tvEventId: TextView =
            view.findViewById(R.id.tvEventName) // New TextView for eventId
        private val tvDate: TextView = view.findViewById(R.id.tvDate)
        private val tvTime: TextView = view.findViewById(R.id.tvTime)
        private val tvLocation: TextView = view.findViewById(R.id.tvLocation)
        private val tvParticipants: TextView = view.findViewById(R.id.tvParticipants)

        @SuppressLint("SetTextI18n")
        fun bind(event: Event) {
            // Bind the event details, including eventId
            tvEventId.text = event.name // Display the eventId
            tvDate.text = event.date
            tvTime.text = formatTimeRange(event.startTime, event.endTime)
            tvLocation.text = event.location

            if (event.participants.isEmpty()) {
                tvParticipants.visibility = View.GONE
            } else if (event.participants.size == 1) {
                tvParticipants.text = event.participants.joinToString("")
            } else {
                tvParticipants.text = event.participants.joinToString(", ")
            }

            // Set click listener for the item
            itemView.setOnClickListener {
                onItemClick(event) // Trigger the click lambda
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_event, parent, false)
        return EventViewHolder(view)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object DiffCallback : DiffUtil.ItemCallback<Event>() {
        override fun areItemsTheSame(oldItem: Event, newItem: Event): Boolean {
            // Use eventId to uniquely identify each item
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Event, newItem: Event): Boolean {
            return oldItem == newItem
        }
    }

    fun formatTimeRange(startTime: String, endTime: String): String {
        // Define the formatter for parsing and formatting
        val formatter = DateTimeFormatter.ofPattern("HH:mm")

        // Parse the input times
        val start = LocalTime.parse(startTime, formatter)
        val end = LocalTime.parse(endTime, formatter)

        // Format and return the result
        return "${start.format(formatter)} - ${end.format(formatter)}"
    }
}
