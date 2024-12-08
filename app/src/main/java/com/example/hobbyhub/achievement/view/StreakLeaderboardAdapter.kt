package com.example.hobbyhub.achievement.view

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.hobbyhub.R
import com.example.hobbyhub.authentication.model.User
import com.example.hobbyhub.databinding.ItemLeaderboardBinding

class StreakLeaderboardAdapter(private val leaderboard: List<Pair<User, Int>>) :
    RecyclerView.Adapter<StreakLeaderboardAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemLeaderboardBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemLeaderboardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val (user, bestStreak) = leaderboard[position]
        holder.binding.userName.text = user.name
        holder.binding.userDetails.text = "$bestStreak days streak"

        // Optionally, you can display badges or other indicators depending on the streak
        val badgeResource = getBadgeForStreak(position)
        holder.binding.userBadges.setImageResource(badgeResource)
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