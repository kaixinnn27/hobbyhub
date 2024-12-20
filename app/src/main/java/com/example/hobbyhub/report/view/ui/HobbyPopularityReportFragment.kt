package com.example.hobbyhub.report.view.ui

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.hobbyhub.R
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.utils.MPPointF
import com.google.firebase.firestore.FirebaseFirestore
import kotlin.math.abs

class HobbyPopularityReportFragment : Fragment() {

    private val db = FirebaseFirestore.getInstance()
    private lateinit var barChart: BarChart
    private lateinit var legendLayout: LinearLayout
    private lateinit var mostPopularHobbyTextView: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_hobby_popularity, container, false)
        barChart = view.findViewById(R.id.barChartHobbyPopularity)
        legendLayout = view.findViewById(R.id.legendLayout)
        mostPopularHobbyTextView = view.findViewById(R.id.tvMostPopularHobby)

        fetchHobbyPopularityData()
        return view
    }

    private fun fetchHobbyPopularityData() {
        db.collection("hobbies").get()
            .addOnSuccessListener { hobbyDocuments ->
                val hobbyMap = hobbyDocuments.associate {
                    it.id to (it.getString("name") ?: "Unknown")
                }
                android.util.Log.d("HobbyPopularityFragment", "hobbyMap: $hobbyMap")
                calculateUserHobbyPopularity(hobbyMap)
            }
            .addOnFailureListener { e ->
                android.util.Log.e("HobbyPopularityFragment", "Error fetching hobbies", e)
            }
    }

    private fun calculateUserHobbyPopularity(hobbyMap: Map<String, String>) {
        db.collection("userHobbies").get()
            .addOnSuccessListener { userHobbyDocuments ->
                val hobbyCounts = hobbyMap.keys.associateWith { 0 }.toMutableMap()

                for (document in userHobbyDocuments) {
                    val savedHobbies = document.get("savedHobbies") as? List<String> ?: continue
                    for (hobbyID in savedHobbies) {
                        hobbyCounts[hobbyID] = (hobbyCounts[hobbyID] ?: 0) + 1
                    }
                }

                val hobbyPopularity = hobbyCounts.mapKeys { hobbyMap[it.key] ?: "Unknown" }
                val mostPopularByPicks = hobbyPopularity.maxByOrNull { it.value }
                android.util.Log.d("HobbyPopularityFragment", "Most popular by picks: $mostPopularByPicks")
                fetchHobbyRatings(hobbyPopularity, hobbyMap, mostPopularByPicks)
            }
            .addOnFailureListener { e ->
                android.util.Log.e("HobbyPopularityFragment", "Error fetching user hobbies", e)
            }
    }

    private fun fetchHobbyRatings(
        hobbyPopularity: Map<String, Int>,
        hobbyMap: Map<String, String>,
        mostPopularByPicks: Map.Entry<String, Int>?
    ) {
        db.collection("reviews").get()
            .addOnSuccessListener { reviewDocuments ->
                val hobbyRatings = mutableMapOf<String, MutableList<Float>>()

                for (document in reviewDocuments) {
                    val hobbyId = document.getString("hobbyId") ?: continue
                    val rating = document.getDouble("rating")?.toFloat() ?: continue

                    hobbyRatings.getOrPut(hobbyId) { mutableListOf() }.add(rating)
                }

                val hobbyRatingsWithPopularity = hobbyRatings.mapKeys { (hobbyId, _) ->
                    hobbyMap[hobbyId] ?: "Unknown"
                }.mapValues { (hobbyName, ratings) ->
                    val popularity = hobbyPopularity[hobbyName] ?: 0
                    ratings.average().toFloat() to popularity
                }

                val mostPopularByRating = hobbyRatingsWithPopularity.maxByOrNull { it.value.first }

                displayMostPopularHobby(mostPopularByRating, mostPopularByPicks)
                displayHobbyChart(hobbyPopularity)
                populateLegend(hobbyPopularity)
            }
            .addOnFailureListener { e ->
                android.util.Log.e("HobbyPopularityFragment", "Error fetching reviews", e)
            }
    }

    private fun displayMostPopularHobby(
        mostPopularByRating: Map.Entry<String, Pair<Float, Int>>?,
        mostPopularByPicks: Map.Entry<String, Int>?
    ) {
        val ratingHobby = mostPopularByRating?.key ?: "N/A"
        val ratingValue = mostPopularByRating?.value?.first ?: 0

        val picksHobby = mostPopularByPicks?.key ?: "N/A"
        val picksCount = mostPopularByPicks?.value ?: 0

        // Log values for debugging
        android.util.Log.d("HobbyPopularityFragment", "ratingHobby: $ratingHobby")
        android.util.Log.d("HobbyPopularityFragment", "ratingValue: $ratingValue")
        android.util.Log.d("HobbyPopularityFragment", "picksHobby: $picksHobby")
        android.util.Log.d("HobbyPopularityFragment", "picksCount: $picksCount")

        // Proceed with setting the text
        mostPopularHobbyTextView.text = getString(
            R.string.most_popular_hobby_text,
            ratingHobby,
            ratingValue,
            picksHobby,
            picksCount
        )

        mostPopularHobbyTextView.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.teal_200))
        mostPopularHobbyTextView.setPadding(16, 16, 16, 16)
        mostPopularHobbyTextView.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
    }


    private fun displayHobbyChart(hobbyPopularity: Map<String, Int>) {
        val barEntries = hobbyPopularity.entries.mapIndexed { index, entry ->
            BarEntry(index.toFloat(), entry.value.toFloat())
        }

        val colors = generateUniqueColors(hobbyPopularity.size)

        val barDataSet = BarDataSet(barEntries, "Hobby Popularity").apply {
            valueTextSize = 12f
            setColors(colors)
        }

        val xLabels = hobbyPopularity.keys.toList()
        val xAxis = barChart.xAxis
        xAxis.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                return if (value.toInt() in xLabels.indices) {
                    xLabels[value.toInt()]
                } else ""
            }
        }
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.granularity = 1f
        xAxis.setDrawGridLines(false)
        xAxis.setDrawLabels(true)

        val markerView = HobbyMarkerView(requireContext(), xLabels)
        barChart.marker = markerView
        barChart.legend.isEnabled = false
        barChart.data = BarData(barDataSet)
        barChart.description.isEnabled = false
        barChart.animateY(1000)
        barChart.setScaleEnabled(true)
        barChart.setScaleMinima(8f, 1f) // Adjust the scale as needed
        barChart.centerViewTo(0f, 0f, barChart.axisLeft.axisDependency)
        barChart.invalidate()
    }

    private fun populateLegend(hobbyPopularity: Map<String, Int>) {
        legendLayout.removeAllViews()

        val colors = generateUniqueColors(hobbyPopularity.size)

        hobbyPopularity.keys.forEachIndexed { index, hobbyName ->
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
            legendLayout.addView(legendItem)
        }
    }

    private fun generateUniqueColors(size: Int): List<Int> {
        val colors = mutableListOf<Int>()
        val goldenRatio = 0.618033988749895
        var hue = abs(System.currentTimeMillis() % 360) / 360.0

        repeat(size) {
            hue += goldenRatio
            hue %= 1.0
            val color = Color.HSVToColor(floatArrayOf((hue * 360).toFloat(), 0.5f, 0.95f))
            colors.add(color)
        }

        return colors
    }

    class HobbyMarkerView(context: android.content.Context, private val labels: List<String>) :
        MarkerView(context, R.layout.marker_view_layout) {
        private val markerTextView: TextView = findViewById(R.id.markerText)

        override fun refreshContent(e: Entry?, highlight: Highlight?) {
            val index = e?.x?.toInt() ?: return
            val hobbyName = labels.getOrNull(index) ?: "Unknown"
            markerTextView.text = "Hobby: $hobbyName\nCount: ${e.y.toInt()}"
            super.refreshContent(e, highlight)
        }

        override fun getOffset(): MPPointF {
            return MPPointF(-width / 2f, -height.toFloat())
        }
    }
}
