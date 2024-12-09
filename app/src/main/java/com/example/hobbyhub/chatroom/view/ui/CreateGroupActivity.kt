package com.example.hobbyhub.chatroom.view.ui

import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.hobbyhub.authentication.viewmodel.AuthViewModel
import com.example.hobbyhub.chatroom.view.adapter.SelectFriendAdapter
import com.example.hobbyhub.chatroom.viewmodel.ChatViewModel
import com.example.hobbyhub.databinding.ActivityCreateGroupBinding

class CreateGroupActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCreateGroupBinding
    private val chatViewModel: ChatViewModel by lazy { ChatViewModel() }
    private val authViewModel: AuthViewModel by viewModels()
    private lateinit var selectFriendAdapter: SelectFriendAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateGroupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Setup RecyclerView
        selectFriendAdapter = SelectFriendAdapter()
        binding.rvSelectFriends.adapter = selectFriendAdapter
        binding.rvSelectFriends.layoutManager = LinearLayoutManager(this)

        // Fetch and observe friends
        chatViewModel.getFriends().observe(this) { friends ->
            selectFriendAdapter.submitList(friends)
        }

        // Create group on button click
        binding.btnCreateGroup.setOnClickListener {
            val selectedFriends = selectFriendAdapter.getSelectedFriends()
            val userId = authViewModel.getCurrentUserId()
            if (selectedFriends.isNotEmpty() && userId != null) {
                val newList = selectedFriends + userId
                showGroupNameDialog(newList)
            } else {
                Toast.makeText(this, "Please select at least one friend", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showGroupNameDialog(selectedFriends: List<String>) {
        val input = EditText(this).apply {
            hint = "Enter group name"
        }

        AlertDialog.Builder(this)
            .setTitle("Group Name")
            .setMessage("Enter a name for your group")
            .setView(input)
            .setPositiveButton("Create") { _, _ ->
                val groupName = input.text.toString().trim()
                if (groupName.isNotEmpty()) {
                    chatViewModel.createGroup(groupName, selectedFriends)
                    Toast.makeText(this, "Group '$groupName' created!", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this, "Group name cannot be empty", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .create()
            .show()
    }
}
