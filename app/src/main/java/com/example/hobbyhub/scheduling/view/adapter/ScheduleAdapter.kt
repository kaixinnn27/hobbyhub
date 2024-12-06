package com.example.hobbyhub.scheduling.view.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.hobbyhub.R
import com.example.hobbyhub.scheduling.model.Event

class ScheduleAdapter(
    private val onItemClick: (Event) -> Unit // Lambda to handle item clicks
) : ListAdapter<Event, ScheduleAdapter.EventViewHolder>(DiffCallback) {

    inner class EventViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val tvEventId: TextView = view.findViewById(R.id.tvEventId) // New TextView for eventId
        private val tvDate: TextView = view.findViewById(R.id.tvDate)
        private val tvTime: TextView = view.findViewById(R.id.tvTime)
        private val tvLocation: TextView = view.findViewById(R.id.tvLocation)
        private val tvParticipants: TextView = view.findViewById(R.id.tvParticipants)

        fun bind(event: Event) {
            // Bind the event details, including eventId
            tvEventId.text = "Event ID: ${event.eventId}" // Display the eventId
            tvDate.text = event.date
            tvTime.text = event.startTime
            tvLocation.text = event.location
            tvParticipants.text = event.participants.joinToString(", ")

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
            return oldItem.eventId == newItem.eventId
        }

        override fun areContentsTheSame(oldItem: Event, newItem: Event): Boolean {
            return oldItem == newItem
        }
    }
}
