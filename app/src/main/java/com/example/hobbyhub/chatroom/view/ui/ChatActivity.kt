package com.example.hobbyhub.chatroom.view.ui

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.hobbyhub.authentication.viewmodel.AuthViewModel
import com.example.hobbyhub.chatroom.view.adapter.MessageAdapter
import com.example.hobbyhub.chatroom.viewmodel.ChatViewModel
import com.example.hobbyhub.databinding.ActivityChatBinding
import com.google.firebase.auth.FirebaseAuth

class ChatActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatBinding
    private lateinit var messageAdapter: MessageAdapter
    private val chatViewModel: ChatViewModel by viewModels()
    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupToolbar()
        val userId = authViewModel.getCurrentUserId()

        messageAdapter = userId?.let { MessageAdapter(userId, authViewModel, lifecycleScope ) }!!
        binding.rvMessages.apply {
            adapter = messageAdapter
            layoutManager = LinearLayoutManager(this@ChatActivity)
        }

        val chatType = intent.getStringExtra("chatType") ?: ""
        val chatId = intent.getStringExtra("chatId") ?: ""
        val chatName = intent.getStringExtra("chatName") ?: ""

        binding.tvUsername.text = chatName

        // Handle both friend and group chats
        if (chatType == "friend") {
            // Friend chat
            chatViewModel.getMessagesWithFriend(chatId).observe(this, Observer { messages ->
                messageAdapter.setMessages(messages)
                binding.rvMessages.scrollToPosition(messageAdapter.itemCount - 1)
            })
        } else if (chatType == "group") {
            // Group chat
            chatViewModel.getGroupMessages(chatId).observe(this, Observer { messages ->
                messageAdapter.setMessages(messages)
                binding.rvMessages.scrollToPosition(messageAdapter.itemCount - 1)
            })
        }

        // Send button click listener
        binding.btnSend.setOnClickListener {
            val messageContent = binding.etMessage.text.toString().trim()
            if (messageContent.isNotEmpty()) {
                if (chatType == "friend") {
                    chatViewModel.sendMessage(chatId, messageContent)
                } else if (chatType == "group") {
                    chatViewModel.sendGroupMessage(chatId, messageContent)
                }
                binding.etMessage.text.clear()
            }
        }
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.toolbar.setNavigationOnClickListener { onBackPressed() }
    }
}
