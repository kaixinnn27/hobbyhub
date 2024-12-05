package com.example.hobbyhub

import android.os.Bundle
import android.os.SystemClock
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.example.hobbyhub.databinding.ActivityMainBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val db = FirebaseFirestore.getInstance()
    private val userId = FirebaseAuth.getInstance().currentUser?.uid
    private var sessionStartTime: Long = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sessionStartTime = SystemClock.elapsedRealtime()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val isAdmin = intent.getBooleanExtra("isAdmin", false)
        setupBottomNavigation(isAdmin)
    }

    private fun setupBottomNavigation(isAdmin: Boolean) {
        val navView: BottomNavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_activity_main)

        if (isAdmin) {
            addReportModule(navView)
        } else {
            removeReportModule(navView)
        }

        navView.setupWithNavController(navController)
    }

    private fun addReportModule(navView: BottomNavigationView) {
        val menu = navView.menu
        if (menu.findItem(R.id.navigation_report) == null) { // Avoid duplicate menu item
            menu.add(
                0, R.id.navigation_report, menu.size(), getString(R.string.report_title)
            ).setIcon(R.drawable.ic_report) // Add report button with icon
        }
    }

    private fun removeReportModule(navView: BottomNavigationView) {
        val menu = navView.menu
        menu.removeItem(R.id.navigation_report) // Ensure report module is not shown
    }

    override fun onStop() {
        super.onStop()
        // Track session end
        val sessionEndTime = SystemClock.elapsedRealtime()
        val sessionDuration = (sessionEndTime - sessionStartTime) / 1000 // Convert ms to seconds
        updateUsageTime(sessionDuration)
    }

    private fun updateUsageTime(duration: Long) {
        if (userId != null) {
            val userRef = db.collection("user").document(userId)
            db.runTransaction { transaction ->
                val snapshot = transaction.get(userRef)
                val currentUsageTime = snapshot.getLong("app_usage_time") ?: 0
                transaction.update(userRef, "app_usage_time", currentUsageTime + duration)
            }.addOnSuccessListener {
                android.util.Log.d("MainActivity", "App usage time updated successfully.")
            }.addOnFailureListener { e ->
                android.util.Log.e("MainActivity", "Failed to update app usage time", e)
            }
        }
    }
}
