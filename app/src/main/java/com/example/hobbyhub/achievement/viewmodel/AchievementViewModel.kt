package com.example.hobbyhub.achievement.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import java.time.LocalDate
import java.time.ZoneId

class AchievementViewModel : ViewModel() {
    private val col = Firebase.firestore.collection("achievements")

    private val _streaks = MutableLiveData<Map<String, Any>>()
    val streaks: LiveData<Map<String, Any>> get() = _streaks

    init {
        val firebaseUser = FirebaseAuth.getInstance().currentUser
        firebaseUser?.uid?.let { updateDailyStreak(it) }
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
