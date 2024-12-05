package com.example.hobbyhub.hobby.view

import android.os.Bundle
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
import com.example.hobbyhub.authentication.viewmodel.AuthViewModel
import com.example.hobbyhub.databinding.FragmentHomeBinding
import com.example.hobbyhub.findbuddy.view.ui.FindBuddyFragment
import com.example.hobbyhub.findbuddy.view.ui.MapFragment
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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)

        loadMapFragment()
        setupHorizontalAdapter()
        loadUserPhoto()

        binding.cardViewMap.setOnClickListener {
            nav.navigate(R.id.navigation_find_buddy)
        }

        binding.findBuddyBtn.setOnClickListener {
            nav.navigate(R.id.navigation_find_buddy)
        }

        return binding.root
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
        binding.hobbiesRv.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        binding.hobbiesRv.adapter = horizontalHobbyAdapter

        val userId = authViewModel.getCurrentUserId()
        Log.d("HomeFragment","userId -> $userId")
        if(userId!=null){
            lifecycleScope.launch {
                val userHobby: UserHobby? = userHobbyViewModel.get(userId)
                Log.d("HomeFragment","userHobby -> $userHobby")
                if(userHobby!=null && userHobby.preferredCategories.isNotEmpty()){
                    hobbyViewModel.getHobbiesByCategories(userHobby.preferredCategories)
                }
            }
        }

        hobbyViewModel.userHobbies.observe(viewLifecycleOwner) { hobbies ->
            horizontalHobbyAdapter.updateData(hobbies)
        }
    }
}