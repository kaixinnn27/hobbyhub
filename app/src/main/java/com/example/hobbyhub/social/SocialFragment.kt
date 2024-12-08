package com.example.hobbyhub.social

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.hobbyhub.R
import com.example.hobbyhub.databinding.FragmentSocialBinding
import com.google.android.material.tabs.TabLayoutMediator

class SocialFragment : Fragment() {

    private lateinit var binding: FragmentSocialBinding

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

        return binding.root
    }
}
