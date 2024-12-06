package com.example.hobbyhub.scheduling.view.ui

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.hobbyhub.R

class TimeSlotAdapter(
    private val context: Context,
    private val busySlots: List<String>
) : RecyclerView.Adapter<TimeSlotAdapter.TimeSlotViewHolder>() {

    inner class TimeSlotViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val timeTextView: TextView = itemView.findViewById(R.id.tvTimeSlot)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TimeSlotViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_time_slot, parent, false)
        return TimeSlotViewHolder(view)
    }

    override fun onBindViewHolder(holder: TimeSlotViewHolder, position: Int) {
        val timeSlot = busySlots[position]
        holder.timeTextView.text = timeSlot
        holder.timeTextView.setBackgroundColor(ContextCompat.getColor(context, R.color.dark_gray))
        holder.timeTextView.setTextColor(ContextCompat.getColor(context, android.R.color.white))
    }

    override fun getItemCount(): Int = busySlots.size
}
