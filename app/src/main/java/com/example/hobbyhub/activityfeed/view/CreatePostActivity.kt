package com.example.hobbyhub.activityfeed.view

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.hobbyhub.R
import com.example.hobbyhub.activityfeed.model.Post
import com.example.hobbyhub.activityfeed.viewmodel.PostViewModel
import com.example.hobbyhub.authentication.model.User
import com.example.hobbyhub.authentication.viewmodel.AuthViewModel
import com.example.hobbyhub.databinding.ActivityCreatePostBinding
import com.example.hobbyhub.utility.cropToBlob
import com.example.hobbyhub.utility.toBitmap
import com.google.firebase.firestore.Blob
import kotlinx.coroutines.launch

class CreatePostActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCreatePostBinding
    private val postViewModel: PostViewModel by viewModels()
    private val authViewModel: AuthViewModel by viewModels()

    private val launcher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {
                binding.previewImageView.setImageURI(it.data?.data)
                binding.previewImageView.visibility = View.VISIBLE
                binding.addImageBtn.visibility = View.GONE
            } else {
                binding.previewImageView.visibility = View.GONE
                binding.addImageBtn.visibility = View.VISIBLE
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreatePostBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()

        val userId = authViewModel.getCurrentUserId()
        if (!userId.isNullOrBlank()) {
            lifecycleScope.launch {
                val user = authViewModel.get(userId)
                user?.let {
                    populateUserData(it)
                }
            }
        }

        binding.previewImageView.setOnClickListener {
            openImagePicker()
        }

        binding.addImageBtn.setOnClickListener {
            openImagePicker()
        }

        binding.submitPostButton.setOnClickListener {
            createPost()
        }
    }

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        launcher.launch(intent)
    }

    private fun createPost() {
        val photo = binding.previewImageView.drawable
        val description = binding.descriptionEditText.text.toString()

        if (photo == null) {
            Toast.makeText(this, "Please select an image", Toast.LENGTH_SHORT).show()
            return
        }

        if (description.isEmpty()) {
            Toast.makeText(this, "Please add a description", Toast.LENGTH_SHORT).show()
            return
        }

        // Show a loading indicator
        binding.submitPostButton.isEnabled = false
        val userId = authViewModel.getCurrentUserId()
        if (!userId.isNullOrEmpty()) {
            lifecycleScope.launch {
                val user = authViewModel.get(userId)
                if (user != null) {
                    val post = Post(
                        userId = userId,
                        username = user.name,
                        description = description,
                        photo = binding.previewImageView.cropToBlob(300, 300)
                    )
                    val success = postViewModel.addPost(post)
                    handlePostSubmissionResult(success)
                } else {
                    binding.submitPostButton.isEnabled = true
                }
            }
        } else {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show()
            binding.submitPostButton.isEnabled = true
        }
    }

    private fun handlePostSubmissionResult(success: Boolean) {
        runOnUiThread {
            if (success) {
                Toast.makeText(this, "Post created successfully!", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, "Failed to create post", Toast.LENGTH_SHORT).show()
            }
            binding.submitPostButton.isEnabled = true
        }
    }

    private fun populateUserData(user: User) {
        with(binding) {
            usernameTv.text = user.name
            if (user.photo.toBitmap() != null) {
                binding.headerProfile.setImageBitmap(user.photo.toBitmap())
            } else {
                binding.headerProfile.setImageResource(R.drawable.profile)
            }
        }
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.toolbar.setNavigationOnClickListener { onBackPressed() }
    }
}