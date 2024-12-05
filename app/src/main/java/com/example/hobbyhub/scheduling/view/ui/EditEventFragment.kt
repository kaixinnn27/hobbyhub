package com.example.hobbyhub.scheduling.view.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.hobbyhub.databinding.FragmentEditEventBinding
import com.example.hobbyhub.scheduling.model.Event
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Locale

class EditEventFragment : Fragment() {

    private var _binding: FragmentEditEventBinding? = null
    private val binding get() = _binding!!
    private val args: EditEventFragmentArgs by navArgs()
    private val auth by lazy { FirebaseAuth.getInstance() }
    private val db by lazy { FirebaseFirestore.getInstance() }
    private var selectedDate: String = ""
    private var selectedTime: String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditEventBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val eventId = args.event.eventId
        fetchEventData(eventId)

        binding.btnSaveChanges.setOnClickListener {
            saveUpdatedEvent(eventId)
        }

        binding.btnDeleteEvent.setOnClickListener {
            deleteEvent(eventId)
        }
    }

    private fun fetchEventData(eventId: String?) {
        if (eventId.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "Invalid Event ID.", Toast.LENGTH_SHORT).show()
            findNavController().popBackStack()
            return
        }

        val userId = auth.currentUser?.uid ?: return
        db.collection("schedule").document(userId).collection("events").document(eventId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val event = document.toObject(Event::class.java)
                    event?.let {
                        populateFields(it)
                    }
                } else {
                    Toast.makeText(requireContext(), "Event not found.", Toast.LENGTH_SHORT).show()
                    findNavController().popBackStack()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Failed to fetch event: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }


    private fun populateFields(event: Event) {
        binding.etEventId.setText(event.eventId)
        selectedDate = event.date
        selectedTime = event.time
        binding.btnDatePicker.text = event.date
        binding.btnTimePicker.text = event.time

        binding.btnDatePicker.setOnClickListener {
            val datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Select Date")
                .setSelection(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(event.date)?.time)
                .build()
            datePicker.show(parentFragmentManager, "DATE_PICKER")
            datePicker.addOnPositiveButtonClickListener { selection ->
                selectedDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(selection)
                binding.btnDatePicker.text = selectedDate
            }
        }
        binding.btnTimePicker.setOnClickListener {
            val timeParts = event.time.split(":").map { it.toInt() }
            val timePicker = MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_24H)
                .setHour(timeParts[0])
                .setMinute(timeParts[1])
                .setTitleText("Select Time")
                .build()
            timePicker.show(parentFragmentManager, "TIME_PICKER")
            timePicker.addOnPositiveButtonClickListener {
                selectedTime = String.format("%02d:%02d", timePicker.hour, timePicker.minute)
                binding.btnTimePicker.text = selectedTime
            }
        }

        val locations = listOf("Room A", "Room B", "Room C")
        val locationAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, locations)
        locationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerLocation.adapter = locationAdapter
        binding.spinnerLocation.setSelection(getLocationIndex(event.location))

        val participantsAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, event.participants)
        binding.multiAutocompleteParticipants.setAdapter(participantsAdapter)
        binding.multiAutocompleteParticipants.setText(event.participants.joinToString(", "))
    }

    private fun getLocationIndex(location: String): Int {
        val locations = listOf("Room A", "Room B", "Room C")
        return locations.indexOf(location).takeIf { it >= 0 } ?: 0
    }

    private fun saveUpdatedEvent(eventId: String) {
        val userId = auth.currentUser?.uid ?: return

        val date = binding.btnDatePicker.text.toString()
        val time = binding.btnTimePicker.text.toString()
        val location = binding.spinnerLocation.selectedItem?.toString() ?: ""
        val participantsText = binding.multiAutocompleteParticipants.text.toString()
        val participants = participantsText.split(",").map { it.trim() }.filter { it.isNotEmpty() }

        if (date.isEmpty() || time.isEmpty() || location.isEmpty() || participants.isEmpty()) {
            Toast.makeText(requireContext(), "Please fill out all fields.", Toast.LENGTH_SHORT).show()
            return
        }

        val updatedEvent = Event(
            eventId = eventId,
            date = date,
            time = time,
            location = location,
            participants = participants
        )

        // Use user-provided eventId
        db.collection("schedule").document(userId).collection("events").document(eventId)
            .set(updatedEvent)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Changes saved successfully!", Toast.LENGTH_SHORT).show()
                findNavController().popBackStack()
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Failed to save changes: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }


    private fun deleteEvent(eventId: String) {
        val userId = auth.currentUser?.uid ?: return

        db.collection("schedule").document(userId).collection("events").document(eventId)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Event deleted successfully!", Toast.LENGTH_SHORT).show()
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
