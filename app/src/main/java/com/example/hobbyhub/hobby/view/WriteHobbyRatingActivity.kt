package com.example.hobbyhub.hobby.view

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.hobbyhub.R
import com.example.hobbyhub.authentication.viewmodel.AuthViewModel
import com.example.hobbyhub.databinding.ActivityWriteHobbyRatingBinding
import com.example.hobbyhub.hobby.model.UserRating
import com.example.hobbyhub.hobby.viewmodel.HobbyRatingViewModel
import com.google.firebase.auth.FirebaseAuth

class WriteHobbyRatingActivity : AppCompatActivity() {
    private lateinit var binding: ActivityWriteHobbyRatingBinding
    private val reviewViewModel: HobbyRatingViewModel by viewModels()
    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWriteHobbyRatingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()

        binding.buttonSubmit.setOnClickListener {
            submitReview()
        }
    }

    private fun submitReview() {
        val rating = binding.ratingBar.rating
        val reviewText = binding.editTextReview.text.toString().trim()

        if (rating == 0f || reviewText.isEmpty()) {
            Toast.makeText(this, "Please provide a rating and a review.", Toast.LENGTH_SHORT).show()
            return
        }

        val userId = authViewModel.getCurrentUserId()?: run {
            Toast.makeText(this, "User ID not found.", Toast.LENGTH_SHORT).show()
            return
        }

        // Get the place ID from the intent
        val hobbyId = intent.getStringExtra("hobbyId") ?: run {
            Toast.makeText(this, "Place ID not found.", Toast.LENGTH_SHORT).show()
            return
        }
        val userRating = UserRating(userId, hobbyId, rating, reviewText) // Make sure placeId is included

        // Call ViewModel to save the review
        reviewViewModel.addReview(userRating) { success ->
            if (success) {
                Toast.makeText(this, "Review submitted successfully!", Toast.LENGTH_SHORT).show()
                finish() // Close the activity after submission
            } else {
                Toast.makeText(this, "Failed to submit review.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.toolbar.setNavigationOnClickListener {
            this.onBackPressed()
        }
    }
}