package com.example.hobbyhub.hobby.view

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.hobbyhub.R
import com.example.hobbyhub.achievement.view.AchievementActivity
import com.example.hobbyhub.achievement.viewmodel.AchievementViewModel
import com.example.hobbyhub.authentication.viewmodel.AuthViewModel
import com.example.hobbyhub.databinding.FragmentHomeBinding
import com.example.hobbyhub.findbuddy.view.MapFragment
import com.example.hobbyhub.hobby.model.Hobby
import com.example.hobbyhub.hobby.model.UserHobby
import com.example.hobbyhub.hobby.viewmodel.HobbyViewModel
import com.example.hobbyhub.hobby.viewmodel.UserHobbyViewModel
import com.example.hobbyhub.utility.toBitmap
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding
    private val nav by lazy { findNavController() }
    private lateinit var horizontalHobbyAdapter: HorizontalHobbyAdapter
    private val authViewModel: AuthViewModel by activityViewModels()
    private val userHobbyViewModel: UserHobbyViewModel by activityViewModels()
    private val hobbyViewModel: HobbyViewModel by activityViewModels()
    private val achievementViewModel: AchievementViewModel by activityViewModels()
    private var searchQuery: String = ""
    private val filteredHobbyList = mutableListOf<Hobby>()
    private lateinit var filteredAdapter: FilteredAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)

        loadMapFragment()
        setupHorizontalAdapter()
        loadUserPhoto()
        setupVerticalAdapter()
        setupStreaks()

        binding.cardViewMap.setOnClickListener {
            nav.navigate(R.id.navigation_find_buddy)
        }

        binding.findBuddyBtn.setOnClickListener {
            nav.navigate(R.id.navigation_find_buddy)
        }

        binding.achievementBtn.setOnClickListener {
            val intent = Intent(context, AchievementActivity::class.java)
            context?.startActivity(intent)
        }

        binding.searchBar.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                searchQuery = s.toString().trim() // Update the current query
                if (searchQuery.isEmpty()) {
                    // No search query, show home content and hide filtered list
                    binding.contentLayout.visibility = View.VISIBLE
                    binding.filteredRecyclerView.visibility = View.GONE
                    binding.noResultsImage.visibility = View.GONE
                    binding.actionbarTv.text = "Home"
                } else {
                    // There is a search query, show filtered results
                    binding.contentLayout.visibility = View.GONE
                    binding.actionbarTv.text = "Search"
                    filterList(searchQuery)
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        return binding.root
    }

    @SuppressLint("SetTextI18n")
    private fun setupStreaks() {
        achievementViewModel.streaks.observe(viewLifecycleOwner) { streakData ->
            Log.d("setupStreaks", "$streakData")
            // Update the UI with streak data (current streak and best streak)
            val currentStreak = streakData["currentValue"] as? Int ?: 0
            val bestStreak = streakData["bestStreak"] as? Int ?: 0

            // Display current streak and best streak in your TextViews
            binding.streakTextView.text = "$currentStreak days"
            binding.bestStreaksTv.text = "$bestStreak days"
        }
    }

    private fun setupVerticalAdapter() {
        filteredAdapter = FilteredAdapter(filteredHobbyList)
        binding.filteredRecyclerView.layoutManager = LinearLayoutManager(context)
        binding.filteredRecyclerView.adapter = filteredAdapter

        hobbyViewModel.userHobbies.observe(viewLifecycleOwner) { places ->
            if (searchQuery.isNotEmpty()) {
                filterList(searchQuery)
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun filterList(query: String) {
        // Filter places based on query
        val filteredItems = hobbyViewModel.userHobbies.value?.filter { hobby ->
            hobby.name.contains(query, ignoreCase = true)
        } ?: listOf()

        // Update filtered list and notify adapter
        filteredHobbyList.clear()
        filteredHobbyList.addAll(filteredItems)
        filteredAdapter.notifyDataSetChanged()

        // Show or hide RecyclerView based on the filtered list
        if (filteredItems.isEmpty()) {
            binding.filteredRecyclerView.visibility = View.GONE
            binding.noResultsImage.visibility = View.VISIBLE
        } else {
            binding.filteredRecyclerView.visibility = View.VISIBLE
            binding.noResultsImage.visibility = View.GONE
        }
    }

    private fun loadUserPhoto() {
        val userId = authViewModel.getCurrentUserId()

        if (!userId.isNullOrBlank()) {
            lifecycleScope.launch {
                val user = authViewModel.get(userId)
                user?.let {
                    if (user.photo.toBitmap() != null) {
                        binding.headerProfile.setImageBitmap(user.photo.toBitmap())
                        binding.letterOverlayTv.visibility = View.GONE
                    } else {
                        binding.headerProfile.setImageResource(R.drawable.profile_bg)
                        binding.letterOverlayTv.visibility = View.VISIBLE

                        val firstLetter = user.name.firstOrNull()?.toString()?.uppercase() ?: "U"
                        binding.letterOverlayTv.text = firstLetter
                    }
                }
            }
        }
    }

    private fun loadMapFragment() {
        val transaction = activity?.supportFragmentManager?.beginTransaction()
        transaction?.replace(R.id.cardViewMap, MapFragment())
        transaction?.disallowAddToBackStack()
        transaction?.commit()
    }

    private fun setupHorizontalAdapter() {
        horizontalHobbyAdapter = HorizontalHobbyAdapter(emptyList())
        binding.hobbiesRv.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        binding.hobbiesRv.adapter = horizontalHobbyAdapter

        val userId = authViewModel.getCurrentUserId()
        Log.d("HomeFragment", "userId -> $userId")
        if (userId != null) {
            lifecycleScope.launch {
                val userHobby: UserHobby? = userHobbyViewModel.get(userId)
                Log.d("HomeFragment", "userHobby -> $userHobby")
                if (userHobby != null && userHobby.preferredCategories.isNotEmpty()) {
                    hobbyViewModel.getHobbiesByCategories(userHobby.preferredCategories)
                }
            }
        }

        hobbyViewModel.userHobbies.observe(viewLifecycleOwner) { hobbies ->
            horizontalHobbyAdapter.updateData(hobbies)
        }
    }
}