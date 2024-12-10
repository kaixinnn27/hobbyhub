package com.example.hobbyhub.authentication.view

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.hobbyhub.MainActivity
import com.example.hobbyhub.R
import com.example.hobbyhub.authentication.model.User
import com.example.hobbyhub.authentication.model.UserPreferences
import com.example.hobbyhub.authentication.viewmodel.AuthViewModel
import com.example.hobbyhub.authentication.viewmodel.UserPreferencesViewModel
import com.example.hobbyhub.databinding.FragmentEnableFingerprintBinding
import com.google.firebase.firestore.toObject
import kotlinx.coroutines.launch

class EnableFingerprintFragment : Fragment() {

    private lateinit var binding: FragmentEnableFingerprintBinding
    private val nav by lazy { findNavController() }
    private val authViewModel: AuthViewModel by activityViewModels()
    private val preferencesViewModel: UserPreferencesViewModel by activityViewModels()
    private var userId: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentEnableFingerprintBinding.inflate(inflater, container, false)

        userId = authViewModel.getCurrentUserId()

        binding.enableFingerprintBtn.setOnClickListener {
            saveFingerprintPreference(true)
            try {
                navigateToMainActivity()
            } catch (e: IllegalArgumentException) {
                println("Caught error: ${e.message}")
                Toast.makeText(
                    requireContext(),
                    e.message,
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        binding.skipBtn.setOnClickListener {
            saveFingerprintPreference(false)
            try {
                navigateToMainActivity()
            } catch (e: IllegalArgumentException) {
                println("Caught error: ${e.message}")
                Toast.makeText(
                    requireContext(),
                    e.message,
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        return binding.root
    }

    private fun saveFingerprintPreference(isEnabled: Boolean) {
        if (userId != null) {
            val preferences = UserPreferences(
                id = userId!!,
                enableFingerprint = isEnabled,
                firstTimeLogin = false,
                locale = "en"
            )
            lifecycleScope.launch {
                val preferencesFromDb = preferencesViewModel.get(userId!!)
                if (preferencesFromDb == null) {
                    val success = preferencesViewModel.set(preferences)
                    if (success) {
                        Toast.makeText(
                            requireContext(),
                            "Fingerprint Enabled",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        Toast.makeText(
                            requireContext(),
                            "Failed to enable fingerprint",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    val updated = preferencesViewModel.update(preferences)
                    if (updated) {
                        Toast.makeText(
                            requireContext(),
                            "Fingerprint Enabled",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        Toast.makeText(
                            requireContext(),
                            "Failed to enable fingerprint",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
    }

    private fun navigateToMainActivity() {
        val userId = authViewModel.getCurrentUserId()

        if (userId == null) {
            throw IllegalArgumentException("UserId not found. Please login again!")
        }

        lifecycleScope.launch {
            val user = authViewModel.get(userId)
            if (user != null) {
                val intent = Intent(context, MainActivity::class.java).apply {
                    putExtra("userId", userId)
                    putExtra("isAdmin", user.admin)
                }
                startActivity(intent)
                requireActivity().finish()
            } else {
                throw IllegalArgumentException("User not found. Please login again!")
            }
        }
    }
}