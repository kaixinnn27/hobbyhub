package com.example.hobbyhub.report.view.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.hobbyhub.R
import com.example.hobbyhub.databinding.FragmentReportBinding

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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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