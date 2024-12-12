package com.example.hobbyhub.authentication.view

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.hobbyhub.MainActivity
import com.example.hobbyhub.R
import com.example.hobbyhub.authentication.model.User
import com.example.hobbyhub.authentication.viewmodel.AuthViewModel
import com.example.hobbyhub.authentication.viewmodel.UserPreferencesViewModel
import com.example.hobbyhub.databinding.FragmentLoginBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.toObject
import kotlinx.coroutines.launch

class LoginFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var binding: FragmentLoginBinding
    private val nav by lazy { findNavController() }
    private val user = Firebase.firestore.collection("user")
    private val preferencesViewModel: UserPreferencesViewModel by activityViewModels()
    private val authViewModel: AuthViewModel by activityViewModels()
    private var userId: String? = null
    private var isFingerprintEnabled: Boolean = false
    private var isFirstTimeLogin: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLoginBinding.inflate(inflater, container, false)

        auth = Firebase.auth
        checkFingerprintAndAuthenticate()

        binding.toRegisterBtn.setOnClickListener {
            nav.navigate(R.id.registerFragment)
        }

        binding.resetForgetPwdBtn.setOnClickListener {
            nav.navigate(R.id.forgotPasswordFragment)
        }

        binding.loginBtn.setOnClickListener {
            val email = binding.editTextEmail.text.toString()
            val pwd = binding.editTextPassword.text.toString()
            if (validateInputFields()) {
                auth.signInWithEmailAndPassword(email, pwd).addOnSuccessListener { authResult ->
                    // Fetch user data from FireStore based on the authenticated user's email
                    fetchUserAndNavigate(authResult.user?.uid ?: "")
                }.addOnFailureListener { exception ->
                    Toast.makeText(
                        requireContext(),
                        "Login failed: ${exception.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

        return binding.root
    }

    private fun fetchUserAndNavigate(userId: String) {
        // because after register user is dont have
        if (isFirstTimeLogin) {
            // If first login then let them choose enable fingerprint
            nav.navigate(R.id.enableFingerprintFragment)
        } else {
            user.document(userId).get().addOnSuccessListener { documentSnapshot ->
                val user = documentSnapshot.toObject<User>()
                if (user != null) {
                    val isAdmin = when (val adminValue = documentSnapshot.get("admin")) {
                        is Boolean -> adminValue // If already Boolean
                        is String -> adminValue.toBoolean() // If stored as String
                        else -> false // Default to false if missing or unknown type
                    }
                    val intent = Intent(context, MainActivity::class.java).apply {
                        putExtra("userId", userId) // Pass user ID to MainActivity
                        putExtra("isAdmin", isAdmin) // Pass admin status to MainActivity
                    }
                    startActivity(intent)
                    requireActivity().finish()
                } else {
                    Toast.makeText(
                        requireContext(),
                        "User data not found",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }.addOnFailureListener { exception ->
                Toast.makeText(
                    requireContext(),
                    "Failed to fetch user data: ${exception.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun validateInputFields(): Boolean {
        val email = binding.editTextEmail.text.toString()
        val pwd = binding.editTextPassword.text.toString()
        if (email.isBlank() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.editTextEmail.error = "Invalid email format!"
            return false
        }
        if (pwd.length < 8) {
            binding.editTextPassword.error = "Password should be at least 8 characters long!"
            return false
        }
        return true
    }

    private fun checkFingerprintAndAuthenticate() {
        val currentUser = auth.currentUser
        userId = authViewModel.getCurrentUserId()
        if (userId != null) {
            lifecycleScope.launch {
                val preferences = preferencesViewModel.get(userId!!)
                Log.d("checkFingerprintAndAuthenticate", "$preferences")
                isFingerprintEnabled = preferences?.enableFingerprint ?: false
                isFirstTimeLogin = preferences?.firstTimeLogin ?: true

                Log.d("LoginFragment", "preferences -> $preferences")
                Log.d("LoginFragment", "isFirstTimeLogin -> $isFirstTimeLogin")

                if (currentUser != null && isFingerprintEnabled) {
                    val biometricManager = BiometricManager.from(requireContext())
                    if (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)
                        == BiometricManager.BIOMETRIC_SUCCESS
                    ) {
                        showFingerprintPrompt()
                    } else {
                        Toast.makeText(
                            requireContext(),
                            "Biometric authentication not supported.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
    }

    private fun showFingerprintPrompt() {
        val executor = ContextCompat.getMainExecutor(requireContext())
        val biometricPrompt = BiometricPrompt(
            this,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    AlertDialog.Builder(requireContext())
                        .setTitle("Authentication Error")
                        .setMessage(errString)
                        .setPositiveButton("Retry") { _, _ -> showFingerprintPrompt() }
                        .setNegativeButton("Cancel") { _, _ ->
                            Toast.makeText(
                                requireContext(),
                                "Authentication required. Please log in manually.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        .show()
                }

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    navigateToMainActivity()
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    Toast.makeText(
                        requireContext(),
                        "Authentication failed, please log in with email and password.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        )

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Biometric Login")
            .setSubtitle("Log in using your biometric credential")
            .setDescription("Use your fingerprint to log in securely.")
            .setNegativeButtonText("Cancel")
            .build()

        biometricPrompt.authenticate(promptInfo)
    }

    private fun navigateToMainActivity() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            fetchUserAndNavigate(currentUser.uid)
        }
    }
}