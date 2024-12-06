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
import com.example.hobbyhub.report.model.UserActivityData
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.ValueFormatter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class UserActivityReportFragment : Fragment() {

    private val db = FirebaseFirestore.getInstance()
    private val userCollection = db.collection("user")
    private lateinit var adapter: UserActivityAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var barChart: BarChart
    private var featureStartTime: Long = 0L

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_user_activity, container, false)
        recyclerView = view.findViewById(R.id.rvUserActivity)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        barChart = view.findViewById(R.id.barChartTotalUsers)
        barChart = view.findViewById(R.id.barChartTotalUsageTime)

        fetchUserData()
        return view
    }

    private fun fetchUserData() {
        userCollection.get().addOnSuccessListener { documents ->
            val userData = documents.mapNotNull { doc ->
                val username = doc.getString("name")
                val createdAtMillis = doc.getLong("createdAt")
                val createdAt = createdAtMillis?.let { millis ->
                    val date = Date(millis)
                    val formatter = SimpleDateFormat(
                        "yyyy-MM-dd HH:mm:ss",
                        Locale.getDefault()
                    ) // Format as date and time
                    formatter.format(date)
                }
                val usageTime = doc.getLong("app_usage_time")
                if (username != null && createdAt != null && usageTime != null) {
                    UserActivityData(username, createdAt, usageTime)
                } else {
                    null
                }
            }

            displayUserActivity(userData)
            displayHistogram(userData)
        }.addOnFailureListener { e ->
            android.util.Log.e("UserActivityReport", "Error fetching user data", e)
        }
    }

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

    private fun displayHistogram(data: List<UserActivityData>) {
        // Total Users
        val totalUsers = data.size
        val totalUsersEntries = listOf(BarEntry(0f, totalUsers.toFloat()))
        val totalUsersLabels = listOf("Total Users")
        setupBarChart(
            barChart = view?.findViewById(R.id.barChartTotalUsers),
            entries = totalUsersEntries,
            labels = totalUsersLabels,
            chartTitle = "Total Users"
        )

        // Total App Usage Time
        val totalUsageTime = data.mapNotNull { it.appUsageTime?.toFloat() }.sum()
        val totalUsageTimeEntries = listOf(BarEntry(0f, totalUsageTime))
        val totalUsageTimeLabels = listOf("Total App Usage Time")
        setupBarChart(
            barChart = view?.findViewById(R.id.barChartTotalUsageTime),
            entries = totalUsageTimeEntries,
            labels = totalUsageTimeLabels,
            chartTitle = "Total App Usage Time (seconds)"
        )
    }

    private fun setupBarChart(
        barChart: BarChart?,
        entries: List<BarEntry>,
        labels: List<String>,
        chartTitle: String
    ) {
        if (barChart == null) return

        val barDataSet = BarDataSet(entries, chartTitle).apply {
            valueTextSize = 12f
            color = resources.getColor(R.color.selected_green, null)
        }

        val xAxis = barChart.xAxis
        xAxis.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                return if (value.toInt() in labels.indices) {
                    labels[value.toInt()]
                } else ""
            }
        }
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.granularity = 1f
        xAxis.setDrawGridLines(false)

        barChart.data = BarData(barDataSet)
        barChart.description.isEnabled = false
        barChart.animateY(1000)
        barChart.invalidate() // Refresh chart
    }
}
