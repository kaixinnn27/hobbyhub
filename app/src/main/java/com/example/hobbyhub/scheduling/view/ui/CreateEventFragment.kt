package com.example.hobbyhub.scheduling.view.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.hobbyhub.databinding.FragmentCreateEventBinding
import com.example.hobbyhub.scheduling.model.Event
import com.example.hobbyhub.scheduling.viewmodel.ScheduleViewModel
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Locale

class CreateEventFragment : Fragment() {

    private var _binding: FragmentCreateEventBinding? = null
    private val binding get() = _binding!!

    private val scheduleViewModel: ScheduleViewModel by activityViewModels()
    private val auth by lazy { FirebaseAuth.getInstance() }
    private val db by lazy { FirebaseFirestore.getInstance() }

    private var selectedDate: String = ""
    private var selectedTime: String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCreateEventBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val locations = listOf("Room A", "Room B", "Room C")
        val participants = listOf("user1@example.com", "user2@example.com", "user3@example.com")

        // Location Spinner
        binding.spinnerLocation.adapter =
            ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, locations)

        // Participants Input
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, participants)
        binding.multiAutocompleteParticipants.setAdapter(adapter)
        binding.multiAutocompleteParticipants.setTokenizer(android.widget.MultiAutoCompleteTextView.CommaTokenizer())

        // Date Picker
        binding.btnDatePicker.setOnClickListener {
            val datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Select Date")
                .build()
            datePicker.show(parentFragmentManager, "DATE_PICKER")
            datePicker.addOnPositiveButtonClickListener { selection ->
                selectedDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(selection)
                binding.btnDatePicker.text = selectedDate
            }
        }

        // Time Picker
        binding.btnTimePicker.setOnClickListener {
            val timePicker = MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_24H)
                .setTitleText("Select Time")
                .build()
            timePicker.show(parentFragmentManager, "TIME_PICKER")
            timePicker.addOnPositiveButtonClickListener {
                selectedTime = String.format("%02d:%02d", timePicker.hour, timePicker.minute)
                binding.btnTimePicker.text = selectedTime
            }
        }

        // Save Button
        binding.btnSaveEvent.setOnClickListener {
            if (validateInputs()) {
                saveEventToFirestore()
            }
        }
    }

    private fun validateInputs(): Boolean {
        if (binding.etEventId.text.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "Please enter an Event ID.", Toast.LENGTH_SHORT).show()
            return false
        }
        if (selectedDate.isEmpty()) {
            Toast.makeText(requireContext(), "Please select a date.", Toast.LENGTH_SHORT).show()
            return false
        }
        if (selectedTime.isEmpty()) {
            Toast.makeText(requireContext(), "Please select a time.", Toast.LENGTH_SHORT).show()
            return false
        }
        if (binding.spinnerLocation.selectedItem == null) {
            Toast.makeText(requireContext(), "Please select a location.", Toast.LENGTH_SHORT).show()
            return false
        }
        if (binding.multiAutocompleteParticipants.text.isEmpty()) {
            Toast.makeText(requireContext(), "Please add participants.", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    private fun saveEventToFirestore() {
        val userId = auth.currentUser?.uid ?: return
        val eventId = binding.etEventId.text.toString().trim() // Use user-provided event ID

        val event = Event(
            date = selectedDate,
            time = selectedTime,
            location = binding.spinnerLocation.selectedItem.toString(),
            participants = binding.multiAutocompleteParticipants.text.toString().split(",").map { it.trim() }
        )

        db.collection("schedule").document(userId).collection("events").document(eventId)
            .set(event)
            .addOnSuccessListener {
                scheduleViewModel.addEvent(event) // Update ViewModel
                Toast.makeText(requireContext(), "Event created successfully!", Toast.LENGTH_SHORT).show()
                parentFragmentManager.popBackStack() // Navigate back
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Failed to save event: ${e.message}", Toast.LENGTH_SHORT).show()
            }

        val documentPath = "schedule/$userId/events/$eventId"
        println("Firestore Path: $documentPath")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
