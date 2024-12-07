package com.example.hobbyhub.report.view.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.hobbyhub.R

class FeedbackCommentsAdapter(
    private val comments: List<String>
) : RecyclerView.Adapter<FeedbackCommentsAdapter.FeedbackViewHolder>() {

    // ViewHolder to bind each feedback comment
    inner class FeedbackViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvComment: TextView = itemView.findViewById(R.id.tvComment)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FeedbackViewHolder {
        // Inflate the item layout for feedback comments
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_feedback_comment, parent, false)
        return FeedbackViewHolder(view)
    }

    override fun onBindViewHolder(holder: FeedbackViewHolder, position: Int) {
        // Bind each comment to the TextView
        holder.tvComment.text = comments[position]
    }

    override fun getItemCount(): Int {
        return comments.size
    }
}
