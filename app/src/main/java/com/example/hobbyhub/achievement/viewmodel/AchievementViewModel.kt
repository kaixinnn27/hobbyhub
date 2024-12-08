package com.example.hobbyhub.achievement.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.hobbyhub.authentication.model.User
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.ZoneId

class AchievementViewModel : ViewModel() {
    private val col = Firebase.firestore.collection("achievements")
    private val usersCol= Firebase.firestore.collection("user")

    private val _streaks = MutableLiveData<Map<String, Any>>()
    val streaks: LiveData<Map<String, Any>> get() = _streaks

    init {
        val firebaseUser = FirebaseAuth.getInstance().currentUser
        firebaseUser?.uid?.let { updateDailyStreak(it) }
    }

    suspend fun getStreakLeaderboard(): List<Pair<User, Int>> {
        return withContext(Dispatchers.IO) {
            try {
                // 1. Fetch all achievements
                val achievementsSnapshot = col.get().await()

                // 2. Create a list to hold user IDs and their best streaks
                val userStreaks = mutableListOf<Pair<String, Int>>()

                // Loop through achievements to extract user IDs and best streaks
                for (achievementDoc in achievementsSnapshot.documents) {
                    val userId = achievementDoc.id
                    val streakData = achievementDoc["streaks"] as? Map<String, Any>
                    Log.d("getStreakLeaderboard", "streakData -> $streakData")
                    val bestStreak = streakData?.get("bestStreak") as? Long ?: 0
                    Log.d("getStreakLeaderboard", "userId -> $userId bestStreak -> $bestStreak")
                    userStreaks.add(Pair(userId, bestStreak.toInt()))
                }

                // 3. Fetch user data for each user in the userStreaks list
                val userList = mutableListOf<User>()
                for ((userId, _) in userStreaks) {
                    val userDoc = usersCol.document(userId).get().await()
                    val user = userDoc.toObject(User::class.java)
                    if (user != null) {
                        userList.add(user)
                    }
                }

                // 4. Combine users and their best streaks into a list
                val userWithStreaks = userStreaks.mapNotNull { (userId, bestStreak) ->
                    val user = userList.find { it.id == userId }
                    user?.let { Pair(user, bestStreak) }
                }

                // Sort users by best streak in descending order and return top 3
                return@withContext userWithStreaks
                    .sortedByDescending { it.second } // Sort by streaks in descending order
                    .take(3) // Get top 3
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Error fetching streak leaderboard: $e")
                return@withContext emptyList<Pair<User, Int>>() // Return empty list if error occurs
            }
        }
    }

    fun updateDailyStreak(userId: String) {
        val today = LocalDate.now()
        val ref = col.document(userId)

        ref.get().addOnSuccessListener { document ->
            if (document.exists()) {
                val streakData = document.get("streaks") as? Map<*, *>
                val lastUpdated = (streakData?.get("lastUpdated") as? Timestamp)?.toDate()
                val currentValue = (streakData?.get("currentValue") as? Long)?.toInt() ?: 0
                val bestStreak = (streakData?.get("bestStreak") as? Long)?.toInt() ?: 0
                val milestones = (streakData?.get("milestones") as? List<*>)
                    ?.filterIsInstance<Int>() ?: emptyList()

                val lastDate = lastUpdated?.toInstant()?.atZone(ZoneId.systemDefault())?.toLocalDate()
                val newStreak = if (lastDate == today.minusDays(1)) {
                    currentValue + 1 // Increment streak
                } else if (lastDate != today) {
                    1 // Reset streak if not consecutive
                } else {
                    currentValue // No change for same-day check-in
                }

                // Update the best streak if current streak surpasses it
                val newBestStreak = if (newStreak > bestStreak) newStreak else bestStreak

                // Update milestones
                val updatedMilestones = milestones.toMutableList()
                milestones.forEach { milestone ->
                    if (newStreak >= milestone && !updatedMilestones.contains(milestone)) {
                        updatedMilestones.add(milestone)
                        notifyUser(userId, "Milestone Reached: $milestone-Day Streak!")
                    }
                }

                // Update Firestore
                val updatedData = mapOf(
                    "currentValue" to newStreak,
                    "bestStreak" to newBestStreak, // Update the best streak
                    "lastUpdated" to Timestamp.now(),
                    "milestones" to updatedMilestones
                )
                ref.update(mapOf("streaks" to updatedData))
                    .addOnSuccessListener {
                        // Post updated streak data to LiveData
                        _streaks.postValue(updatedData)
                    }
                    .addOnFailureListener { e ->
                        // Log or handle failure
                        e.printStackTrace()
                    }
            } else {
                // Initialize streaks if not present
                initializeStreaks(ref)
            }
        }.addOnFailureListener { e ->
            // Log or handle failure
            e.printStackTrace()
        }
    }

    private fun initializeStreaks(ref: DocumentReference) {
        val initialData = mapOf(
            "currentValue" to 1,
            "bestStreak" to 1, // Set initial best streak to 1
            "lastUpdated" to Timestamp.now(),
            "milestones" to emptyList<Int>()
        )
        ref.set(mapOf("streaks" to initialData), SetOptions.merge())
            .addOnSuccessListener {
                // Post initialized streak data to LiveData
                _streaks.postValue(initialData)
            }
            .addOnFailureListener { e ->
                // Log or handle failure
                e.printStackTrace()
            }
    }

    private fun notifyUser(userId: String, message: String) {
        // Add notification logic here, such as showing a Toast or in-app alert
        println("Notify $userId: $message")
    }
}
