package com.example.hobbyhub.hobby.view

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.hobbyhub.R
import com.example.hobbyhub.authentication.viewmodel.AuthViewModel
import com.example.hobbyhub.databinding.ActivityHobbyDetailsBinding
import com.example.hobbyhub.hobby.model.Hobby
import com.example.hobbyhub.hobby.viewmodel.HobbyRatingViewModel
import com.example.hobbyhub.hobby.viewmodel.UserHobbyViewModel
import kotlinx.coroutines.launch

class HobbyDetailsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHobbyDetailsBinding
    private var hobby: Hobby? = null
    private var userId: String? = null
    private val userHobbyViewModel: UserHobbyViewModel by viewModels()
    private val authViewModel: AuthViewModel by viewModels()
    private val reviewViewModel: HobbyRatingViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHobbyDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupToolbar()

        userId = authViewModel.getCurrentUserId()

        hobby = intent.getSerializableExtra("hobby") as? Hobby
        displayHobbyDetails()
        setupImageCarousel()
        setupFavoriteButton()
        fetchReviews()

        binding.showAllReviewsButton.setOnClickListener {
            val intent = Intent(this, RatingListActivity::class.java).apply {
                putExtra("hobbyId", hobby?.id)
            }
            startActivity(intent)
        }
    }

    private fun displayHobbyDetails() {
        hobby?.let {
            binding.toolbarTitle.text = it.name
            binding.detailDescription.text = it.description
            binding.hobbyCategory.text = it.category.toString()
            Log.d("displayHobbyDetails", "category -> ${it.category}")
        }
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.toolbar.setNavigationOnClickListener { onBackPressed() }
    }

    private fun setupImageCarousel() {
        hobby?.imageUrl?.let { imageUrls ->
            val imageCarouselAdapter = ImageCarouselAdapter(imageUrls.toMutableList())
            binding.carousel.adapter = imageCarouselAdapter
            binding.carousel.layoutManager =
                LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        }
    }

    private fun setupFavoriteButton() {
        val hobbyId = hobby?.id ?: return

        if (userId == null) {
            Log.d("setupFavoriteButton", "User ID is null")
            return
        }

        lifecycleScope.launch {
            // Observe favorite status and update UI
            userHobbyViewModel.isFavorite(userId!!, hobbyId)
            userHobbyViewModel.isFavorite.observe(this@HobbyDetailsActivity) { isFavorite ->
                updateFavoriteIcon(isFavorite)
            }

            // Handle toggle on click
            binding.loveIcon.setOnClickListener {
                toggleFavorite(hobbyId)
            }
        }
    }

    private fun updateFavoriteIcon(isFavorite: Boolean) {
        val drawableRes = if (isFavorite) {
            R.drawable.ic_favourite
        } else {
            R.drawable.ic_not_favourite
        }
        binding.loveIcon.setImageResource(drawableRes)
    }

    private fun toggleFavorite(hobbyId: String) {
        if (userId == null) {
            Log.d("toggleFavorite", "User ID is null")
            return
        }

        lifecycleScope.launch {
            val isFavorite = userHobbyViewModel.isFavorite.value ?: false
            if (isFavorite) {
                val success = userHobbyViewModel.removeFavorite(userId!!, hobbyId)
                showToast(success, "Removed from Favorites", "Failed to remove from Favorites")
            } else {
                val success = userHobbyViewModel.addFavorite(userId!!, hobbyId)
                showToast(success, "Added to Favorites", "Failed to add to Favorites")
            }
        }
    }

    private fun showToast(success: Boolean, successMsg: String, errorMsg: String) {
//        val message = if (success) successMsg else errorMsg
//        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun updateReviewCount(count: Int) {
        binding.reviewCount.text = "$count reviews"
    }

    private fun initializeProgressBars(maxCount: Int) {
        // Set maximum value for all progress bars
        binding.progressExcellent.max = maxCount
        binding.progressVeryGood.max = maxCount
        binding.progressAverage.max = maxCount
        binding.progressPoor.max = maxCount
        binding.progressTerrible.max = maxCount
    }

    private fun updateRatingCounts(
        excellentCount: Int,
        veryGoodCount: Int,
        averageCount: Int,
        poorCount: Int,
        terribleCount: Int
    ) {
        binding.progressExcellent.progress = excellentCount
        binding.excellentCount.text = excellentCount.toString()

        binding.progressVeryGood.progress = veryGoodCount
        binding.veryGoodCount.text = veryGoodCount.toString()

        binding.progressAverage.progress = averageCount
        binding.averageCount.text = averageCount.toString()

        binding.progressPoor.progress = poorCount
        binding.poorCount.text = poorCount.toString()

        binding.progressTerrible.progress = terribleCount
        binding.terribleCount.text = terribleCount.toString()
    }

    private fun updateProgressBars(
        excellent: Int,
        veryGood: Int,
        average: Int,
        poor: Int,
        terrible: Int
    ) {
        binding.progressExcellent.progress = excellent
        binding.progressVeryGood.progress = veryGood
        binding.progressAverage.progress = average
        binding.progressPoor.progress = poor
        binding.progressTerrible.progress = terrible
    }

    private fun fetchReviews() {
        hobby?.let { reviewViewModel.fetchReviewsByHobbyId(it.id) }
        reviewViewModel.reviewsLiveData.observe(this@HobbyDetailsActivity){ result ->
            val reviewCount = result.size
            var excellentCount = 0
            var veryGoodCount = 0
            var averageCount = 0
            var poorCount = 0
            var terribleCount = 0

            for (document in result) {
                val rating = document.rating.toDouble()
                when {
                    rating == 5.0 -> excellentCount++
                    rating >= 4.0 && rating < 5.0 -> veryGoodCount++
                    rating >= 3.0 && rating < 4.0 -> averageCount++
                    rating >= 2.0 && rating < 3.0 -> poorCount++
                    rating >= 1.0 && rating < 2.0 -> terribleCount++
                }
            }

            var totalRatingPoints = 0.0

            for (document in result) {
                val rating = document.rating.toDouble()
                totalRatingPoints += rating
            }

            // Update the review count
            updateReviewCount(reviewCount)

            // Initialize progress bar maximum values
            initializeProgressBars(reviewCount)

            val averageRating = if (reviewCount > 0) {
                totalRatingPoints / reviewCount.toDouble()
            } else {
                0f // Set to 0 if there are no reviews
            }

            val formattedAverageRating = String.format("%.1f", averageRating)

            // Update RatingBar with the calculated average rating
            binding.ratingBar.rating = formattedAverageRating.toFloat()
            binding.averageRating.text = formattedAverageRating

            // Check if reviewCount is greater than zero to avoid division by zero
            if (reviewCount > 0) {

                // Update progress bars with the actual counts
                updateProgressBars(
                    excellentCount,
                    veryGoodCount,
                    averageCount,
                    poorCount,
                    terribleCount
                )

                // Update rating counts
                updateRatingCounts(
                    excellentCount,
                    veryGoodCount,
                    averageCount,
                    poorCount,
                    terribleCount
                )
            } else {
                // Handle the case when there are no reviews
                updateProgressBars(0, 0, 0, 0, 0) // Reset progress bars
                updateRatingCounts(0, 0, 0, 0, 0)
            }
        }
    }
}
