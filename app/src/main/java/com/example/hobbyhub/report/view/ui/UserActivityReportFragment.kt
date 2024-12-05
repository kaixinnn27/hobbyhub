package com.example.hobbyhub.report.view.ui

import android.os.Bundle
import android.os.SystemClock
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.hobbyhub.R
import com.example.hobbyhub.report.view.adapter.UserActivityAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Date

class UserActivityReportFragment : Fragment() {

    private val db = FirebaseFirestore.getInstance()
    private val userCollection = db.collection("user")
    private lateinit var adapter: UserActivityAdapter
    private lateinit var recyclerView: RecyclerView
    private var featureStartTime: Long = 0L

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_user_activity, container, false)
        recyclerView = view.findViewById(R.id.rvUserActivity)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        fetchUserData()
        return view
    }

    private fun fetchUserData() {
        userCollection.get().addOnSuccessListener { documents ->
            val userData = documents.mapNotNull { doc ->
                val createdAt = doc.getTimestamp("createdAt")?.toDate()
                val usageTime = doc.getLong("app_usage_time")
                if (createdAt != null && usageTime != null) {
                    val userActivityData = UserActivityData(doc.id, createdAt, usageTime)
                    android.util.Log.d("UserActivityReport", "Fetched Data: $userActivityData")
                    android.util.Log.w(
                        "UserActivityReport",
                        "Document ID ${doc.id} is missing required fields. created_at: $createdAt, app_usage_time: $usageTime"
                    )
                    UserActivityData(doc.id, createdAt, usageTime)
                } else {
                    android.util.Log.w(
                        "UserActivityReport",
                        "Document ID ${doc.id} is missing required fields. created_at: $createdAt, app_usage_time: $usageTime"
                    )
                    android.util.Log.w("UserActivityReport", "Incomplete Data for document ID: ${doc.id}")
                    null
                }
            }

            android.util.Log.d("UserActivityReport", "Total Users Fetched: ${userData.size}")

            // Pass the data to the UI
            displayUserActivity(userData)
        }.addOnFailureListener { e ->
            android.util.Log.e("UserActivityReport", "Error fetching user data", e)
        }
    }

    data class UserActivityData(
        val userId: String,
        val createdAt: Date,
        val appUsageTime: Long // in seconds
    )

    private fun displayUserActivity(data: List<UserActivityData>) {
        adapter = UserActivityAdapter(data)
        recyclerView.adapter = adapter
    }

    override fun onResume() {
        super.onResume()
        featureStartTime = SystemClock.elapsedRealtime()
    }

    override fun onPause() {
        super.onPause()
        val featureEndTime = SystemClock.elapsedRealtime()
        val featureUsageDuration = (featureEndTime - featureStartTime) / 1000
        updateFeatureUsageTime(featureUsageDuration)
    }

    private fun updateFeatureUsageTime(duration: Long) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            val userRef = FirebaseFirestore.getInstance().collection("user").document(userId)
            FirebaseFirestore.getInstance().runTransaction { transaction ->
                val snapshot = transaction.get(userRef)
                val currentFeatureTime = snapshot.getLong("feature_usage_time") ?: 0
                transaction.update(userRef, "feature_usage_time", currentFeatureTime + duration)
            }.addOnSuccessListener {
                android.util.Log.d("UserActivityReportFragment", "Feature usage time updated successfully.")
            }.addOnFailureListener { e ->
                android.util.Log.e("UserActivityReportFragment", "Failed to update feature usage time", e)
            }
        }
    }
}