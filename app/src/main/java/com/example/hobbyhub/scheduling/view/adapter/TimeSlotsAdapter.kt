package com.example.hobbyhub.scheduling.view.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.hobbyhub.R

class TimeSlotAdapter(
    private val timeSlots: List<String>,
    private val busyTimes: List<String>,
    private val onSlotSelected: (String) -> Unit
) : RecyclerView.Adapter<TimeSlotAdapter.TimeSlotViewHolder>() {

    inner class TimeSlotViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textView: TextView = itemView.findViewById(R.id.tvTimeSlot)

        fun bind(time: String, isBusy: Boolean) {
            textView.text = time
            textView.setBackgroundColor(
                if (isBusy) ContextCompat.getColor(itemView.context, R.color.dark_gray)
                else ContextCompat.getColor(itemView.context, R.color.light_green)
            )
            textView.setTextColor(
                if (isBusy) ContextCompat.getColor(itemView.context, android.R.color.white)
                else ContextCompat.getColor(itemView.context, android.R.color.black)
            )
            itemView.setOnClickListener {
                if (!isBusy) onSlotSelected(time)
                else Toast.makeText(itemView.context, "This time is busy.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TimeSlotViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_time_slot, parent, false)
        return TimeSlotViewHolder(view)
    }

    override fun onBindViewHolder(holder: TimeSlotViewHolder, position: Int) {
        val time = timeSlots[position]
        val isBusy = time in busyTimes
        holder.bind(time, isBusy)
    }

    override fun getItemCount(): Int = timeSlots.size
}

