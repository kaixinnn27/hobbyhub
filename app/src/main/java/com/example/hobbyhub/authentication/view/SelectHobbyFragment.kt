package com.example.hobbyhub.authentication.view

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.hobbyhub.R
import com.example.hobbyhub.authentication.viewmodel.AuthViewModel
import com.example.hobbyhub.databinding.FragmentSelectHobbyBinding
import com.example.hobbyhub.hobby.model.HobbyCategory
import com.example.hobbyhub.hobby.model.UserHobby
import com.example.hobbyhub.hobby.viewmodel.UserHobbyViewModel
import kotlinx.coroutines.launch

class SelectHobbyFragment : Fragment() {
    private lateinit var binding: FragmentSelectHobbyBinding
    private val selectedCategories = mutableListOf<HobbyCategory>()
    private val nav by lazy { findNavController() }
    private val authVm: AuthViewModel by activityViewModels()
    private val userHobbyVm: UserHobbyViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSelectHobbyBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUserHobby()
        setupCategoryClicks()

        binding.proceedButton.setOnClickListener {
            addHobbyCategories()
        }
    }

    private fun initUserHobby() {
        val userId = authVm.getCurrentUserId()
        if (userId != null) {
            lifecycleScope.launch {
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
                                context,
                                "UserHobby initialize successfully!",
                                Toast.LENGTH_SHORT
                            )
                                .show()
                        } else {
                            Toast.makeText(
                                context,
                                "Failed to initialize UserHobby!",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            }
        } else {
            Toast.makeText(context, "Please register again!", Toast.LENGTH_SHORT).show()
            nav.navigate(R.id.loginFragment)
        }
    }

    private fun addHobbyCategories() {
        Log.d("addHobbyCategories", "selectedCategories -> $selectedCategories")

        val userId = authVm.getCurrentUserId()
        if (userId != null && selectedCategories.isNotEmpty()) {
            val userHobbyObj = UserHobby(
                id = userId,
                preferredCategories = selectedCategories,
                savedHobbies = emptyList(),
                completedHobbies = emptyList(),
                hobbyRecommendations = emptyList(),
            )
            lifecycleScope.launch {
                val success: Boolean = userHobbyVm.set(userHobbyObj)
                if (success) {
                    Toast.makeText(
                        context,
                        "Registered successfully! Please login!",
                        Toast.LENGTH_SHORT
                    )
                        .show()
                    authVm.signOut()
                    nav.navigate(R.id.loginFragment)
                } else {
                    Toast.makeText(
                        context,
                        "Failed to add hobby! Please login again!",
                        Toast.LENGTH_SHORT
                    ).show()
                    nav.navigate(R.id.loginFragment)
                }
            }
        } else {
            Toast.makeText(context, "Please register again!", Toast.LENGTH_SHORT).show()
            nav.navigate(R.id.loginFragment)
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
}