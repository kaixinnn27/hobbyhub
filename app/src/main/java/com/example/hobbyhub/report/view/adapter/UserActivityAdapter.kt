package com.example.hobbyhub.report.view.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.hobbyhub.R
import com.example.hobbyhub.report.view.ui.UserActivityReportFragment
import java.text.SimpleDateFormat
import java.util.Locale

class UserActivityAdapter(
    private val data: List<UserActivityReportFragment.UserActivityData>
) : RecyclerView.Adapter<UserActivityAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvUserId: TextView = itemView.findViewById(R.id.tvUserId)
        private val tvCreatedAt: TextView = itemView.findViewById(R.id.tvCreatedAt)
        private val tvAppUsageTime: TextView = itemView.findViewById(R.id.tvAppUsageTime)

        fun bind(item: UserActivityReportFragment.UserActivityData) {
            tvUserId.text = "User ID: ${item.userId}"
            tvCreatedAt.text = "Created At: ${SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(item.createdAt)}"
            tvAppUsageTime.text = "App Usage Time: ${item.appUsageTime / 60} minutes"
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_user_activity, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(data[position])
    }

    override fun getItemCount(): Int = data.size
}