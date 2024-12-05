package com.example.hobbyhub.scheduling.view.ui

import EventDecorator
import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.hobbyhub.R
import com.example.hobbyhub.databinding.ItemCalendarBinding
import com.example.hobbyhub.scheduling.model.Event
import com.example.hobbyhub.scheduling.view.adapter.EventAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.prolificinteractive.materialcalendarview.CalendarDay

class CalendarFragment : Fragment() {

    private var _binding: ItemCalendarBinding? = null
    private val binding get() = _binding!!
    private val db by lazy { FirebaseFirestore.getInstance() }
    private val auth by lazy { FirebaseAuth.getInstance() }
    private val eventDates = mutableSetOf<CalendarDay>() // Store dates with events
    private var eventsForSelectedDate = emptyList<Event>() // Store events for the selected date

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = ItemCalendarBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fetchEvent()
        eventDates.forEach { Log.d("CalendarFragment", "Event Date: $it") }

        binding.materialCalendarView.setOnDateChangedListener { _, date, _ ->
            fetchEventsForSelectedDate(date)
        }
    }

    private fun fetchEvent() {
        val userId = auth.currentUser?.uid ?: return
        db.collection("schedule").document(userId).collection("events")
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val date = document.getString("date") ?: continue
                    Log.d("CalendarFragment", "Fetched Date: $date") // Log fetched date
                    val parts = date.split("-").map { it.toInt() }
                    if (parts.size == 3) {
                        val day = CalendarDay.from(parts[0], parts[1], parts[2])
                        eventDates.add(day)
                        Log.d("CalendarFragment", "Parsed CalendarDay: $day") // Log parsed date
                    } else {
                        Log.e("CalendarFragment", "Invalid date format: $date") // Log invalid format
                    }
                }
                val backgroundDrawable =
                    ContextCompat.getDrawable(requireContext(), R.drawable.event_background)!!
                val decorator = EventDecorator(eventDates, Color.WHITE, backgroundDrawable)
                binding.materialCalendarView.addDecorator(decorator)
                Log.d("CalendarFragment", "Event Dates Decorated: $eventDates") // Log decorated dates
            }
            .addOnFailureListener { e ->
                e.printStackTrace()
                Log.e("CalendarFragment", "Error fetching events", e) // Log error
            }
    }

    @SuppressLint("DefaultLocale")
    private fun fetchEventsForSelectedDate(date: CalendarDay) {
        val selectedDate = String.format("%04d-%02d-%02d", date.year, date.month, date.day)
        Log.d("CalendarFragment", "Selected Date: $selectedDate") // Log selected date
        val userId = auth.currentUser?.uid ?: return

        db.collection("schedule").document(userId).collection("events")
            .whereEqualTo("date", selectedDate)
            .get()
            .addOnSuccessListener { documents ->
                eventsForSelectedDate = documents.map { it.toObject(Event::class.java) }
                Log.d("CalendarFragment", "Fetched Events for $selectedDate: $eventsForSelectedDate") // Log fetched events
                displayEventsSelection(eventsForSelectedDate)
            }
            .addOnFailureListener { e ->
                e.printStackTrace()
                Log.e("CalendarFragment", "Error fetching events for $selectedDate", e) // Log error
            }
    }

    private fun displayEventsSelection(events: List<Event>) {
        Log.d("CalendarFragment", "Displaying Events: $events")
        val adapter = EventAdapter(events) { selectedEvent ->
            Log.d("CalendarFragment", "Selected Event: $selectedEvent")
            displayEventDetails(selectedEvent) // Show details of the tapped event
        }
        binding.rvEventList.layoutManager = LinearLayoutManager(requireContext())
        binding.rvEventList.adapter = adapter
    }

    @SuppressLint("SetTextI18n")
    private fun displayEventDetails(event: Event) {
        Log.d("CalendarFragment", "Displaying Event Details: $event")
        binding.tvEventDetails.text = """
            Event ID: ${event.eventId}
            Date: ${event.date}
            Time: ${event.time}
            Location: ${event.location}
            Participants: ${event.participants.joinToString(", ")}
        """.trimIndent()
        binding.tvEventDetails.visibility = View.VISIBLE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
