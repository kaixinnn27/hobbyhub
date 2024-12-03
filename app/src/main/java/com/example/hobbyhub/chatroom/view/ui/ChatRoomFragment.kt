package com.example.hobbyhub.chatroom.view.ui

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.DividerItemDecoration
import com.example.hobbyhub.chatroom.view.adapter.UserFriendAdapter
import com.example.hobbyhub.chatroom.viewmodel.ChatViewModel
import com.example.hobbyhub.databinding.FragmentChatRoomBinding

class ChatRoomFragment : Fragment() {

    private lateinit var binding: FragmentChatRoomBinding
    private val chatViewModel: ChatViewModel by activityViewModels()
    private lateinit var friendAdapter: UserFriendAdapter
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentChatRoomBinding.inflate(inflater, container, false)

        friendAdapter = UserFriendAdapter(requireContext()) { holder, friend ->
            // No need to navigate here, handled inside the adapter
        }

        binding.rvFriendList.adapter = friendAdapter
        binding.rvFriendList.addItemDecoration(DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL))

        chatViewModel.getFriends().observe(viewLifecycleOwner) { friends ->
            Log.d("ChatRoomFragment", "Friends list updated: ${friends.size} items")
            friendAdapter.submitList(friends)
        }

        return binding.root
    }
}