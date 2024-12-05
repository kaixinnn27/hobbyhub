package com.example.hobbyhub

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.example.hobbyhub.databinding.ActivityMainBinding
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
}
