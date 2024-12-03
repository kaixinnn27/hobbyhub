package com.example.hobbyhub.profile.view.ui

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.example.hobbyhub.R
import com.example.hobbyhub.authentication.view.AuthenticationActivity
import com.example.hobbyhub.chatroom.viewmodel.ChatViewModel
import com.example.hobbyhub.databinding.FragmentProfileBinding
import com.example.hobbyhub.profile.viewmodel.ProfileViewModel
import com.example.hobbyhub.utility.toBitmap
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import kotlinx.coroutines.launch

class ProfileFragment : Fragment() {

    private lateinit var binding: FragmentProfileBinding
    private lateinit var auth: FirebaseAuth
    private val vm: ProfileViewModel by activityViewModels()
    private val chatViewModel: ChatViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentProfileBinding.inflate(inflater, container, false)

        auth = Firebase.auth
        val currentUser = auth.currentUser
        binding.logoutBtn.setOnClickListener {
            auth.signOut()
            val intent = Intent(context, AuthenticationActivity::class.java)
            startActivity(intent)
            requireActivity().finish()
        }

        binding.editBtn.setOnClickListener {
            if (currentUser != null) {
                val intent = Intent(context, ProfileDetailsActivity::class.java)
                intent.putExtra("userId", currentUser.uid)
                startActivity(intent)
            } else {
                AlertDialog.Builder(context)
                    .setIcon(R.drawable.ic_error)
                    .setTitle("Error")
                    .setMessage("User not found!")
                    .setPositiveButton("Dismiss", null)
                    .show()
            }
        }

        currentUser?.uid?.let { userId ->
            viewLifecycleOwner.lifecycleScope.launch {
                val user = vm.get(userId)
                if (user != null) {
                    binding.tvUsername.text = user.name
                    binding.tvCourse.text = user.studyField
                    binding.tvLearningStyle.text = user.learningStyle
                    binding.tvInterest.text = user.interest
//                    binding.imgProfile.setImageBitmap(user.photo?.toBitmap())
                    if (user.photo.toBitmap() != null) {
                        // Set the user's photo if it's not null
                        binding.imgProfile.setImageBitmap(user.photo.toBitmap())
                    }
                    else{
                        binding.imgProfile.setImageResource(R.drawable.profile)
                    }
                }
            }
        }

        chatViewModel.getFriends().observe(viewLifecycleOwner) {
            val (friend1, friend2) = chatViewModel.getTopTwoFriends()

            if(friend1 == null && friend2 == null){
                binding.tvBuddyLabel.visibility = View.GONE
            }

            if (friend1 != null) {
                binding.friend1.visibility = View.VISIBLE
                binding.avatar1.setImageBitmap(friend1.photo?.toBitmap())
                binding.tvFriendName1.text = friend1.name
            }

            if (friend2 != null) {
                binding.friend2.visibility = View.VISIBLE
                binding.avatar2.setImageBitmap(friend2.photo?.toBitmap())
                binding.tvFriendName2.text = friend2.name
            }
        }

        return binding.root
    }
}