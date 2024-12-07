package com.example.hobbyhub.chatroom.view.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.hobbyhub.R
import com.example.hobbyhub.chatroom.model.Message
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MessageAdapter(private val currentUserId: String) : RecyclerView.Adapter<MessageAdapter.MessageViewHolder>() {

    private var messagesList = mutableListOf<Message>()

    inner class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // Text Message Views
        val messageContentSend: TextView = itemView.findViewById(R.id.tvMessageContentSend)
        val messageContentReceived: TextView = itemView.findViewById(R.id.tvMessageContentReceived)

        // Invitation Message Views
        val invitationSectionSend: View = itemView.findViewById(R.id.invitationSectionSend)
        val tvInvitationMessageSend: TextView = itemView.findViewById(R.id.tvInvitationMessageSend)
        val tvEventDetailsSend: TextView = itemView.findViewById(R.id.tvEventDetailsSend)

        val invitationSectionReceive: View = itemView.findViewById(R.id.invitationSectionReceive)
        val tvInvitationMessageReceive: TextView = itemView.findViewById(R.id.tvInvitationMessageReceive)
        val tvEventDetailsReceive: TextView = itemView.findViewById(R.id.tvEventDetailsReceive)
        val btnAccept: View = itemView.findViewById(R.id.btnAccept)
        val btnDecline: View = itemView.findViewById(R.id.btnDecline)

        fun bind(message: Message) {
            // Log message details
            Log.d("MessageAdapter", "Binding message: $message")
            when (message.type) {
                "text" -> {
                    if (message.senderId == currentUserId) {
                        // Show sent text message
                        messageContentSend.text = message.content
                        messageContentSend.visibility = View.VISIBLE

                        // Hide others
                        messageContentReceived.visibility = View.GONE
                        invitationSectionSend.visibility = View.GONE
                        invitationSectionReceive.visibility = View.GONE
                    } else {
                        // Show received text message
                        messageContentReceived.text = message.content
                        messageContentReceived.visibility = View.VISIBLE

                        // Hide others
                        messageContentSend.visibility = View.GONE
                        invitationSectionSend.visibility = View.GONE
                        invitationSectionReceive.visibility = View.GONE
                    }
                }

                "event_invitation" -> {
                    if (message.senderId == currentUserId) {
                        // Show sent invitation
                        invitationSectionSend.visibility = View.VISIBLE
                        invitationSectionReceive.visibility = View.GONE
                        messageContentSend.visibility = View.GONE
                        messageContentReceived.visibility = View.GONE

                        tvInvitationMessageSend.text = "You've sent an event invitation"
                        tvEventDetailsSend.text = "Event: ${message.eventId ?: "Unknown"}\n" +
                                "Date: ${message.eventDate ?: "Unknown"}\n" +
                                "Time: ${message.eventStartTime ?: "Unknown"} - ${message.eventEndTime ?: "Unknown"}"
                    } else {
                        // Show received invitation
                        invitationSectionReceive.visibility = View.VISIBLE
                        invitationSectionSend.visibility = View.GONE
                        messageContentSend.visibility = View.GONE
                        messageContentReceived.visibility = View.GONE

                        tvInvitationMessageReceive.text = "You're invited to an event"
                        tvEventDetailsReceive.text = "Event: ${message.eventId ?: "Unknown"}\n" +
                                "Date: ${message.eventDate ?: "Unknown"}\n" +
                                "Time: ${message.eventStartTime ?: "Unknown"} - ${message.eventEndTime ?: "Unknown"}"

                        // Set click listeners for actions
                        btnAccept.setOnClickListener { acceptEventInvitation(message) }
                        btnDecline.setOnClickListener { declineEventInvitation(message) }
                    }
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.message_item, parent, false)
        return MessageViewHolder(view)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val message = messagesList[position]
        holder.bind(message)
    }

    override fun getItemCount(): Int {
        return messagesList.size
    }

    fun setMessages(messages: List<Message>) {
        messagesList = messages.toMutableList()
        notifyDataSetChanged()
    }

    private fun acceptEventInvitation(message: Message) {
        val eventDetails = mapOf(
            "eventId" to message.eventId,
            "date" to message.eventDate,
            "startTime" to message.eventStartTime,
            "endTime" to message.eventEndTime,
            "status" to "accepted"
        )

        val currentUser = FirebaseAuth.getInstance().currentUser?.uid
        currentUser?.let {
            FirebaseFirestore.getInstance().collection("schedule")
                .document(it)
                .collection("events")
                .document(message.eventId ?: "")
                .set(eventDetails)
                .addOnSuccessListener {
                    Log.d("MessageAdapter", "Event accepted: ${message.eventId}")
                }
                .addOnFailureListener { e ->
                    Log.e("MessageAdapter", "Failed to accept event: ${e.message}")
                }
        }
    }

    private fun declineEventInvitation(message: Message) {
        Log.d("MessageAdapter", "Event declined: ${message.eventId}")
    }
}
