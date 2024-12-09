package com.example.hobbyhub.report.view.ui

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.hobbyhub.R
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

    private val db = FirebaseFirestore.getInstance()
    private lateinit var barChart: BarChart
    private lateinit var pieChart: PieChart
    private lateinit var detailsAdapter: DemographicDetailsAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_user_demographics_report, container, false)

        barChart = view.findViewById(R.id.barChartDemographics)
        pieChart = view.findViewById(R.id.pieChartHobbies)

        setupRecyclerView(view)
        fetchHobbiesAndDemographics()

        return view
    }

    private fun setupRecyclerView(view: View) {
        val recyclerView = view.findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.rvDemographicsDetails)
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

                // Pass both user hobbies and hobbyMap to process demographics
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

                for (document in userDocuments) {
                    val userId = document.id
                    val age = document.getLong("age") ?: 0
                    val gender = document.getString("gender") ?: "Unknown"
                    val location = document.getString("location") ?: "Unknown"
                    val savedHobbies = userHobbiesMap[userId] ?: emptyList()

                    // Resolve hobby IDs to their names
                    val resolvedHobbies = savedHobbies.mapNotNull { hobbyId ->
                        val hobbyName = hobbyMap[hobbyId]
                        if (hobbyName == null) {
                            android.util.Log.w("UserDemographicsFragment", "Unresolved hobby ID: $hobbyId")
                        }
                        hobbyName
                    }

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

                    // Hobbies
                    resolvedHobbies.forEach { hobbyName ->
                        hobbiesByDemographics[hobbyName] = hobbiesByDemographics.getOrDefault(hobbyName, 0) + 1
                    }
                }

                android.util.Log.d("UserDemographicsFragment", "Hobbies by Demographics: $hobbiesByDemographics")
                android.util.Log.d("UserDemographicsFragment", "User Hobbies Map: $userHobbiesMap")

                android.util.Log.d("UserDemographicsFragment", "Hobbies by Demographics: $hobbiesByDemographics")

                // Display Charts and Details
                displayBarChart(ageGroups, genderCounts, locationCounts)
                displayPieChart(hobbiesByDemographics)
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
        var index = 0

        // Add Age Groups
        ageGroups.forEach { (group, count) ->
            entries.add(BarEntry(index++.toFloat(), count.toFloat()))
            labels.add("Age: $group")
        }

        // Add Gender Counts
        genderCounts.forEach { (gender, count) ->
            entries.add(BarEntry(index++.toFloat(), count.toFloat()))
            labels.add("Gender: $gender")
        }

        // Add Location Counts
        locationCounts.forEach { (location, count) ->
            entries.add(BarEntry(index++.toFloat(), count.toFloat()))
            labels.add("Location: $location")
        }

        val barDataSet = BarDataSet(entries, "Age, Gender, and Location Distribution")
        barDataSet.colors = listOf(
            Color.BLUE, Color.RED, Color.GREEN, Color.MAGENTA, Color.CYAN
        )

        val data = BarData(barDataSet)
        data.barWidth = 0.9f

        barChart.data = data
        barChart.xAxis.valueFormatter = IndexAxisValueFormatter(labels)
        barChart.xAxis.granularity = 1f
        barChart.xAxis.isGranularityEnabled = true
        barChart.description.isEnabled = false
        barChart.invalidate()
    }

    private fun displayPieChart(hobbiesByDemographics: Map<String, Int>) {
        val entries = hobbiesByDemographics.map { (hobby, count) ->
            PieEntry(count.toFloat(), hobby)
        }

        val pieDataSet = PieDataSet(entries, "Hobby Distribution")
        pieDataSet.colors = listOf(
            Color.BLUE, Color.RED, Color.GREEN, Color.MAGENTA, Color.CYAN
        )

        val data = PieData(pieDataSet)
        data.setValueFormatter(PercentFormatter())
        data.setValueTextSize(12f)

        pieChart.data = data
        pieChart.description.isEnabled = false
        pieChart.invalidate()
    }
}
