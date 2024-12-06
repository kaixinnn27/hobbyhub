package com.example.hobbyhub.scheduling.view.ui

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.hobbyhub.R
import com.example.hobbyhub.databinding.FragmentCreateEventBinding
import com.example.hobbyhub.scheduling.model.Event
import com.example.hobbyhub.utility.EventReminderReceiver
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import java.text.SimpleDateFormat
import java.util.Locale

class CreateEventFragment : Fragment() {

    private var _binding: FragmentCreateEventBinding? = null
    private val binding get() = _binding!!

    private val auth by lazy { FirebaseAuth.getInstance() }
    private val db by lazy { FirebaseFirestore.getInstance() }

    private var selectedDate: String = ""
    private var selectedTime: String = ""
    private val busyTimes = mutableListOf<String>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCreateEventBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Setup UI components
        setupUI()
    }

    private fun setupUI() {
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
                Log.d("CreateEventFragment", "Selected date: $selectedDate")
                fetchParticipantsAvailability()
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

    private fun fetchParticipantsAvailability() {
        val participants = binding.multiAutocompleteParticipants.text.toString()
            .split(",").map { it.trim() }.filter { it.isNotEmpty() }

        if (participants.isEmpty()) {
            Log.e("CreateEventFragment", "No participants provided.")
            Toast.makeText(requireContext(), "Please add valid participants.", Toast.LENGTH_SHORT).show()
            return
        }

        Log.d("CreateEventFragment", "Fetching availability for participants: $participants")
        busyTimes.clear()

        // Fetch participant busy times for the selected date
        val tasks = participants.map { participant ->
            Log.d("CreateEventFragment", "Fetching schedule for participant: $participant")
            db.collection("schedule")
                .document(participant) // Check participant ID validity
                .collection("events")
                .whereEqualTo("date", selectedDate)
                .get()
        }

        com.google.android.gms.tasks.Tasks.whenAllSuccess<QuerySnapshot>(tasks)
            .addOnSuccessListener { snapshots ->
                for (snapshot in snapshots) {
                    for (doc in snapshot.documents) {
                        val time = doc.getString("time")
                        if (time != null) {
                            busyTimes.add(time)
                            Log.d("CreateEventFragment", "Added busy time: $time")
                        } else {
                            Log.w("CreateEventFragment", "Document missing 'time' field: ${doc.id}")
                        }
                    }
                }
                generateFreeSlots()
            }
            .addOnFailureListener { e ->
                Log.e("CreateEventFragment", "Error fetching participant availability: ${e.message}", e)
                Toast.makeText(requireContext(), "Failed to fetch availability.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun generateFreeSlots() {
        val workingHours = listOf(
            "09:00", "10:00", "11:00", "12:00", "13:00", "14:00", "15:00", "16:00", "17:00", "17:29"
        )
        val freeSlots = workingHours.filter { it !in busyTimes }

        Log.d("CreateEventFragment", "Busy times: $busyTimes")
        Log.d("CreateEventFragment", "Working hours: $workingHours")
        Log.d("CreateEventFragment", "Free slots: $freeSlots")

        if (freeSlots.isEmpty()) {
            binding.timeSlotScrollView.visibility = View.GONE
            Toast.makeText(requireContext(), "No free slots available.", Toast.LENGTH_SHORT).show()
        } else {
            binding.timeSlotScrollView.visibility = View.VISIBLE
            populateTimeSlots(workingHours, freeSlots)
        }
    }

    private fun populateTimeSlots(workingHours: List<String>, freeSlots: List<String>) {
        binding.llTimeSlotsContainer.removeAllViews()

        workingHours.forEach { time ->
            val timeSlotView = layoutInflater.inflate(R.layout.item_time_slot, binding.llTimeSlotsContainer, false)

            val timeText = timeSlotView.findViewById<TextView>(R.id.tvTimeSlot)
            timeText.text = time

            // Highlight busy times
            if (time in freeSlots) {
                timeSlotView.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.light_green))
            } else {
                timeSlotView.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.dark_gray))
            }

            // Handle user selection
            timeSlotView.setOnClickListener {
                if (time in freeSlots) {
                    selectedTime = time
                    Toast.makeText(requireContext(), "Selected time: $selectedTime", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "This time is busy.", Toast.LENGTH_SHORT).show()
                }
            }

            binding.llTimeSlotsContainer.addView(timeSlotView)
        }
    }


    private fun saveEventToFirestore() {
        val userId = auth.currentUser?.uid ?: return
        val eventId = binding.etEventId.text.toString().trim()

        val event = Event(
            date = selectedDate,
            eventId = eventId,
            time = selectedTime,
            location = binding.spinnerLocation.selectedItem.toString(),
            participants = binding.multiAutocompleteParticipants.text.toString()
                .split(",").map { it.trim() },
            reminderTime = selectedTime
        )

        Log.d("CreateEventFragment", "Saving event: $event")
        db.collection("schedule").document(userId).collection("events").document(eventId)
            .set(event)
            .addOnSuccessListener {
                Log.d("CreateEventFragment", "Event saved successfully.")
                setEventReminder(event)
                Toast.makeText(requireContext(), "Event created successfully!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Log.e("CreateEventFragment", "Failed to save event: ${e.message}", e)
                Toast.makeText(requireContext(), "Failed to save event.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun setEventReminder(event: Event) {
        val alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // Check if exact alarms can be scheduled
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S &&
            !alarmManager.canScheduleExactAlarms()
        ) {
            Toast.makeText(
                requireContext(),
                "Please enable exact alarm permissions in Settings.",
                Toast.LENGTH_LONG
            ).show()
            Log.e("CreateEventFragment", "Cannot schedule exact alarms.")
            return
        }

        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

        try {
            val eventDateTime = dateFormat.parse("${event.date} ${event.reminderTime}")
            val intent = Intent(requireContext(), EventReminderReceiver::class.java).apply {
                putExtra("eventTitle", "Event Reminder: ${event.eventId}")
                putExtra("eventTime", event.time)
            }

            val pendingIntent = PendingIntent.getBroadcast(
                requireContext(),
                event.eventId.hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            if (eventDateTime != null) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    eventDateTime.time,
                    pendingIntent
                )
            }
            Log.d("CreateEventFragment", "Reminder set for: ${event.date} ${event.reminderTime}")
        } catch (e: Exception) {
            Log.e("CreateEventFragment", "Failed to parse reminder time: ${e.message}")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        this._binding = null
    }
}
