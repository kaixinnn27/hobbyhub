package com.example.hobbyhub.report.view.ui

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.hobbyhub.R
import com.example.hobbyhub.databinding.FragmentUserFeedbackBinding
import com.example.hobbyhub.report.view.adapter.FeedbackCommentsAdapter
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore

class UserFeedbackAnalysisFragment : Fragment() {

    private var _binding: FragmentUserFeedbackBinding? = null
    private val binding get() = _binding!!
    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUserFeedbackBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fetchRatingDistribution()
        fetchFeedbackComments()
        fetchAverageRatingsPerHobby()
    }

    private fun fetchRatingDistribution() {
        db.collection("reviews").get()
            .addOnSuccessListener { documents ->
                val ratingCounts = mutableMapOf(1 to 0, 2 to 0, 3 to 0, 4 to 0, 5 to 0)

                for (document in documents) {
                    val rating = document.getLong("rating")?.toInt() ?: continue
                    ratingCounts[rating] = ratingCounts.getOrDefault(rating, 0) + 1
                }

                val entries = ratingCounts.map { (rating, count) ->
                    BarEntry(rating.toFloat(), count.toFloat())
                }

                val colors = listOf(
                    Color.RED, Color.YELLOW, Color.CYAN, Color.BLUE, Color.GREEN
                )

                val dataSet = BarDataSet(entries, "Rating Distribution").apply {
                    setColors(colors)
                    valueTextSize = 12f
                }
                val barData = BarData(dataSet)
                binding.barChartRatings.data = barData

                // Populate the legend using a custom LinearLayout
                populateLegendForRatings(colors)

                // Configure x-axis labels (use stars for ratings)
                binding.barChartRatings.xAxis.apply {
                    valueFormatter = object : ValueFormatter() {
                        override fun getFormattedValue(value: Float): String {
                            return "${value.toInt()} Star"
                        }
                    }
                    granularity = 1f
                    setDrawLabels(true)
                }

                binding.barChartRatings.description = Description().apply { text = "Ratings Distribution" }
                binding.barChartRatings.invalidate()
            }
            .addOnFailureListener { e ->
                Log.e("RatingDistribution", "Error fetching ratings: ${e.message}")
            }
    }

    private fun populateLegendForRatings(colors: List<Int>) {
        val legendLabels = listOf("1 Star", "2 Stars", "3 Stars", "4 Stars", "5 Stars")

        binding.legendRatingLayout.removeAllViews() // Assuming `legendLayout` is a LinearLayout in your layout XML.

        legendLabels.forEachIndexed { index, label ->
            val color = colors[index]

            val legendItem = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.HORIZONTAL
                setPadding(8, 8, 8, 8)
            }

            val legendColorBox = View(requireContext()).apply {
                layoutParams = LinearLayout.LayoutParams(40, 40).apply {
                    setMargins(0, 0, 16, 0)
                }
                setBackgroundColor(color)
            }

            val legendText = TextView(requireContext()).apply {
                text = label
                textSize = 14f
                setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
            }

            legendItem.addView(legendColorBox)
            legendItem.addView(legendText)
            binding.legendRatingLayout.addView(legendItem)
        }
    }

    private fun fetchFeedbackComments() {
        db.collection("reviews").get()
            .addOnSuccessListener { reviewDocuments ->
                val feedbackComments = mutableListOf<String>()
                val userIds = reviewDocuments.mapNotNull { it.getString("userId") }
                val userMap = mutableMapOf<String, String>()

                // Fetch user details using document IDs
                db.collection("user").whereIn(FieldPath.documentId(), userIds).get()
                    .addOnSuccessListener { userDocuments ->
                        for (user in userDocuments) {
                            val uid = user.id  // Use document ID as user ID
                            val username = user.getString("name") ?: "Unknown"
                            userMap[uid] = username
                            Log.e("FeedbackUser", "User ID: $uid, Username: $username")
                        }

                        // Map reviews to their respective users
                        for (document in reviewDocuments) {
                            val comment = document.getString("review") ?: "No comment"
                            val userId = document.getString("userId") ?: "Unknown"
                            val username = userMap[userId] ?: "Unknown"
                            Log.d("FeedbackComments", "User ID: $userId, Username: $username")
                            feedbackComments.add("$username: $comment")
                        }

                        // Bind comments to RecyclerView
                        binding.rvFeedbackComments.layoutManager = LinearLayoutManager(requireContext())
                        binding.rvFeedbackComments.adapter = FeedbackCommentsAdapter(feedbackComments)
                    }
                    .addOnFailureListener { e ->
                        Log.d("FeedbackComments", "Error fetching user data: ${e.message}")
                    }
            }
            .addOnFailureListener { e ->
                Log.e("FeedbackComments", "Error fetching reviews: ${e.message}")
            }
    }

    private fun fetchAverageRatingsPerHobby() {
        db.collection("reviews").get()
            .addOnSuccessListener { documents ->
                val hobbyRatings = mutableMapOf<String, MutableList<Float>>()

                // Collect ratings for each hobby
                for (document in documents) {
                    val hobbyId = document.getString("hobbyId") ?: continue
                    val rating = document.getDouble("rating")?.toFloat() ?: continue

                    hobbyRatings.getOrPut(hobbyId) { mutableListOf() }.add(rating)
                }

                val hobbyMap = mutableMapOf<String, String>()
                db.collection("hobbies").get()
                    .addOnSuccessListener { hobbyDocuments ->
                        for (hobby in hobbyDocuments) {
                            hobbyMap[hobby.id] = hobby.getString("name") ?: "Unknown"
                        }

                        val entries = mutableListOf<BarEntry>()
                        val hobbyNames = mutableListOf<String>()

                        // Prepare Bar Entries and Hobby Names
                        hobbyRatings.entries.forEachIndexed { index, (hobbyId, ratings) ->
                            val average = ratings.average().toFloat()
                            entries.add(BarEntry(index.toFloat(), average))
                            hobbyNames.add(hobbyMap[hobbyId] ?: "Unknown")
                        }

                        // Create dataset with unique colors
                        val colors = generateUniqueColors(hobbyNames.size)
                        val dataSet = BarDataSet(entries, "Average Rating per Hobby").apply {
                            setColors(colors)
                            valueTextSize = 12f
                        }

                        val barData = BarData(dataSet)
                        binding.barChartSentiment.data = barData

                        // Populate legend below the chart
                        populateLegend(hobbyNames, colors)

                        // Configure x-axis labels (use empty since legends show labels)
                        binding.barChartSentiment.xAxis.apply {
                            valueFormatter = object : IndexAxisValueFormatter() {
                                override fun getFormattedValue(value: Float): String = ""
                            }
                            setDrawLabels(false)
                        }

                        // Configure chart description
                        binding.barChartSentiment.description = Description().apply {
                            text = "Average Hobby Ratings"
                        }

                        binding.barChartSentiment.invalidate()
                    }
            }
            .addOnFailureListener { e ->
                Log.e("UserFeedbackAnalysis", "Error fetching reviews for hobbies: ${e.message}")
            }
    }

    private fun populateLegend(hobbyNames: List<String>, colors: List<Int>) {
        binding.legendLayout.removeAllViews() // Assuming `legendLayout` is a LinearLayout in your layout XML.

        hobbyNames.forEachIndexed { index, hobbyName ->
            val color = colors[index]

            val legendItem = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.HORIZONTAL
                setPadding(8, 8, 8, 8)
            }

            val legendColorBox = View(requireContext()).apply {
                layoutParams = LinearLayout.LayoutParams(40, 40).apply {
                    setMargins(0, 0, 16, 0)
                }
                setBackgroundColor(color)
            }

            val legendText = TextView(requireContext()).apply {
                text = hobbyName
                textSize = 14f
                setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
            }

            legendItem.addView(legendColorBox)
            legendItem.addView(legendText)
            binding.legendLayout.addView(legendItem)
        }
    }

    private fun generateUniqueColors(size: Int): List<Int> {
        val colors = mutableListOf<Int>()
        val goldenRatio = 0.618033988749895
        var hue = (System.currentTimeMillis() % 360) / 360.0

        repeat(size) {
            hue += goldenRatio
            hue %= 1.0
            colors.add(Color.HSVToColor(floatArrayOf((hue * 360).toFloat(), 0.7f, 0.9f)))
        }

        return colors
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
