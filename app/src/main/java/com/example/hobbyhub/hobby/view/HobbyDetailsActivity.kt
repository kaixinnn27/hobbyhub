package com.example.hobbyhub.hobby.view

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.hobbyhub.R
import com.example.hobbyhub.databinding.ActivityHobbyDetailsBinding
import com.example.hobbyhub.hobby.model.Hobby

class HobbyDetailsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHobbyDetailsBinding
    private var hobby: Hobby? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHobbyDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupToolbar()

        hobby = intent.getSerializableExtra("hobby") as? Hobby
        displayHobbyDetails()
        setupImageCarousel()
    }

    private fun displayHobbyDetails() {
        // Set description and address
        if (hobby != null) {
            binding.toolbarTitle.text = hobby!!.name
            binding.detailDescription.text = hobby!!.description
            binding.hobbyCategory.text = hobby!!.category.toString()
            Log.d("displayHobbyDetails", "category -> ${hobby!!.category}")
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

    private fun setupImageCarousel() {
        val imageUrls = hobby?.imageUrl
        val imageCarouselAdapter = imageUrls?.let { ImageCarouselAdapter(it.toMutableList()) }

        // Set the adapter to the RecyclerView
        binding.carousel.adapter = imageCarouselAdapter
        binding.carousel.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
    }
}