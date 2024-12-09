package com.example.hobbyhub.hobby.view

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.hobbyhub.R
import com.example.hobbyhub.authentication.viewmodel.AuthViewModel
import com.example.hobbyhub.databinding.ActivityFavouriteHobbyBinding
import com.example.hobbyhub.hobby.viewmodel.UserHobbyViewModel
import kotlinx.coroutines.launch

class FavouriteHobbyActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFavouriteHobbyBinding
    private val userHobbyViewModel: UserHobbyViewModel by viewModels()
    private val authViewModel: AuthViewModel by viewModels()
    private var userId: String? = null
    private lateinit var adapter: FilteredAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFavouriteHobbyBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupToolbar()
        setupRecyclerView()

        userId = authViewModel.getCurrentUserId()

        // Call getAllFavoriteHobbies asynchronously
        userId?.let {
            lifecycleScope.launch {
                val success = userHobbyViewModel.getAllFavoriteHobbies(it)
                if (success) {
                    // Observe the favorite hobbies list after loading
                    userHobbyViewModel.favoriteHobbies.observe(this@FavouriteHobbyActivity) { hobbies ->
                        // Update RecyclerView with the hobbies list
                        adapter = FilteredAdapter(hobbies)
                        binding.hobbiesRv.adapter = adapter
                    }
                } else {
                    // Handle error, e.g., show a message to the user
                    // You could show a Snackbar or a Toast here.
                }
            }
        }
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.toolbar.setNavigationOnClickListener { onBackPressed() }
    }

    private fun setupRecyclerView() {
        binding.hobbiesRv.layoutManager = LinearLayoutManager(this)
    }
}
