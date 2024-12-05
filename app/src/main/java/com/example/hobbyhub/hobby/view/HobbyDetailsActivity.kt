package com.example.hobbyhub.hobby.view

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
import com.example.hobbyhub.hobby.viewmodel.UserHobbyViewModel
import kotlinx.coroutines.launch

class HobbyDetailsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHobbyDetailsBinding
    private var hobby: Hobby? = null
    private var userId: String? = null
    private val userHobbyViewModel: UserHobbyViewModel by viewModels()
    private val authViewModel: AuthViewModel by viewModels()

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
}
