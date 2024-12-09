package com.example.hobbyhub.activityfeed.view

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.hobbyhub.activityfeed.viewmodel.PostViewModel
import com.example.hobbyhub.authentication.viewmodel.AuthViewModel
import com.example.hobbyhub.databinding.FragmentActivityFeedBinding

class ActivityFeedFragment : Fragment() {

    private lateinit var binding: FragmentActivityFeedBinding
    private val postViewModel: PostViewModel by viewModels()
    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentActivityFeedBinding.inflate(inflater, container, false)

        setupFab()
        setupRecyclerView()

        return binding.root
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun setupRecyclerView() {
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())

        val userId = authViewModel.getCurrentUserId()
        userId?.let {
            postViewModel.fetchPosts()
            postViewModel.posts.observe(viewLifecycleOwner) { posts ->
                val adapter = PostAdapter(posts, postViewModel, userId, viewLifecycleOwner.lifecycleScope, authViewModel)
                binding.recyclerView.adapter = adapter
                val sortedPosts = posts.sortedByDescending { it.timestamp }
                adapter.submitList(sortedPosts)
            }
        }
    }

    private fun setupFab() {
        binding.fabCreatePost.setOnClickListener {
            val intent = Intent(requireContext(), CreatePostActivity::class.java)
            startActivity(intent)
        }
    }
}
