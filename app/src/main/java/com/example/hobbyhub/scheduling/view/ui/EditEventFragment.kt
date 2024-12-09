package com.example.hobbyhub.scheduling.view.ui

import android.annotation.SuppressLint
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
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.hobbyhub.R
import com.example.hobbyhub.R.*
import com.example.hobbyhub.databinding.FragmentEditEventBinding
import com.example.hobbyhub.scheduling.model.Event
import com.example.hobbyhub.utility.EventReminderReceiver
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class EditEventFragment : Fragment() {

    private var _binding: FragmentEditEventBinding? = null
    private val binding get() = _binding!!
    private val auth by lazy { FirebaseAuth.getInstance() }
    private val db by lazy { FirebaseFirestore.getInstance() }
    private val args: EditEventFragmentArgs by navArgs()
    private var selectedDate: String = ""
    private var selectedStartTime: String = ""
    private var selectedEndTime: String = ""
    private val busyTimes = mutableListOf<String>()
    private val friendsMap = mutableMapOf<String, String>()
    private val nav by lazy { findNavController() }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditEventBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val eventId = args.event.id
        fetchEventData(eventId)
        fetchFriends()
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

    @SuppressLint("DefaultLocale")
    private fun populateFields(event: Event) {
        binding.etEventId.setText(event.id)
        binding.etEventName.setText(event.name)
        selectedDate = event.date
        binding.btnDatePicker.text = event.date
        binding.btnStartTimePicker.text = event.startTime
        binding.btnEndTimePicker.text = event.endTime
        updateSelectedTimeDisplay()

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

        binding.btnStartTimePicker.setOnClickListener {
            val timeParts = event.startTime.split(":").map { it.toInt() }
            val timePicker = MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_24H)
                .setHour(timeParts[0])
                .setMinute(timeParts[1])
                .setTitleText("Select Time")
                .build()
            timePicker.show(parentFragmentManager, "TIME_PICKER")
            timePicker.addOnPositiveButtonClickListener {
                selectedStartTime = String.format("%02d:%02d", timePicker.hour, timePicker.minute)
                binding.btnStartTimePicker.text = selectedStartTime
                updateSelectedTimeDisplay()
            }
        }

        binding.btnEndTimePicker.setOnClickListener {
            val timeParts = event.endTime.split(":").map { it.toInt() }
            val timePicker = MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_24H)
                .setHour(timeParts[0])
                .setMinute(timeParts[1])
                .setTitleText("Select Time")
                .build()
            timePicker.show(parentFragmentManager, "TIME_PICKER")
            timePicker.addOnPositiveButtonClickListener {
                selectedEndTime = String.format("%02d:%02d", timePicker.hour, timePicker.minute)
                binding.btnEndTimePicker.text = selectedEndTime
                updateSelectedTimeDisplay()
            }
        }

        val locations = listOf("Room A", "Room B", "Room C")
        val locationAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, locations)
        locationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerLocation.adapter = locationAdapter
        binding.spinnerLocation.setSelection(getLocationIndex(event.location))

        binding.btnSaveChanges.setOnClickListener {
            if (validateInputs()) {
                saveUpdatedEvent()
            }
        }
        binding.btnDeleteEvent.setOnClickListener { deleteEvent() }
        binding.recyclerTimeSlots.layoutManager = LinearLayoutManager(requireContext())

        val participantsAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, event.participants)
        binding.multiAutocompleteParticipants.setAdapter(participantsAdapter)
        binding.multiAutocompleteParticipants.setText(event.participants.joinToString(", "))
        fetchParticipantsAvailability()
    }

    private fun getLocationIndex(location: String): Int {
        val locations = listOf("Room A", "Room B", "Room C")
        return locations.indexOf(location).takeIf { it >= 0 } ?: 0
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

    private fun saveUpdatedEvent() {
        val userId = auth.currentUser?.uid ?: return
        val eventId = binding.etEventId.text.toString().trim()
        val eventName = binding.etEventName.text.toString().trim()

        val participantUsernames = binding.multiAutocompleteParticipants.text.toString()
            .split(",").map { it.trim() }
        val participantIds = participantUsernames.mapNotNull { friendsMap[it] }
        Log.d("CreateEventFragment", "Saving Event for Participants: $participantIds")

        val event = Event(
            date = selectedDate,
            name = eventName,
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
                setEventReminders(event)
                Toast.makeText(requireContext(), "Event created successfully!", Toast.LENGTH_SHORT).show()
                nav.navigate(R.id.navigation_schedule)
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
                "eventId" to event.id,
                "name" to event.name,
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

    private fun setEventReminders(event: Event) {
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
            val eventDateTime = dateFormat.parse("${event.date} ${event.startTime}")
            if (eventDateTime != null) {
                scheduleReminder(
                    alarmManager,
                    eventDateTime.time - (60 * 60 * 1000),
                    "Reminder: ${event.name} starts in 1 hour!",
                    event.id.hashCode() + 1
                )
                scheduleReminder(
                    alarmManager,
                    eventDateTime.time - (30 * 60 * 1000),
                    "Reminder: ${event.name} starts in 30 minutes!",
                    event.id.hashCode() + 2
                )
            }
        } catch (e: Exception) {
            Log.e("EditEventFragment", "Failed to set reminders: ${e.message}")
        }
    }

    private fun scheduleReminder(
        alarmManager: AlarmManager,
        triggerAtMillis: Long,
        message: String,
        requestCode: Int
    ) {
        val intent = Intent(requireContext(), EventReminderReceiver::class.java).apply {
            putExtra("eventTitle", message)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            requireContext(),
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
        Log.d("EditEventFragment", "Scheduled reminder: $message at ${Date(triggerAtMillis)}")
    }

    private fun deleteEvent() {
        val userId = auth.currentUser?.uid ?: return
        val eventId = binding.etEventId.text.toString()
        db.collection("schedule").document(userId).collection("events").document(eventId)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Event deleted successfully.", Toast.LENGTH_SHORT).show()
                activity?.onBackPressed()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to delete event.", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
