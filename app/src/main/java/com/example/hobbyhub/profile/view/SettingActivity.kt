package com.example.hobbyhub.profile.view

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.hobbyhub.authentication.model.UserPreferences
import com.example.hobbyhub.authentication.viewmodel.AuthViewModel
import com.example.hobbyhub.authentication.viewmodel.UserPreferencesViewModel
import com.example.hobbyhub.databinding.ActivitySettingBinding
import kotlinx.coroutines.launch

class SettingActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingBinding
    private val authViewModel: AuthViewModel by viewModels()
    private val preferencesViewModel: UserPreferencesViewModel by viewModels()
    private var userId: String? = null
    private var preferences: UserPreferences? = null

    override fun onCreate(savedInstanceState: Bundle?) {
//        applySavedLocale()
        super.onCreate(savedInstanceState)
        binding = ActivitySettingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userId = authViewModel.getCurrentUserId()
        setupFingerprintToggle()
        setupLanguageSelector()
//        updateAppLocale()
    }

    private fun setupFingerprintToggle() {
        if (userId != null) {
            // Load saved preference (example; replace with your method)
            lifecycleScope.launch {
                val preferences = preferencesViewModel.get(userId!!)
                if (preferences != null) {
                    // Temporarily disable listener to avoid redundant triggers
                    binding.switchFingerprint.setOnCheckedChangeListener(null)
                    binding.switchFingerprint.isChecked = preferences.enableFingerprint
                }

                // Set a listener for the toggle
                binding.switchFingerprint.setOnCheckedChangeListener { _, isChecked ->
                    Log.d("Fingerprint Switch", "Fingerprint enabled: $isChecked")
                    val newPreferences = UserPreferences(
                        id = userId!!,
                        enableFingerprint = isChecked
                    )
                    lifecycleScope.launch {
                        val result = preferencesViewModel.update(newPreferences)
                        if (result) {
                            Toast.makeText(
                                this@SettingActivity,
                                if (isChecked) "Fingerprint Enabled" else "Fingerprint Disabled",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            Toast.makeText(
                                this@SettingActivity,
                                "Failed to update fingerprint preference",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            }
        }
    }

    private fun setupLanguageSelector() {
//        val languageSpinner: Spinner = binding.spinnerLanguage
//        val languageOptions = resources.getStringArray(R.array.language_options)
//        val currentLanguage = preferences?.locale ?: "en"
//        val selectedIndex = languageOptions.indexOf(currentLanguage)
//        var isSpinnerInitialSetup = true // Flag to differentiate setup vs user interaction
//
//        // Set initial selection without triggering the listener
//        if (selectedIndex >= 0) {
//            languageSpinner.setSelection(selectedIndex)
//        }
//
//        languageSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
//            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
//                if (isSpinnerInitialSetup) {
//                    isSpinnerInitialSetup = false // First trigger; skip to avoid unnecessary changes
//                    return
//                }
//
//                val selectedLanguage = languageOptions[position]
//                if (selectedLanguage == currentLanguage) {
//                    return // No change; avoid unnecessary update and loop
//                }
//
//                lifecycleScope.launch {
//                    val updatedPreferences = preferences?.copy(locale = selectedLanguage)
//                        ?: UserPreferences(id = userId!!, locale = selectedLanguage)
//
//                    val result = preferencesViewModel.update(updatedPreferences)
//                    if (result) {
//                        Toast.makeText(
//                            this@SettingActivity,
//                            "Language changed to $selectedLanguage",
//                            Toast.LENGTH_SHORT
//                        ).show()
//
//                        // Save the selected language and restart activity
//                        LocaleUtil.applyLocalizedContext(applicationContext, selectedLanguage)
//                        preferences = updatedPreferences // Save updated preferences locally
//                        recreate() // Restart activity to apply locale
//                    } else {
//                        Toast.makeText(
//                            this@SettingActivity,
//                            "Failed to update language preference",
//                            Toast.LENGTH_SHORT
//                        ).show()
//                    }
//                }
//            }
//
//            override fun onNothingSelected(parent: AdapterView<*>?) {
//                // Do nothing
//            }
//        }
    }



//    private fun updateAppLocale() {
//        lifecycleScope.launch {
//            val preferences = userId?.let { preferencesViewModel.get(it) }
//            if (preferences != null) {
//                val savedLocale = preferences.locale
//                val currentLocale = LocaleUtil.getLocaleFromPrefCode(savedLocale)
//
//                // Apply locale only if different from the current system locale
//                if (resources.configuration.locales.get(0) != currentLocale) {
//                    LocaleUtil.applyLocalizedContext(applicationContext, savedLocale)
//                }
//            }
//        }
//    }
}