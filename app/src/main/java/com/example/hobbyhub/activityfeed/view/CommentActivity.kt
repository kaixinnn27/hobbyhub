package com.example.hobbyhub.activityfeed.view

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.hobbyhub.R
import com.example.hobbyhub.activityfeed.model.Comment
import com.example.hobbyhub.activityfeed.viewmodel.PostViewModel
import com.example.hobbyhub.authentication.viewmodel.AuthViewModel
import com.example.hobbyhub.databinding.ActivityCommentBinding
import com.example.hobbyhub.utility.cropToBlob
import com.example.hobbyhub.utility.toBitmap
import kotlinx.coroutines.launch

class CommentActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCommentBinding
    private val postViewModel: PostViewModel by viewModels()
    private val authViewModel: AuthViewModel by viewModels()
    private var postId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCommentBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupToolbar()

        postId = intent.getStringExtra("postId")

        // Setup RecyclerView
        binding.commentsRv.layoutManager = LinearLayoutManager(this)

        // Load comments for the post
        postId?.let { loadComments(it) }

        // Handle the comment submission
        binding.submitCommentButton.setOnClickListener {
            val commentText = binding.commentInput.text.toString().trim()
            if (commentText.isNotEmpty() && postId != null) {
                submitComment(postId!!, commentText)
            } else {
                Toast.makeText(this, "Please enter a comment", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.toolbar.setNavigationOnClickListener { onBackPressed() }
    }

    private fun loadComments(postId: String) {
        lifecycleScope.launch {
            val comments = postViewModel.getComments(postId)
            val commentAdapter = CommentAdapter()
            binding.commentsRv.adapter = commentAdapter
            commentAdapter.submitList(comments)
        }
    }

    private fun submitComment(postId: String, commentText: String) {
        val userId = authViewModel.getCurrentUserId() ?: return
        lifecycleScope.launch {
            val user = authViewModel.get(userId) ?: return@launch

            val newComment = Comment(
                postId = postId,
                userId = userId,
                username = user.name,
                text = commentText,
                userProfile = user.photo
            )

            val success = postViewModel.addComment(postId, newComment)

            if (success) {
                // Provide feedback that comment was added
                Toast.makeText(this@CommentActivity, "Comment submitted", Toast.LENGTH_SHORT).show()

                // Optionally, reload the comments to include the new comment
                loadComments(postId)

                // Clear the input field after submission
                binding.commentInput.text.clear()
            } else {
                Toast.makeText(this@CommentActivity, "Failed to submit comment", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }
}
