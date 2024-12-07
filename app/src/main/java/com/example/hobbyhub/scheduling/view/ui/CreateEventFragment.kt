package com.example.hobbyhub.scheduling.view.ui

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.hobbyhub.R
import com.example.hobbyhub.databinding.FragmentCreateEventBinding
import com.example.hobbyhub.scheduling.model.Event
import com.example.hobbyhub.utility.EventReminderReceiver
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class CreateEventFragment : Fragment() {

    private var _binding: FragmentCreateEventBinding? = null
    private val binding get() = _binding!!

    private val auth by lazy { FirebaseAuth.getInstance() }
    private val db by lazy { FirebaseFirestore.getInstance() }

    private var selectedDate: String = ""
    private var selectedStartTime: String = ""
    private var selectedEndTime: String = ""
    private val busyTimes = mutableListOf<String>()
    private val friendsMap = mutableMapOf<String, String>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCreateEventBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
        fetchFriends()
    }

    private fun setupUI() {
        val locations = listOf("Room A", "Room B", "Room C")
        binding.btnStartTimePicker.setOnClickListener {
            showTimePicker { selectedTime ->
                selectedStartTime = selectedTime
                updateSelectedTimeDisplay()
            }
        }

        binding.btnEndTimePicker.setOnClickListener {
            showTimePicker { selectedTime ->
                selectedEndTime = selectedTime
                updateSelectedTimeDisplay()
            }
        }

        // Location Spinner
        binding.spinnerLocation.adapter =
            ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, locations)

        // Date Picker
        binding.btnDatePicker.setOnClickListener {
            val datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Select Date")
                .build()
            datePicker.show(parentFragmentManager, "DATE_PICKER")
            datePicker.addOnPositiveButtonClickListener { selection ->
                selectedDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(selection)
                binding.btnDatePicker.text = selectedDate
                fetchParticipantsAvailability() // Fetch availability after date is selected
            }
        }

        binding.btnSaveEvent.setOnClickListener {
            if (validateInputs()) {
                saveEventToFirestore()
            }
        }
    }

    private fun showTimePicker(onTimeSelected: (String) -> Unit) {
        val timePicker = MaterialTimePicker.Builder()
            .setTimeFormat(TimeFormat.CLOCK_24H)
            .setTitleText("Select Time")
            .build()
        timePicker.show(parentFragmentManager, "TIME_PICKER")
        timePicker.addOnPositiveButtonClickListener {
            val time = String.format("%02d:%02d", timePicker.hour, timePicker.minute)
            onTimeSelected(time)
        }
    }

    private fun updateSelectedTimeDisplay() {
        binding.tvSelectedTimeRange.text = when {
            selectedStartTime.isNotEmpty() && selectedEndTime.isNotEmpty() -> "Selected Time: $selectedStartTime to $selectedEndTime"
            selectedStartTime.isNotEmpty() -> "Start Time: $selectedStartTime"
            selectedEndTime.isNotEmpty() -> "End Time: $selectedEndTime"
            else -> "Selected Time: Not Set"
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
        if (selectedStartTime.isEmpty() || selectedEndTime.isEmpty()) {
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

    private fun generateBusySlots() {
        val unavailableText = binding.tvUnavailableTimes
        val recyclerView = binding.recyclerTimeSlots

        if (busyTimes.isEmpty()) {
            unavailableText.visibility = View.GONE
            recyclerView.visibility = View.GONE
            Toast.makeText(requireContext(), "No busy slots available.", Toast.LENGTH_SHORT).show()
        } else {
            unavailableText.visibility = View.VISIBLE
            recyclerView.visibility = View.VISIBLE

            // Use RecyclerView Adapter
            val adapter = TimeSlotAdapter(requireContext(), busyTimes)
            recyclerView.layoutManager = LinearLayoutManager(requireContext())
            recyclerView.adapter = adapter
        }
    }

    private fun addBusyTimesToRange(startTime: String, endTime: String) {
        val range = "$startTime - $endTime"
        if (!busyTimes.contains(range)) {
            busyTimes.add(range)
        }
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
        val tasks = participants.map { participantUsername ->
            val participantId = friendsMap[participantUsername] ?: return@map null
            Log.d("CreateEventFragment", "Fetching schedule for participant: $participantUsername ($participantId)")
            db.collection("schedule")
                .document(participantId) // Check participant ID validity
                .collection("events")
                .whereEqualTo("date", selectedDate)
                .get()
        }.filterNotNull()

        com.google.android.gms.tasks.Tasks.whenAllSuccess<QuerySnapshot>(tasks)
            .addOnSuccessListener { snapshots ->
                for (snapshot in snapshots) {
                    for (doc in snapshot.documents) {
                        val startTime = doc.getString("startTime")
                        val endTime = doc.getString("endTime")
                        if (startTime != null && endTime != null) {
                            // Add the range to busyTimes
                            addBusyTimesToRange(startTime, endTime)
                        } else {
                            Log.w("CreateEventFragment", "Document missing 'startTime' or 'endTime' field: ${doc.id}")
                        }
                    }
                }
                generateBusySlots() // Generate busy slots to be displayed
            }
            .addOnFailureListener { e ->
                Log.e("CreateEventFragment", "Error fetching participant availability: ${e.message}", e)
                Toast.makeText(requireContext(), "Failed to fetch availability.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun fetchFriends() {
        val currentUser = auth.currentUser?.uid ?: return
        Log.d("CreateEventFragment", "Current user ID: $currentUser")
        db.collection("user").document(currentUser).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val friendIds = document["friends"] as? List<String>
                    if (friendIds != null) {
                        Log.e("CreateEventFragment", "Friends: $friendIds")
                        fetchFriendDetails(friendIds)
                    } else {
                        Log.e("CreateEventFragment", "No 'friends' field in user document.")
                    }
                } else {
                    Log.e("CreateEventFragment", "User document does not exist.")
                }
            }
            .addOnFailureListener { e ->
                Log.e("CreateEventFragment", "Error fetching friends: ${e.message}")
            }
    }

    private fun fetchFriendDetails(friendIds: List<String>) {
        val usernames = mutableListOf<String>()
        friendIds.forEach { userId ->
            db.collection("user").document(userId).get()
                .addOnSuccessListener { document ->
                    val username = document["name"] as? String ?: return@addOnSuccessListener
                    usernames.add(username)
                    friendsMap[username] = userId
                    Log.d("CreateEventFragment", "Friend fetched: $username -> $userId")
                    updateParticipantAutoComplete(usernames)
                }
                .addOnFailureListener { e ->
                    Log.e("CreateEventFragment", "Error fetching friend details: ${e.message}")
                }
        }
    }

    private fun updateParticipantAutoComplete(usernames: List<String>) {
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, usernames)
        binding.multiAutocompleteParticipants.setAdapter(adapter)
        binding.multiAutocompleteParticipants.setTokenizer(android.widget.MultiAutoCompleteTextView.CommaTokenizer())
    }

    private fun saveEventToFirestore() {
        val userId = auth.currentUser?.uid ?: return
        val eventId = binding.etEventId.text.toString().trim()

        val participantUsernames = binding.multiAutocompleteParticipants.text.toString()
            .split(",").map { it.trim() }
        val participantIds = participantUsernames.mapNotNull { friendsMap[it] }
        Log.d("CreateEventFragment", "Saving Event for Participants: $participantIds")

        val event = Event(
            date = selectedDate,
            eventId = eventId,
            startTime = selectedStartTime,
            endTime = selectedEndTime,
            location = binding.spinnerLocation.selectedItem.toString(),
            participants = participantUsernames,
            reminderTime = selectedStartTime
        )

        db.collection("schedule").document(userId).collection("events").document(eventId)
            .set(event)
            .addOnSuccessListener {
                Log.d("CreateEventFragment", "Event saved successfully.")
                sendInvitations(event, participantIds)
                setEventReminder(event)
                Toast.makeText(requireContext(), "Event created successfully!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Log.e("CreateEventFragment", "Failed to save event: ${e.message}")
                Toast.makeText(requireContext(), "Failed to save event.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun sendInvitations(event: Event, participantIds: List<String>) {
        val currentUser = auth.currentUser ?: return
        participantIds.forEach { participantId ->
            val chatRoomId = if (currentUser.uid < participantId) {
                "${currentUser.uid}_$participantId"
            } else {
                "${participantId}_${currentUser.uid}"
            }

            val invitationMessage = mapOf(
                "type" to "event_invitation",
                "eventId" to event.eventId,
                "eventDate" to event.date,
                "eventStartTime" to event.startTime,
                "eventEndTime" to event.endTime,
                "senderId" to currentUser.uid,
                "timestamp" to System.currentTimeMillis()
            )

            db.collection("chats").document(chatRoomId).collection("messages")
                .add(invitationMessage)
                .addOnSuccessListener {
                    Log.d("CreateEventFragment", "Invitation sent to $participantId")
                }
                .addOnFailureListener { e ->
                    Log.e("CreateEventFragment", "Failed to send invitation: ${e.message}")
                }
        }
    }

    private fun setEventReminder(event: Event) {
        val alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager

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
                putExtra("eventTime", event.startTime)
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
        _binding = null
    }
}
