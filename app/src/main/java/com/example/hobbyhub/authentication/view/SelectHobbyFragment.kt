package com.example.hobbyhub.authentication.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.hobbyhub.R
import com.example.hobbyhub.databinding.FragmentSelectHobbyBinding
import com.example.hobbyhub.hobby.model.HobbyCategory

class SelectHobbyFragment : Fragment() {
    private lateinit var binding: FragmentSelectHobbyBinding
    private val selectedCategories = mutableListOf<HobbyCategory>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSelectHobbyBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        populateHobbyCategories()

        binding.proceedButton.setOnClickListener {
            println("Selected Categories: $selectedCategories")
            // Proceed to next step or fragment
        }
    }

    private fun populateHobbyCategories() {
        val categories = HobbyCategory.entries.toTypedArray() // Use your enum
        val gridLayout = binding.gridLayoutHobbyCategories

        categories.forEach { category ->
            val categoryView = LayoutInflater.from(context).inflate(R.layout.category_item, gridLayout, false)
            val categoryLabel = categoryView.findViewById<TextView>(R.id.categoryLabel)
            val categoryIcon = categoryView.findViewById<ImageView>(R.id.categoryIcon)

            categoryLabel.text = category.name
            categoryIcon.setImageResource(getIconForCategory(category))

            categoryView.setOnClickListener {
                toggleCategorySelection(categoryView, category)
            }

            gridLayout.addView(categoryView)
        }
    }

    private fun toggleCategorySelection(view: View, category: HobbyCategory) {
        val isSelected = view.isSelected
        view.isSelected = !isSelected

        // Optional: Change UI to indicate selection state
        if (view.isSelected) {
            view.setBackgroundResource(R.drawable.selected_background)
        } else {
            view.setBackgroundResource(R.drawable.default_background)
        }

        // Handle selected category logic (e.g., add/remove from a list)
        // Example:
        if (view.isSelected) {
            selectedCategories.add(category)
        } else {
            selectedCategories.remove(category)
        }
    }

    private fun getIconForCategory(category: HobbyCategory): Int {
        return when (category) {
            HobbyCategory.ARTS_CRAFTS -> R.drawable.ic_add
            HobbyCategory.OUTDOOR_ACTIVITIES -> R.drawable.ic_back
            HobbyCategory.FOOD_COOKING -> R.drawable.ic_book
            // Add other categories
            HobbyCategory.FITNESS_WELLNESS -> TODO()
            HobbyCategory.TECHNOLOGY_SCIENCE -> TODO()
            HobbyCategory.MUSIC_PERFORMANCE -> TODO()
            HobbyCategory.MISCELLANEOUS -> TODO()
        }
    }
}