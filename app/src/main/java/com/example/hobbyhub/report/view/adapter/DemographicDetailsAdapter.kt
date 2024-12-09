package com.example.hobbyhub.report.view.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.hobbyhub.R

class DemographicDetailsAdapter : RecyclerView.Adapter<DemographicDetailsAdapter.ViewHolder>() {

    private val data = mutableListOf<String>()

    fun updateData(
        ageGroups: Map<String, Int>,
        genderCounts: Map<String, Int>,
        locationCounts: Map<String, Int>
    ) {
        data.clear()
        ageGroups.forEach { (group, count) -> data.add("Age $group: $count users") }
        genderCounts.forEach { (gender, count) -> data.add("Gender $gender: $count users") }
        locationCounts.forEach { (location, count) -> data.add("Location $location: $count users") }
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_demographic_detail, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.textView.text = data[position]
    }

    override fun getItemCount() = data.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textView: TextView = itemView.findViewById(R.id.tvDetail)
    }
}
