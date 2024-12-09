package com.example.hobbyhub.scheduling.view.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.hobbyhub.R
import com.example.hobbyhub.authentication.viewmodel.AuthViewModel
import com.example.hobbyhub.databinding.FragmentScheduleBinding
import com.example.hobbyhub.scheduling.model.Event
import com.example.hobbyhub.scheduling.view.adapter.ScheduleAdapter
import com.example.hobbyhub.scheduling.viewmodel.ScheduleViewModel
import com.example.hobbyhub.utility.toBitmap
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

class ScheduleFragment : Fragment() {

    private lateinit var binding: FragmentScheduleBinding

    private val scheduleViewModel: ScheduleViewModel by activityViewModels()
    private val auth by lazy { FirebaseAuth.getInstance() }
    private val db by lazy { FirebaseFirestore.getInstance() }
    private val authViewModel: AuthViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentScheduleBinding.inflate(inflater, container, false)
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
        loadUserPhoto()

        // Add new event button
        binding.fabAddEvent.setOnClickListener {
            // Navigate to CreateEventFragment
            findNavController().navigate(R.id.createEventFragment)
        }

        binding.fabCalendar.setOnClickListener {
            findNavController().navigate(R.id.calendarFragment)
        }
    }

    private fun loadUserPhoto() {
        val userId = authViewModel.getCurrentUserId()

        if (!userId.isNullOrBlank()) {
            lifecycleScope.launch {
                val user = authViewModel.get(userId)
                user?.let {
                    if (user.photo.toBitmap() != null) {
                        binding.headerProfile.setImageBitmap(user.photo.toBitmap())
                        binding.letterOverlayTv.visibility = View.GONE
                    } else {
                        binding.headerProfile.setImageResource(R.drawable.profile_bg)
                        binding.letterOverlayTv.visibility = View.VISIBLE

                        val firstLetter = user.name.firstOrNull()?.toString()?.uppercase() ?: "U"
                        binding.letterOverlayTv.text = firstLetter
                    }
                }
            }
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
                        id = doc.id
                    }
                }
                scheduleViewModel.setEvents(events)
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    context,
                    "Failed to fetch schedule: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun navigateToEditEvent(event: Event) {
        val action = ScheduleFragmentDirections.actionScheduleFragmentToEditEventFragment(event)
        findNavController().navigate(action)
    }
}
