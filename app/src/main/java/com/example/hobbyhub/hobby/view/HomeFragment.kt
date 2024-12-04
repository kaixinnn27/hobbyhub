package com.example.hobbyhub.hobby.view

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.hobbyhub.R
import com.example.hobbyhub.authentication.viewmodel.AuthViewModel
import com.example.hobbyhub.databinding.FragmentHomeBinding
import com.example.hobbyhub.findbuddy.view.ui.FindBuddyFragment
import com.example.hobbyhub.hobby.model.HobbyCategory
import com.example.hobbyhub.hobby.model.UserHobby
import com.example.hobbyhub.hobby.viewmodel.HobbyViewModel
import com.example.hobbyhub.hobby.viewmodel.UserHobbyViewModel
import com.google.android.gms.maps.MapFragment
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding
    private val nav by lazy { findNavController() }
    private lateinit var horizontalHobbyAdapter: HorizontalHobbyAdapter
    private lateinit var authViewModel: AuthViewModel
    private lateinit var userHobbyViewModel: UserHobbyViewModel
    private lateinit var hobbyViewModel: HobbyViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)

        loadMapFragment()
        setupHorizontalAdapter()

        binding.cardViewMap.setOnClickListener {
            nav.navigate(R.id.navigation_find_buddy)
        }

        binding.findBuddyBtn.setOnClickListener {
            nav.navigate(R.id.navigation_find_buddy)
        }

        return binding.root
    }

    private fun loadMapFragment() {
        val transaction = activity?.supportFragmentManager?.beginTransaction()
        transaction?.replace(R.id.cardViewMap, FindBuddyFragment())
        transaction?.disallowAddToBackStack()
        transaction?.commit()
    }

    private fun setupHorizontalAdapter() {
        horizontalHobbyAdapter = HorizontalHobbyAdapter(emptyList())
        binding.hobbiesRv.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        binding.hobbiesRv.adapter = horizontalHobbyAdapter

//        val userId = authViewModel.getCurrentUserId()
//        if(userId!=null){
//            lifecycleScope.launch {
//                val userHobby: UserHobby? = userHobbyViewModel.get(userId)
//                if(userHobby!=null && userHobby.preferredCategories.isNotEmpty()){
//                    hobbyViewModel.getHobbiesByCategories(userHobby.preferredCategories)
//                }
//            }
//        }
//
//        hobbyViewModel.userHobbies.observe(viewLifecycleOwner) { hobbies ->
//            horizontalHobbyAdapter.updateData(hobbies)
//        }
    }
}