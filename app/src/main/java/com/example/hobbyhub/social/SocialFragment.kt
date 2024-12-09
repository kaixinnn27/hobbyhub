package com.example.hobbyhub.social

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.example.hobbyhub.R
import com.example.hobbyhub.authentication.viewmodel.AuthViewModel
import com.example.hobbyhub.chatroom.viewmodel.ChatViewModel
import com.example.hobbyhub.databinding.FragmentSocialBinding
import com.example.hobbyhub.utility.toBitmap
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.launch

class SocialFragment : Fragment() {

    private lateinit var binding: FragmentSocialBinding
    private val authViewModel: AuthViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSocialBinding.inflate(inflater, container, false)

        val adapter = SocialPagerAdapter(this)
        binding.viewPager.adapter = adapter

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "Activity Feed"
                1 -> "Chat"
                else -> null
            }
        }.attach()

        val userId = authViewModel.getCurrentUserId()

        if (userId != null) {
            viewLifecycleOwner.lifecycleScope.launch {
                val user = authViewModel.get(userId)
                if (user != null) {

                    if (user.photo.toBitmap() != null) {
                        binding.headerProfile.setImageBitmap(user.photo.toBitmap())
                        binding.letterOverlayTv.visibility = View.GONE

                    } else {
                        binding.headerProfile.setImageResource(R.drawable.profile_bg)
                        binding.letterOverlayTv.visibility = View.VISIBLE

                        val firstLetter = user.name.firstOrNull()?.toString()?.uppercase() ?: "U"
                        binding.letterOverlayTv.text = firstLetter
                    }
                }
            }
        }

        return binding.root
    }
}
