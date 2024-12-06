package com.example.hobbyhub.report.view.ui

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.hobbyhub.R
import com.example.hobbyhub.databinding.FragmentReportBinding
import com.example.hobbyhub.utility.EventReminderReceiver

class ReportModuleFragment : Fragment() {

    private var _binding: FragmentReportBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentReportBinding.inflate(inflater, container, false)
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val testIntent = Intent(requireContext(), EventReminderReceiver::class.java).apply {
            putExtra("eventTitle", "Test Event")
            putExtra("eventTime", "10:00 AM")
        }
        val testPendingIntent = PendingIntent.getBroadcast(
            requireContext(),
            0,
            testIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // For Android 12+ (API level 31+)
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    System.currentTimeMillis() + 5000,
                    testPendingIntent
                )
            } else {
                // Notify the user about missing permission
                Toast.makeText(
                    requireContext(),
                    "Exact alarms are not allowed. Please enable the permission in settings.",
                    Toast.LENGTH_LONG
                ).show()
            }
        } else {
            // For Android 11 and below
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                System.currentTimeMillis() + 5000,
                testPendingIntent
            )
        }

        setupButtonListeners()
    }


    private fun setupButtonListeners() {
        binding.btnUserActivityReport.setOnClickListener {
            findNavController().navigate(R.id.action_reportMainFragment_to_userActivityReportFragment)
        }

        binding.btnHobbyPopularityReport.setOnClickListener {
            findNavController().navigate(R.id.action_reportMainFragment_to_hobbyPopularityReportFragment)
        }

        binding.btnUserDemographicsReport.setOnClickListener {
            findNavController().navigate(R.id.action_reportMainFragment_to_userDemographicsReportFragment)
        }

        binding.btnUserFeedbackAnalysis.setOnClickListener {
            findNavController().navigate(R.id.action_reportMainFragment_to_userFeedbackAnalysisFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}