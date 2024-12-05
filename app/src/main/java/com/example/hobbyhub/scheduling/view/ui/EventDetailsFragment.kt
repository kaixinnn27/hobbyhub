package com.example.hobbyhub.scheduling.view.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.hobbyhub.databinding.FragmentEventDetailsBinding
import com.example.hobbyhub.scheduling.model.Event
import com.example.hobbyhub.scheduling.viewmodel.ScheduleViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class EventDetailsFragment : Fragment() {

    private var _binding: FragmentEventDetailsBinding? = null
    private val binding get() = _binding!!

    private val args: EventDetailsFragmentArgs by navArgs()
    private val scheduleViewModel: ScheduleViewModel by activityViewModels()
    private val db by lazy { FirebaseFirestore.getInstance() }
    private val auth by lazy { FirebaseAuth.getInstance() }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEventDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val event = args.event

        // Display Event Details
        binding.tvDate.text = "Date: ${event.date}"
        binding.tvTime.text = "Time: ${event.time}"
        binding.tvLocation.text = "Location: ${event.location}"
        binding.tvParticipants.text = "Participants: ${event.participants.joinToString(", ")}"

        // Edit Event Button
        binding.btnEditEvent.setOnClickListener {
            val action = EventDetailsFragmentDirections.actionEventDetailsFragmentToEditEventFragment(event)
            findNavController().navigate(action)
        }

        // Delete Event Button
        binding.btnDeleteEvent.setOnClickListener {
            deleteEvent(event)
        }
    }

    private fun deleteEvent(event: Event) {
        val userId = auth.currentUser?.uid ?: return
        db.collection("schedule").document(userId)
            .collection("events").whereEqualTo("date", event.date).whereEqualTo("time", event.time)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    document.reference.delete()
                }
                Toast.makeText(requireContext(), "Event deleted successfully.", Toast.LENGTH_SHORT).show()
                scheduleViewModel.removeEvent(event)
                findNavController().popBackStack()
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Failed to delete event: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
