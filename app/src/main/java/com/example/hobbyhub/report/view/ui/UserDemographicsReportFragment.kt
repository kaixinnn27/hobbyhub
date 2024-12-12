package com.example.hobbyhub.report.view.ui

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.hobbyhub.R
import com.example.hobbyhub.databinding.FragmentUserDemographicsReportBinding
import com.example.hobbyhub.report.view.adapter.DemographicDetailsAdapter
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.PercentFormatter
import com.google.firebase.firestore.FirebaseFirestore

class UserDemographicsReportFragment : Fragment() {

    private var _binding: FragmentUserDemographicsReportBinding? = null
    private val binding get() = _binding!!
    private val db = FirebaseFirestore.getInstance()
    private lateinit var barChart: BarChart
    private lateinit var pieChartAge: PieChart
    private lateinit var pieChartGender: PieChart
    private lateinit var pieChartLocation: PieChart
    private lateinit var detailsAdapter: DemographicDetailsAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUserDemographicsReportBinding.inflate(inflater, container, false)
        barChart = binding.barChartDemographics
        pieChartAge = binding.pieChartAge
        pieChartGender = binding.pieChartGender
        pieChartLocation = binding.pieChartLocation

        setupRecyclerView() // No argument needed
        fetchHobbiesAndDemographics()

        return binding.root
    }

    private fun setupRecyclerView() {
        val recyclerView = binding.rvDemographicsDetails
        detailsAdapter = DemographicDetailsAdapter()
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = detailsAdapter
    }

    private fun fetchHobbiesAndDemographics() {
        // Fetch hobbies first
        db.collection("hobbies").get()
            .addOnSuccessListener { hobbyDocuments ->
                // Map hobby IDs to their names
                val hobbyMap = hobbyDocuments.associate {
                    it.id to (it.getString("name") ?: "Unknown")
                }
                android.util.Log.d("UserDemographicsFragment", "Hobbies Retrieved: $hobbyMap")

                // Pass hobbyMap to demographics fetch
                fetchDemographicsData(hobbyMap)
            }
            .addOnFailureListener { e ->
                android.util.Log.e("UserDemographicsFragment", "Error fetching hobbies", e)
            }
    }

    private fun fetchDemographicsData(hobbyMap: Map<String, String>) {
        db.collection("userHobbies").get()
            .addOnSuccessListener { userHobbyDocuments ->
                val userHobbiesMap = mutableMapOf<String, List<String>>()

                // Map user IDs to savedHobbies
                for (document in userHobbyDocuments) {
                    val userId = document.id
                    val savedHobbies = document.get("savedHobbies") as? List<String> ?: emptyList()
                    userHobbiesMap[userId] = savedHobbies
                }
                fetchDemographicsWithHobbies(userHobbiesMap, hobbyMap)
            }
            .addOnFailureListener { e ->
                android.util.Log.e("UserDemographicsFragment", "Error fetching user hobbies", e)
            }
    }

    private fun fetchDemographicsWithHobbies(
        userHobbiesMap: Map<String, List<String>>,
        hobbyMap: Map<String, String>
    ) {
        db.collection("demographics").get()
            .addOnSuccessListener { userDocuments ->
                val ageGroups = mutableMapOf<String, Int>()
                val genderCounts = mutableMapOf<String, Int>()
                val locationCounts = mutableMapOf<String, Int>()
                val hobbiesByDemographics = mutableMapOf<String, Int>()
                val ageGroupHobbies = mutableMapOf<String, MutableMap<String, Int>>()
                val genderHobbies = mutableMapOf<String, MutableMap<String, Int>>()
                val locationHobbies = mutableMapOf<String, MutableMap<String, Int>>()

                for (document in userDocuments) {
                    val userId = document.id
                    val age = document.getLong("age") ?: 0
                    val gender = document.getString("gender") ?: "Unknown"
                    val location = document.getString("location") ?: "Unknown"
                    val savedHobbies = userHobbiesMap[userId]?.mapNotNull { hobbyId ->
                        hobbyMap[hobbyId] // Resolve hobby ID to name
                    } ?: emptyList()

                    // Age Group
                    val ageGroup = when {
                        age < 18 -> "<18"
                        age < 30 -> "18-29"
                        age < 45 -> "30-44"
                        age < 60 -> "45-59"
                        else -> "60+"
                    }
                    ageGroups[ageGroup] = ageGroups.getOrDefault(ageGroup, 0) + 1

                    // Gender
                    genderCounts[gender] = genderCounts.getOrDefault(gender, 0) + 1

                    // Location
                    locationCounts[location] = locationCounts.getOrDefault(location, 0) + 1

                    savedHobbies.forEach { hobbyName ->
                        ageGroupHobbies.computeIfAbsent(ageGroup) { mutableMapOf() }
                            .merge(hobbyName, 1, Int::plus)
                    }

                    // Gender
                    savedHobbies.forEach { hobbyName ->
                        genderHobbies.computeIfAbsent(gender) { mutableMapOf() }
                            .merge(hobbyName, 1, Int::plus)
                    }

                    // Location
                    savedHobbies.forEach { hobbyName ->
                        locationHobbies.computeIfAbsent(location) { mutableMapOf() }
                            .merge(hobbyName, 1, Int::plus)
                    }
                }
                if (hobbiesByDemographics.isEmpty()) {
                    android.util.Log.w("UserDemographicsFragment", "No hobbies resolved for demographics.")
                }

                android.util.Log.d("UserDemographicsFragment", "Hobbies by Demographics: $hobbiesByDemographics")
                android.util.Log.d("UserDemographicsFragment", "User Hobbies Map: $userHobbiesMap")

                // Display Charts and Details
                displayBarChart(ageGroups, genderCounts, locationCounts)
                displayGroupedPieChart(pieChartAge, ageGroupHobbies, "Age Group Distribution")
                displayGroupedPieChart(pieChartGender, genderHobbies, "Gender Distribution")
                displayGroupedPieChart(pieChartLocation, locationHobbies, "Location Distribution")
                detailsAdapter.updateData(ageGroups, genderCounts, locationCounts)
            }
            .addOnFailureListener { e ->
                android.util.Log.e("UserDemographicsFragment", "Error fetching demographics", e)
            }
    }

    private fun displayBarChart(
        ageGroups: Map<String, Int>,
        genderCounts: Map<String, Int>,
        locationCounts: Map<String, Int>
    ) {
        val entries = mutableListOf<BarEntry>()
        val labels = mutableListOf<String>()
        val legendItems = mutableListOf<Pair<String, Int>>() // For legend labels and colors
        var index = 0

        // Add Age Groups
        ageGroups.forEach { (group, count) ->
            entries.add(BarEntry(index++.toFloat(), count.toFloat()))
            labels.add("Age: $group")
            legendItems.add(Pair("Age: $group", Color.BLUE)) // Add legend item with color
        }

        // Add Gender Counts
        genderCounts.forEach { (gender, count) ->
            entries.add(BarEntry(index++.toFloat(), count.toFloat()))
            labels.add("Gender: $gender")
            legendItems.add(Pair("Gender: $gender", Color.RED)) // Add legend item with color
        }

        // Add Location Counts
        locationCounts.forEach { (location, count) ->
            entries.add(BarEntry(index++.toFloat(), count.toFloat()))
            labels.add("Location: $location")
            legendItems.add(Pair("Location: $location", Color.GREEN)) // Add legend item with color
        }

        val barDataSet = BarDataSet(entries, "Age, Gender, and Location Distribution").apply {
            colors = listOf(
                Color.BLUE, Color.RED, Color.GREEN, Color.MAGENTA, Color.CYAN
            )
        }

        val data = BarData(barDataSet).apply {
            barWidth = 0.9f
        }

        barChart.data = data
        barChart.xAxis.apply {
            valueFormatter = IndexAxisValueFormatter(labels)
            granularity = 1f
            isGranularityEnabled = true
        }
        barChart.description.isEnabled = false
        barChart.invalidate()

        // Populate Legend
        populateLegendForBarChart(legendItems)
    }

    private fun populateLegendForBarChart(legendItems: List<Pair<String, Int>>) {
        binding.legendDemoLayout.removeAllViews() // Assuming `legendLayout` is a LinearLayout in your layout XML.

        legendItems.forEach { (label, color) ->
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
            binding.legendDemoLayout.addView(legendItem)
        }
    }

    private fun displayGroupedPieChart(pieChart: PieChart, groupedHobbies: Map<String, Map<String, Int>>, chartTitle: String) {
        val entries = mutableListOf<PieEntry>()
        groupedHobbies.forEach { (group, hobbies) ->
            hobbies.forEach { (hobby, count) ->
                entries.add(PieEntry(count.toFloat(), "$group: $hobby"))
            }
        }

        val pieDataSet = PieDataSet(entries, chartTitle)
        pieDataSet.colors = listOf(
            Color.BLUE, Color.RED, Color.GREEN, Color.MAGENTA, Color.CYAN
        )

        val data = PieData(pieDataSet)
        data.setValueFormatter(PercentFormatter())
        data.setValueTextSize(12f)

        pieChart.data = data
        pieChart.centerText = chartTitle
        pieChart.setCenterTextSize(16f)
        pieChart.setEntryLabelColor(Color.BLACK)
        pieChart.description.isEnabled = false
        pieChart.invalidate()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Ensure proper cleanup
    }
}
