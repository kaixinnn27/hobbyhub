package com.example.hobbyhub.scheduling.view.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.hobbyhub.R
import com.example.hobbyhub.scheduling.model.Event

class EventAdapter(private val events: List<Event>,
                   private val onItemClick: (Event) -> Unit)
    : RecyclerView.Adapter<EventAdapter.EventViewHolder>() {

    inner class EventViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvDate: TextView = itemView.findViewById(R.id.tvDate)
        private val tvTime: TextView = itemView.findViewById(R.id.tvTime)
        private val tvLocation: TextView = itemView.findViewById(R.id.tvLocation)
        private val tvParticipants: TextView = itemView.findViewById(R.id.tvParticipants)

        fun bind(event: Event) {
            tvDate.text = event.date
            tvTime.text = event.time
            tvLocation.text = event.location
            tvParticipants.text = event.participants.joinToString(", ")
            Log.d("EventAdapter", "Binding Event: $event")
            itemView.setOnClickListener {
                Log.d("EventAdapter", "Item Clicked: $event")
                onItemClick(event) // Trigger the lambda function
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_event, parent, false)
        return EventViewHolder(view)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        val event = events[position]
        holder.bind(event)
    }

    override fun getItemCount(): Int = events.size
}
