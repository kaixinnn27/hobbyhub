package com.example.hobbyhub.chatroom.view.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.hobbyhub.chatroom.view.adapter.ChatAdapter
import com.example.hobbyhub.chatroom.model.ChatItem
import com.example.hobbyhub.chatroom.viewmodel.ChatViewModel
import com.example.hobbyhub.databinding.FragmentChatRoomBinding

class ChatRoomFragment : Fragment() {

    private lateinit var binding: FragmentChatRoomBinding
    private val chatViewModel: ChatViewModel by activityViewModels()
    private lateinit var chatAdapter: ChatAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentChatRoomBinding.inflate(inflater, container, false)

        chatAdapter = ChatAdapter(requireContext()) { chatItem ->
            when (chatItem) {
                is ChatItem.FriendItem -> {
                    // Extract friend details from chatItem
                    val friend = chatItem.friend
                    val intent = Intent(context, ChatActivity::class.java)
                    intent.putExtra("chatType", "friend")
                    intent.putExtra("chatId", friend.id) // friend ID
                    intent.putExtra("chatName", friend.name) // friend's name
                    startActivity(intent)
                }

                is ChatItem.GroupItem -> {
                    // Extract group details from chatItem
                    val group = chatItem.group
                    val intent = Intent(context, ChatActivity::class.java)
                    intent.putExtra("chatType", "group")
                    intent.putExtra("chatId", group.id) // group ID
                    intent.putExtra("chatName", group.name) // group name
                    startActivity(intent)
                }
            }
        }
        binding.rvFriendList.adapter = chatAdapter

        // Fetch and observe both friends and groups
        chatViewModel.getFriends().observe(viewLifecycleOwner) { friends ->
            chatViewModel.getGroups().observe(viewLifecycleOwner) { groups ->
                Log.d("ChatAdapter", "Friend items: $friends")
                Log.d("ChatAdapter", "Group items: $groups")
                val chatItems = mutableListOf<ChatItem>()
                chatItems.addAll(friends.map { ChatItem.FriendItem(it) })
                chatItems.addAll(groups.map { ChatItem.GroupItem(it) })
                chatAdapter.submitList(chatItems)
            }
        }

        setupFab()

        return binding.root
    }

    private fun setupFab() {
        binding.fabAddGroup.setOnClickListener {
            val intent = Intent(requireContext(), CreateGroupActivity::class.java)
            startActivity(intent)
        }
    }
}
