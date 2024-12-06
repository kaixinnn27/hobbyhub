package com.example.hobbyhub.scheduling.view.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.hobbyhub.R

class TimeSlotsAdapter(
    private val onSlotSelected: (String) -> Unit
) : RecyclerView.Adapter<TimeSlotsAdapter.TimeSlotViewHolder>() {

    private val timeSlots = mutableListOf<String>()
    private var selectedPosition = RecyclerView.NO_POSITION

    fun submitList(slots: List<String>) {
        timeSlots.clear()
        timeSlots.addAll(slots)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TimeSlotViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_time_slot, parent, false)
        return TimeSlotViewHolder(view)
    }

    override fun onBindViewHolder(holder: TimeSlotViewHolder, position: Int) {
        val slot = timeSlots[position]
        holder.bind(slot, position == selectedPosition)

        holder.itemView.setOnClickListener {
            val previousPosition = selectedPosition
            selectedPosition = holder.adapterPosition
            notifyItemChanged(previousPosition) // Refresh previous selection
            notifyItemChanged(selectedPosition) // Refresh current selection
            onSlotSelected(slot)
        }
    }

    override fun getItemCount(): Int = timeSlots.size

    class TimeSlotViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textView: TextView = itemView.findViewById(R.id.tvTimeSlot)

        fun bind(slot: String, isSelected: Boolean) {
            textView.text = slot
            textView.isSelected = isSelected
            textView.setBackgroundResource(
                if (isSelected) R.drawable.bg_time_slot_selected else R.drawable.bg_time_slot_unselected
            )
        }
    }
}
