package com.example.hobbyhub.achievement.view

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.hobbyhub.BaseActivity
import com.example.hobbyhub.R
import com.example.hobbyhub.achievement.viewmodel.AchievementViewModel
import com.example.hobbyhub.authentication.viewmodel.AuthViewModel
import com.example.hobbyhub.chatroom.viewmodel.ChatViewModel
import com.example.hobbyhub.databinding.ActivityAchievementBinding
import com.example.hobbyhub.hobby.viewmodel.HobbyViewModel
import com.example.hobbyhub.hobby.viewmodel.UserHobbyViewModel
import kotlinx.coroutines.launch

class AchievementActivity : BaseActivity() {

    private lateinit var binding: ActivityAchievementBinding
    private val authViewModel: AuthViewModel by viewModels()
    private val achievementViewModel: AchievementViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAchievementBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupToolbar()
        setupStreaks()
        setupFriendRewards()
        setupFriendLeaderboard()
        setupStreakLeaderboard()
    }

    private fun setupFriendLeaderboard() {
        lifecycleScope.launch {
            val users = authViewModel.getFriendsLeaderboard()
            binding.friendsLeaderboardRecyclerView.layoutManager = LinearLayoutManager(this@AchievementActivity)
            binding.friendsLeaderboardRecyclerView.adapter = FriendsLeaderboardAdapter(users)
        }
    }

    private fun setupStreakLeaderboard() {
        lifecycleScope.launch {
            val users = achievementViewModel.getStreakLeaderboard()
            Log.d("setupStreakLeaderboard", "$users")
            binding.streaksLeaderboardRecyclerView.layoutManager = LinearLayoutManager(this@AchievementActivity)
            binding.streaksLeaderboardRecyclerView.adapter = StreakLeaderboardAdapter(users)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setupFriendRewards() {
        val userId = authViewModel.getCurrentUserId()
        if (userId != null) {
            lifecycleScope.launch {
                val friendSize = authViewModel.getFriendSize(userId)
                binding.friendTextView.text = "$friendSize friends"

                val badgeResource = getBadgeForFriends(friendSize)
                binding.socialRewardImg.setImageResource(badgeResource)
            }
        }
    }

    private fun getBadgeForFriends(friendSize: Int): Int {
        return when {
            friendSize >= 7 -> R.drawable.ic_diamond
            friendSize >= 5 -> R.drawable.ic_gold
            friendSize >= 3 -> R.drawable.ic_silver
            else -> R.drawable.ic_bronze
        }
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.toolbar.setNavigationOnClickListener { onBackPressed() }
    }

    @SuppressLint("SetTextI18n")
    private fun setupStreaks() {
        achievementViewModel.streaks.observe(this@AchievementActivity) { streakData ->
            Log.d("setupStreaks", "$streakData")
            // Update the UI with streak data (current streak and best streak)
            val currentStreak = streakData["currentValue"] as? Int ?: 0
            val bestStreak = streakData["bestStreak"] as? Int ?: 0

            // Display current streak and best streak in your TextViews
            binding.streakTextView.text = "$currentStreak days"
            binding.bestStreaksTv.text = "$bestStreak days"

            val badgeResource = getBadgeForStreak(bestStreak)
            binding.bestStreakReward.setImageResource(badgeResource)
        }
    }

    private fun getBadgeForStreak(bestStreak: Int): Int {
        return when {
            bestStreak >= 7 -> R.drawable.ic_diamond
            bestStreak >= 5 -> R.drawable.ic_gold
            bestStreak >= 3 -> R.drawable.ic_silver
            else -> R.drawable.ic_bronze
        }
    }
}