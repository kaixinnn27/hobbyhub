package com.example.hobbyhub.scheduling.view.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.hobbyhub.R
import com.example.hobbyhub.databinding.FragmentScheduleBinding
import com.example.hobbyhub.scheduling.model.Event
import com.example.hobbyhub.scheduling.view.adapter.ScheduleAdapter
import com.example.hobbyhub.scheduling.viewmodel.ScheduleViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ScheduleFragment : Fragment() {

    private var _binding: FragmentScheduleBinding? = null
    private val binding get() = _binding!!

    private val scheduleViewModel: ScheduleViewModel by activityViewModels()
    private val auth by lazy { FirebaseAuth.getInstance() }
    private val db by lazy { FirebaseFirestore.getInstance() }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentScheduleBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set up RecyclerView with item click handling
        val adapter = ScheduleAdapter { event ->
            navigateToEditEvent(event) // Handle item clicks
        }

        binding.rvSchedule.layoutManager = LinearLayoutManager(requireContext())
        binding.rvSchedule.adapter = adapter

        // Observe LiveData from ViewModel
        scheduleViewModel.events.observe(viewLifecycleOwner) { events ->
            adapter.submitList(events)
        }

        // Fetch schedule data
        fetchUserSchedule()

        // Add new event button
        binding.btnAddEvent.setOnClickListener {
            // Navigate to CreateEventFragment
            findNavController().navigate(R.id.createEventFragment)
        }
    }

    private fun fetchUserSchedule() {
        val userId = auth.currentUser?.uid ?: return
        db.collection("schedule").document(userId).collection("events")
            .get()
            .addOnSuccessListener { documents ->
                val events = documents.mapNotNull { doc ->
                    val event = doc.toObject(Event::class.java)
                    event.apply {
                        eventId = doc.id // Assign the document ID to the eventId field
                    }
                }
                scheduleViewModel.setEvents(events)
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Failed to fetch schedule: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun navigateToEditEvent(event: Event) {
        val action = ScheduleFragmentDirections.actionScheduleFragmentToEditEventFragment(event)
        findNavController().navigate(action)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
