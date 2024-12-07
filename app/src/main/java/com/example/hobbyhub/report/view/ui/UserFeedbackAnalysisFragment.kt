package com.example.hobbyhub.report.view.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.hobbyhub.databinding.FragmentUserFeedbackBinding
import com.example.hobbyhub.report.view.adapter.FeedbackCommentsAdapter
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry

class UserFeedbackAnalysisFragment : Fragment() {

    private var _binding: FragmentUserFeedbackBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentUserFeedbackBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRatingDistribution()
        setupFeedbackComments()
        setupFeedbackThemes()
        setupSentimentAnalysis()
    }

    private fun setupRatingDistribution() {
        val barChart = binding.barChartRatings
        val entries = listOf(
            BarEntry(1f, 50f),  // 1-star ratings
            BarEntry(2f, 30f),  // 2-star ratings
            BarEntry(3f, 20f),  // 3-star ratings
            BarEntry(4f, 70f),  // 4-star ratings
            BarEntry(5f, 100f)  // 5-star ratings
        )
        val dataSet = BarDataSet(entries, "Rating Distribution")
        val barData = BarData(dataSet)

        barChart.data = barData
        barChart.description = Description().apply { text = "Ratings Distribution" }
        barChart.invalidate() // Refresh chart
    }

    private fun setupFeedbackComments() {
        val recyclerView = binding.rvFeedbackComments
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        val feedbackComments = listOf(
            "Great app! Loved the interface.",
            "The app crashes sometimes.",
            "Could use better support for dark mode."
        )
        recyclerView.adapter = FeedbackCommentsAdapter(feedbackComments)
    }

    private fun setupFeedbackThemes() {
        val pieChart = binding.pieChartFeedbackThemes
        val entries = listOf(
            PieEntry(40f, "Usability"),
            PieEntry(30f, "Performance"),
            PieEntry(20f, "Features"),
            PieEntry(10f, "Other")
        )
        val dataSet = PieDataSet(entries, "Feedback Themes")
        val pieData = PieData(dataSet)

        pieChart.data = pieData
        pieChart.description = Description().apply { text = "Common Feedback Themes" }
        pieChart.invalidate() // Refresh chart
    }

    private fun setupSentimentAnalysis() {
        val barChart = binding.barChartSentiment
        val entries = listOf(
            BarEntry(1f, 60f),  // Positive Sentiments
            BarEntry(2f, 20f),  // Neutral Sentiments
            BarEntry(3f, 20f)   // Negative Sentiments
        )
        val dataSet = BarDataSet(entries, "Sentiment Analysis")
        val barData = BarData(dataSet)

        barChart.data = barData
        barChart.description = Description().apply { text = "Sentiment Analysis" }
        barChart.invalidate() // Refresh chart
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
