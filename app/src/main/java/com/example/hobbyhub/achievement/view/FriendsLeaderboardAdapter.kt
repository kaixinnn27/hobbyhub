package com.example.hobbyhub.achievement.view

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.hobbyhub.R
import com.example.hobbyhub.authentication.model.User
import com.example.hobbyhub.databinding.ItemLeaderboardBinding
import com.example.hobbyhub.utility.toBitmap

class FriendsLeaderboardAdapter(private val leaderboard: List<User>) :
    RecyclerView.Adapter<FriendsLeaderboardAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemLeaderboardBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemLeaderboardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user = leaderboard[position]
        holder.binding.userName.text = user.name
        holder.binding.userDetails.text = "${user.friends.size} friends"

        val badgeResource = getBadgeForStreak(position)
        holder.binding.userBadges.setImageResource(badgeResource)

        if(user.photo.toBitmap()!= null){
            holder.binding.userAvatar.setImageBitmap(user.photo.toBitmap())
        }
    }

    override fun getItemCount(): Int = leaderboard.size

    private fun getBadgeForStreak(position: Int): Int {
        return when {
            position == 0 -> R.drawable.ic_1st
            position == 1 -> R.drawable.ic_2nd
            else -> R.drawable.ic_3rd
        }
    }
}
