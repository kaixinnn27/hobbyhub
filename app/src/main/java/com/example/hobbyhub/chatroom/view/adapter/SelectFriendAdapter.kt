package com.example.hobbyhub.chatroom.view.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.hobbyhub.R
import com.example.hobbyhub.chatroom.model.Friend

class SelectFriendAdapter : RecyclerView.Adapter<SelectFriendAdapter.ViewHolder>() {

    private val friends = mutableListOf<Friend>()
    private val selectedFriends = mutableSetOf<String>()

    @SuppressLint("NotifyDataSetChanged")
    fun submitList(friendList: List<Friend>) {
        friends.clear()
        friends.addAll(friendList)
        notifyDataSetChanged()
    }

    fun getSelectedFriends(): List<String> {
        return selectedFriends.toList()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_select_friend, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val friend = friends[position]
        holder.bind(friend)
    }

    override fun getItemCount(): Int = friends.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvName: TextView = itemView.findViewById(R.id.tvFriendName)
        private val checkBox: CheckBox = itemView.findViewById(R.id.cbSelectFriend)

        fun bind(friend: Friend) {
            tvName.text = friend.name
            checkBox.isChecked = selectedFriends.contains(friend.id)

            checkBox.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    selectedFriends.add(friend.id)
                } else {
                    selectedFriends.remove(friend.id)
                }
            }

            itemView.setOnClickListener {
                checkBox.isChecked = !checkBox.isChecked
            }
        }
    }
}
