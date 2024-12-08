package com.example.hobbyhub.social

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.hobbyhub.activityfeed.view.ActivityFeedFragment
import com.example.hobbyhub.chatroom.view.ui.ChatRoomFragment

class SocialPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> ActivityFeedFragment()
            1 -> ChatRoomFragment()
            else -> throw IllegalStateException("Invalid position")
        }
    }
}
