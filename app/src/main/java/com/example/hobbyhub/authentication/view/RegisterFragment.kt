package com.example.hobbyhub.authentication.view

import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.hobbyhub.R
import com.example.hobbyhub.authentication.model.User
import com.example.hobbyhub.authentication.model.UserPreferences
import com.example.hobbyhub.authentication.viewmodel.AuthViewModel
import com.example.hobbyhub.authentication.viewmodel.UserPreferencesViewModel
import com.example.hobbyhub.databinding.FragmentRegisterBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlin.random.Random

class RegisterFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var binding: FragmentRegisterBinding
    private val nav by lazy { findNavController() }
    private val vm: AuthViewModel by activityViewModels()
    private val preferencesViewModel: UserPreferencesViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentRegisterBinding.inflate(inflater, container, false)

        auth = Firebase.auth

        binding.toLoginBtn.setOnClickListener {
            nav.navigate(R.id.loginFragment)
        }

        binding.registerBtn.setOnClickListener {
            val email = binding.editTextEmail.text.toString()
            val pwd = binding.editTextPassword.text.toString()
            if (validateInputFields()) {
                auth.createUserWithEmailAndPassword(email, pwd)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            try {
                                saveUserToFireStoreDb()
                            } catch (error: IllegalArgumentException) {
                                Log.d("Error saveUserToFireStoreDb", "$error")
                            }
                        } else {
                            // Handle specific Firebase errors
                            val exception = task.exception
                            when (exception) {
                                is FirebaseAuthUserCollisionException -> {
                                    Toast.makeText(
                                        context,
                                        "Email is already registered!",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }

                                else -> {
                                    Toast.makeText(
                                        context,
                                        "Failed to create user: ${exception?.message}",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                            Log.e("FirebaseAuth", "Error creating user", exception)
                        }
                    }
            }
        }
        return binding.root
    }

    private val db = FirebaseFirestore.getInstance()

    private fun saveUserToFireStoreDb() {
        val userId = FirebaseAuth.getInstance().currentUser!!.uid
        val name = binding.editTextName.text.toString().trim()
        val email = binding.editTextEmail.text.toString().trim()

        val user = User(
            id = userId,
            name = name,
            email = email,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
            admin = false
        )

        lifecycleScope.launch {
            val success: Boolean = vm.set(user)
            if (success) {
                initPreferences()
                val (latitude, longitude) = generateRandomCoordinates()
                val locationData = hashMapOf(
                    "latitude" to latitude,
                    "longitude" to longitude
                )
                db.collection("location").document(userId)
                    .set(locationData)
                    .addOnSuccessListener {
                        // Clear input fields
                        binding.editTextName.text.clear()
                        binding.editTextEmail.text.clear()
                        binding.editTextPassword.text.clear()

                        // Navigate to UserDemographicFragment
                        Toast.makeText(context, "Account created successfully!", Toast.LENGTH_SHORT)
                            .show()
                        Log.d(
                            "NavigationDebug",
                            "Expected: ${R.id.registerFragment}, Current: ${findNavController().currentDestination?.id}"
                        )
                        Log.d("NavigationGraphDebug", findNavController().graph.toString())
                        findNavController().navigate(
                            RegisterFragmentDirections.actionRegisterFragmentToUserDemographicsFragment()
                        )
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(
                            context,
                            "Failed to save location data: ${e.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
            } else {
                Toast.makeText(context, "Failed to save user!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun initPreferences() {
        val userId = vm.getCurrentUserId()
        if (userId != null) {
            val preferences = UserPreferences(
                id= userId,
                enableFingerprint = false,
                firstTimeLogin = true,
                locale = "en"
            )
            lifecycleScope.launch {
                val success = preferencesViewModel.set(preferences)
            }
        }
    }

    private fun generateRandomCoordinates(): Pair<Double, Double> {
        // Define latitude and longitude bounds for Kuala Lumpur
        val klLatitudeMin = 3.0
        val klLatitudeMax = 3.3
        val klLongitudeMin = 101.5
        val klLongitudeMax = 101.8

        // Generate random latitude within the bounds of Kuala Lumpur
        val latitude = Random.nextDouble(klLatitudeMin, klLatitudeMax)

        // Generate random longitude within the bounds of Kuala Lumpur
        val longitude = Random.nextDouble(klLongitudeMin, klLongitudeMax)

        return Pair(latitude, longitude)
    }

    private fun validateInputFields(): Boolean {
        val name = binding.editTextName.text.toString()
        val email = binding.editTextEmail.text.toString()
        val pwd = binding.editTextPassword.text.toString()

        if (name == "") {
            binding.editTextName.error = "This field is required!"
            return false
        }
        if (email == "") {
            binding.editTextEmail.error = "This field is required!"
            return false
        }
//        if (!isValidTarumtEmail(email)) {
//            binding.editTextEmail.error = "Invalid email format/please use TARUMT email"
//            return false
//        }
        if (pwd == "") {
            binding.editTextPassword.error = "This field is required!"
            return false
        }
        if (binding.editTextPassword.length() < 8) {
            binding.editTextPassword.error = "Password should at least 8 characters long!"
            return false
        }
        return true
    }

    private fun isValidTarumtEmail(email: String): Boolean {
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            return false
        }
        if (!email.contains("@student.tarc.edu.my")) {
            return false
        }
        return true
    }
}