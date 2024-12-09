package com.example.hobbyhub.authentication.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.hobbyhub.R
import com.example.hobbyhub.databinding.FragmentUserDemographicsBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class UserDemographicsFragment : Fragment() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private lateinit var binding: FragmentUserDemographicsBinding
    private val nav by lazy { findNavController() }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentUserDemographicsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnSubmitDemographics.setOnClickListener {
            val age = binding.editTextAge.text.toString().toIntOrNull()
            val gender = binding.editTextGender.text.toString().trim()
            val location = binding.editTextLocation.text.toString().trim()

            if (age == null || gender.isEmpty() || location.isEmpty()) {
                Toast.makeText(context, "All fields are required!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            saveDemographics(age, gender, location)
        }
    }

    private fun saveDemographics(age: Int, gender: String, location: String) {
        val userId = auth.currentUser?.uid ?: return

        val demographicData = hashMapOf(
            "age" to age,
            "gender" to gender,
            "location" to location
        )

        db.collection("demographics").document(userId)
            .set(demographicData)
            .addOnSuccessListener {
                Toast.makeText(context, "Demographics saved successfully!", Toast.LENGTH_SHORT).show()
                nav.navigate(R.id.selectHobbyFragment) // Navigate to Hobby Selection
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Failed to save demographics: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
