package com.example.hobbyhub.chatroom.view.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.hobbyhub.chatroom.model.ChatItem
import com.example.hobbyhub.databinding.ItemPersonBinding
import com.example.hobbyhub.databinding.ItemGroupBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class ChatAdapter(
    private val context: Context,
    private val onItemClick: (ChatItem) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val chatItems = mutableListOf<ChatItem>()

    // Add items to the list
    fun submitList(newItems: List<ChatItem>) {
        chatItems.clear()
        chatItems.addAll(newItems)
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int {
        return when (chatItems[position]) {
            is ChatItem.FriendItem -> VIEW_TYPE_FRIEND
            is ChatItem.GroupItem -> VIEW_TYPE_GROUP
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_FRIEND -> {
                val binding = ItemPersonBinding.inflate(LayoutInflater.from(context), parent, false)
                FriendViewHolder(binding)
            }

            VIEW_TYPE_GROUP -> {
                val binding = ItemGroupBinding.inflate(LayoutInflater.from(context), parent, false)
                GroupViewHolder(binding)
            }

            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is FriendViewHolder -> holder.bind(chatItems[position] as ChatItem.FriendItem)
            is GroupViewHolder -> holder.bind(chatItems[position] as ChatItem.GroupItem)
        }
    }

    override fun getItemCount(): Int = chatItems.size

    inner class FriendViewHolder(private val binding: ItemPersonBinding) :
        RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("SetTextI18n")
        fun bind(friendItem: ChatItem.FriendItem) {
            val friend = friendItem.friend
            binding.tvUsername.text = friend.name
            if (friend.lastMessageTimestamp.toString() != "") {
                binding.tvLastMsgTime.text = formatTimestamp(friend.lastMessageTimestamp)
            } else {
                binding.tvLastMsgTime.visibility = View.GONE
            }
            binding.root.setOnClickListener {
                onItemClick(friendItem)
            }
        }
    }

    inner class GroupViewHolder(private val binding: ItemGroupBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(groupItem: ChatItem.GroupItem) {
            val group = groupItem.group
            binding.tvUsername.text = group.name
            binding.root.setOnClickListener {
                onItemClick(groupItem)
            }
        }
    }

    companion object {
        const val VIEW_TYPE_FRIEND = 0
        const val VIEW_TYPE_GROUP = 1
    }

    private fun formatTimestamp(timestamp: Long): String {
        if (timestamp == 0L) return ""

        val currentTime = System.currentTimeMillis()
        val date = Date(timestamp)
        val calendar = Calendar.getInstance()
        calendar.time = date

        val todayCalendar = Calendar.getInstance()
        todayCalendar.timeInMillis = currentTime

        return if (calendar.get(Calendar.YEAR) == todayCalendar.get(Calendar.YEAR) &&
            calendar.get(Calendar.DAY_OF_YEAR) == todayCalendar.get(Calendar.DAY_OF_YEAR)
        ) {
            // If the timestamp is from today, return the time format
            val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
            timeFormat.format(date)
        } else {
            // Otherwise, return the date format
            val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            dateFormat.format(date)
        }
    }
}
