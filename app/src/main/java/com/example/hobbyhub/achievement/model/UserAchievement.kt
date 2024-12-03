package com.example.hobbyhub.achievement.model

import com.google.firebase.firestore.DocumentId

data class UserAchievements(
    @DocumentId
    var id: String = "",
    var badges: List<Badge> = emptyList(),
    var leaderboardPositions: Map<String, LeaderboardPosition> = emptyMap()
)

data class Badge(
    val badgeId: String = "",
    val badgeName: String = "",
    val dateEarned: Long = System.currentTimeMillis()
)

data class LeaderboardPosition(
    val rank: Int = 0,
    val points: Int = 0
)