package com.example.hobbyhub.profile.view

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.hobbyhub.BaseActivity
import com.example.hobbyhub.R
import com.example.hobbyhub.authentication.view.AuthenticationActivity
import com.example.hobbyhub.authentication.viewmodel.AuthViewModel
import com.example.hobbyhub.databinding.ActivityHobbyCategoryBinding
import com.example.hobbyhub.hobby.model.HobbyCategory
import com.example.hobbyhub.hobby.model.UserHobby
import com.example.hobbyhub.hobby.viewmodel.UserHobbyViewModel
import kotlinx.coroutines.launch

class HobbyCategoryActivity : BaseActivity() {

    private lateinit var binding: ActivityHobbyCategoryBinding
    private val selectedCategories = mutableListOf<HobbyCategory>()
    private val authVm: AuthViewModel by viewModels()
    private val userHobbyVm: UserHobbyViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHobbyCategoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()

        initUserHobby()
        setupCategoryClicks()

        binding.proceedButton.setOnClickListener {
            addHobbyCategories()
        }
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.toolbar.setNavigationOnClickListener { onBackPressed() }
    }

    private fun initUserHobby() {
        val userId = authVm.getCurrentUserId()
        if (userId != null) {
            lifecycleScope.launch {
                // Fetch the UserHobby from Firestore
                val userHobby = userHobbyVm.get(userId)
                if (userHobby == null) {
                    val userHobbyObj = UserHobby(
                        id = userId,
                        preferredCategories = emptyList(),
                        savedHobbies = emptyList(),
                        completedHobbies = emptyList(),
                        hobbyRecommendations = emptyList(),
                    )
                    lifecycleScope.launch {
                        val success: Boolean = userHobbyVm.set(userHobbyObj)
                        if (success) {
                            Toast.makeText(
                                this@HobbyCategoryActivity,
                                "UserHobby initialized successfully!",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            Toast.makeText(
                                this@HobbyCategoryActivity,
                                "Failed to initialize UserHobby!",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                } else {
                    // Fetch the preferred categories from Firestore and mark them as selected
                    val preferredCategories = userHobbyVm.getPreferredCategories(userId)
                    selectCategories(preferredCategories)
                }
            }
        } else {
            Toast.makeText(this@HobbyCategoryActivity, "Please register again!", Toast.LENGTH_SHORT)
                .show()
            val intent = Intent(this, AuthenticationActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun selectCategories(categories: List<HobbyCategory>) {
        // Loop through each category and mark it as selected
        categories.forEach { category ->
            when (category) {
                HobbyCategory.ARTS_CRAFTS -> toggleCategorySelection(
                    binding.categoryArtsCrafts,
                    HobbyCategory.ARTS_CRAFTS
                )

                HobbyCategory.OUTDOOR_ACTIVITIES -> toggleCategorySelection(
                    binding.categoryOutdoorActivities,
                    HobbyCategory.OUTDOOR_ACTIVITIES
                )

                HobbyCategory.FOOD_COOKING -> toggleCategorySelection(
                    binding.categoryFoodCooking,
                    HobbyCategory.FOOD_COOKING
                )

                HobbyCategory.FITNESS_WELLNESS -> toggleCategorySelection(
                    binding.categoryFitness,
                    HobbyCategory.FITNESS_WELLNESS
                )

                HobbyCategory.TECHNOLOGY_SCIENCE -> toggleCategorySelection(
                    binding.categoryTechnology,
                    HobbyCategory.TECHNOLOGY_SCIENCE
                )

                HobbyCategory.MUSIC_PERFORMANCE -> toggleCategorySelection(
                    binding.categoryMusic,
                    HobbyCategory.MUSIC_PERFORMANCE
                )

                HobbyCategory.MISCELLANEOUS -> toggleCategorySelection(
                    binding.categoryMiscellaneous,
                    HobbyCategory.MISCELLANEOUS
                )
            }
        }
    }

//    private fun toggleCategorySelection(view: View, category: HobbyCategory) {
//        // Ensure the category is selected based on the current view state
//        val isSelected = view.isSelected
//        if (!isSelected) {
//            view.isSelected = true
//            view.setBackgroundResource(R.drawable.selected_background)
//            selectedCategories.add(category)
//        }
//    }

    private fun toggleCategorySelection(view: View, category: HobbyCategory) {
        val isSelected = view.isSelected
        view.isSelected = !isSelected

        // Update background
        view.setBackgroundResource(
            if (view.isSelected) R.drawable.selected_background else R.drawable.default_background
        )

        // Update selection list
        if (view.isSelected) {
            selectedCategories.add(category)
        } else {
            selectedCategories.remove(category)
        }
    }

    private fun addHobbyCategories() {
        Log.d("addHobbyCategories", "selectedCategories -> $selectedCategories")

        val userId = authVm.getCurrentUserId()
        if (userId != null && selectedCategories.isNotEmpty()) {
            val userHobbyObj = UserHobby(
                id = userId,
                preferredCategories = selectedCategories,
                completedHobbies = emptyList(),
                hobbyRecommendations = emptyList(),
            )
            lifecycleScope.launch {
                val success: Boolean = userHobbyVm.update(userHobbyObj)
                if (success) {
                    Toast.makeText(
                        this@HobbyCategoryActivity,
                        "Registered successfully! Please login!",
                        Toast.LENGTH_SHORT
                    ).show()
                    finish()
                } else {
                    Toast.makeText(
                        this@HobbyCategoryActivity,
                        "Failed to add hobby! Please try again!",
                        Toast.LENGTH_SHORT
                    ).show()
                    finish()
                }
            }
        } else {
            Toast.makeText(this@HobbyCategoryActivity, "Please login again!", Toast.LENGTH_SHORT)
                .show()
            val intent = Intent(this, AuthenticationActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun setupCategoryClicks() {
        val artsCrafts = binding.root.findViewById<LinearLayout>(R.id.categoryArtsCrafts)
        val outdoorActivities =
            binding.root.findViewById<LinearLayout>(R.id.categoryOutdoorActivities)
        val foodCooking = binding.root.findViewById<LinearLayout>(R.id.categoryFoodCooking)
        val fitness = binding.root.findViewById<LinearLayout>(R.id.categoryFitness)
        val technology = binding.root.findViewById<LinearLayout>(R.id.categoryTechnology)
        val music = binding.root.findViewById<LinearLayout>(R.id.categoryMusic)
        val miscellaneous = binding.root.findViewById<LinearLayout>(R.id.categoryMiscellaneous)

        artsCrafts.setOnClickListener {
            toggleCategorySelection(artsCrafts, HobbyCategory.ARTS_CRAFTS)
        }

        outdoorActivities.setOnClickListener {
            toggleCategorySelection(outdoorActivities, HobbyCategory.OUTDOOR_ACTIVITIES)
        }

        foodCooking.setOnClickListener {
            toggleCategorySelection(foodCooking, HobbyCategory.FOOD_COOKING)
        }

        fitness.setOnClickListener {
            toggleCategorySelection(fitness, HobbyCategory.FITNESS_WELLNESS)
        }

        technology.setOnClickListener {
            toggleCategorySelection(technology, HobbyCategory.TECHNOLOGY_SCIENCE)
        }

        music.setOnClickListener {
            toggleCategorySelection(music, HobbyCategory.MUSIC_PERFORMANCE)
        }

        miscellaneous.setOnClickListener {
            toggleCategorySelection(miscellaneous, HobbyCategory.MISCELLANEOUS)
        }
    }
}
