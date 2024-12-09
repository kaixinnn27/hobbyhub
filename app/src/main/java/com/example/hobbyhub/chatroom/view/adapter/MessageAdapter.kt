package com.example.hobbyhub.chatroom.view.adapter

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.hobbyhub.R
import com.example.hobbyhub.authentication.viewmodel.AuthViewModel
import com.example.hobbyhub.chatroom.model.Message
import com.example.hobbyhub.databinding.MessageItemBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class MessageAdapter(
    private val currentUserId: String,
    private val authViewModel: AuthViewModel,
    private val scope: CoroutineScope
) :
    RecyclerView.Adapter<MessageAdapter.MessageViewHolder>() {

    private var messagesList = mutableListOf<Message>()

    inner class MessageViewHolder(private val binding: MessageItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("SetTextI18n")
        fun bind(message: Message) {
            // Log message details
            Log.d("MessageAdapter", "Binding message: $message")

            when (message.type) {
                "text" -> {
                    if (message.senderId == currentUserId) {
                        // Show sent text message
                        binding.tvMessageContentSend.text = message.content
                        binding.tvMessageContentSend.visibility = View.VISIBLE

                        // Hide others
                        binding.tvSenderNameReceived.visibility = View.GONE
                        binding.tvMessageContentReceived.visibility = View.GONE
                        binding.invitationSectionSend.visibility = View.GONE
                        binding.invitationSectionReceive.visibility = View.GONE
                    } else {
                        scope.launch {
                            val user = authViewModel.get(message.senderId)
                            Log.d("MeesageAdpater", "Checking sender name -> $user")
                            binding.tvSenderNameReceived.visibility = View.VISIBLE
                            binding.tvSenderNameReceived.text = user?.name
                        }
                        // Show received text message

                        binding.tvMessageContentReceived.text = message.content
                        binding.tvMessageContentReceived.visibility = View.VISIBLE

                        // Hide others
                        binding.tvMessageContentSend.visibility = View.GONE
                        binding.invitationSectionSend.visibility = View.GONE
                        binding.invitationSectionReceive.visibility = View.GONE
                    }
                }

                "event_invitation" -> {
                    if (message.senderId == currentUserId) {
                        // Show sent invitation
                        binding.invitationSectionSend.visibility = View.VISIBLE
                        binding.invitationSectionReceive.visibility = View.GONE
                        binding.tvMessageContentSend.visibility = View.GONE
                        binding.tvMessageContentReceived.visibility = View.GONE
                        binding.tvSenderNameReceived.visibility = View.GONE

                        binding.tvInvitationMessageSend.text = "You've sent an event invitation"
                        binding.tvEventDetailsSend.text =
                            "Event: ${message.name ?: "Unknown"}\n" +
                                    "Date: ${message.eventDate ?: "Unknown"}\n" +
                                    "Time: ${message.eventStartTime ?: "Unknown"} - ${message.eventEndTime ?: "Unknown"}"
                    } else {
                        scope.launch {
                            val user = authViewModel.get(message.senderId)
                            Log.d("MeesageAdpater", "Checking sender name -> $user")
                            binding.tvSenderNameReceived.visibility = View.VISIBLE
                            binding.tvSenderNameReceived.text = user?.name
                        }
                        // Show received invitation
                        binding.invitationSectionReceive.visibility = View.VISIBLE
                        binding.invitationSectionSend.visibility = View.GONE
                        binding.tvMessageContentSend.visibility = View.GONE
                        binding.tvMessageContentReceived.visibility = View.GONE

                        binding.tvInvitationMessageReceive.text = "You're invited to an event"
                        binding.tvEventDetailsReceive.text =
                            "Event: ${message.name ?: "Unknown"}\n" +
                                    "Date: ${message.eventDate ?: "Unknown"}\n" +
                                    "Time: ${message.eventStartTime ?: "Unknown"} - ${message.eventEndTime ?: "Unknown"}"

                        // Set click listeners for actions
                        binding.btnAccept.setOnClickListener { acceptEventInvitation(message) }
                        binding.btnDecline.setOnClickListener { declineEventInvitation(message) }
                    }
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val binding = MessageItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MessageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val message = messagesList[position]
        holder.bind(message)
    }

    override fun getItemCount(): Int {
        return messagesList.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setMessages(messages: List<Message>) {
        messagesList = messages.toMutableList()
        notifyDataSetChanged()
    }

    private fun acceptEventInvitation(message: Message) {
        scope.launch {
            val sender = authViewModel.get(message.senderId)
            val currentUser = authViewModel.get(currentUserId)
            val eventDetails = mapOf(
                "id" to message.eventId,
                "name" to message.name,
                "date" to message.eventDate,
                "startTime" to message.eventStartTime,
                "endTime" to message.eventEndTime,
                "status" to "accepted",
                "location" to "",
                "reminderTime" to "",
                "participants" to listOf(sender?.name, currentUser?.name)
            )

            val userId = currentUser?.id
            userId?.let {
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
    }

    private fun declineEventInvitation(message: Message) {
        // do nothing so wont appear in schedule
    }
}
