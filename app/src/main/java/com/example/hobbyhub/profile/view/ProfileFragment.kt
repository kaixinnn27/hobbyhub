package com.example.hobbyhub.profile.view

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
import com.example.hobbyhub.hobby.view.FavouriteHobbyActivity
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
    ): View {
        binding = FragmentProfileBinding.inflate(inflater, container, false)

        auth = Firebase.auth
        val currentUser = auth.currentUser
        binding.logoutBtn.setOnClickListener {
            auth.signOut()
            val intent = Intent(context, AuthenticationActivity::class.java)
            startActivity(intent)
            requireActivity().finish()
        }

        binding.savedHobbyBtn.setOnClickListener {
            val intent = Intent(context, FavouriteHobbyActivity::class.java)
            startActivity(intent)
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
                    binding.userEmail.text = user.email

                    if (user.photo.toBitmap() != null) {
                        binding.headerProfile.setImageBitmap(user.photo.toBitmap())
                        binding.letterOverlayTv.visibility = View.GONE

                        binding.imageViewProfile.setImageBitmap(user.photo.toBitmap())
                        binding.letterOverlay.visibility = View.GONE
                    } else {
                        binding.imageViewProfile.setImageResource(R.drawable.profile_bg)
                        binding.headerProfile.setImageResource(R.drawable.profile_bg)
                        binding.letterOverlay.visibility = View.VISIBLE
                        binding.letterOverlayTv.visibility = View.VISIBLE

                        val firstLetter = user.name.firstOrNull()?.toString()?.uppercase() ?: "U"
                        binding.letterOverlay.text = firstLetter
                        binding.letterOverlayTv.text = firstLetter
                    }
                }
            }
        }

        chatViewModel.getFriends().observe(viewLifecycleOwner) {
            val (friend1, friend2) = chatViewModel.getTopTwoFriends()

            if (friend1 == null && friend2 == null) {
                binding.tvBuddyLabel.visibility = View.GONE
                binding.firstDivider.visibility = View.GONE
            }

            if (friend1 != null) {
                binding.friend1.visibility = View.VISIBLE
                if (friend1.photo.toBitmap() != null) {
                    binding.avatar1.setImageBitmap(friend1.photo.toBitmap())
                }
                binding.tvFriendName1.text = friend1.name
            }

            if (friend2 != null) {
                binding.friend2.visibility = View.VISIBLE
                if (friend2.photo.toBitmap() != null) {
                    binding.avatar2.setImageBitmap(friend2.photo.toBitmap())
                }
                binding.tvFriendName2.text = friend2.name
            }
        }

        binding.settingBtn.setOnClickListener {
            val intent = Intent(context, SettingActivity::class.java)
            startActivity(intent)
        }

        return binding.root
    }
}