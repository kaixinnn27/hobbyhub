package com.example.hobbyhub.profile.view

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.hobbyhub.BaseActivity
import com.example.hobbyhub.authentication.model.UserPreferences
import com.example.hobbyhub.authentication.viewmodel.AuthViewModel
import com.example.hobbyhub.authentication.viewmodel.UserPreferencesViewModel
import com.example.hobbyhub.databinding.ActivitySettingBinding
import com.example.hobbyhub.hobby.view.WriteHobbyRatingActivity
import kotlinx.coroutines.launch

class SettingActivity : BaseActivity() {

    private lateinit var binding: ActivitySettingBinding
    private val authViewModel: AuthViewModel by viewModels()
    private val preferencesViewModel: UserPreferencesViewModel by viewModels()
    private var userId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userId = authViewModel.getCurrentUserId()
        setupFingerprintToggle()
        setupToolbar()

        binding.languagesBtn.setOnClickListener {
            val intent = Intent(this, LocaleActivity::class.java)
            startActivity(intent)
        }

        binding.hobbyCategoryBtn.setOnClickListener {
            val intent = Intent(this, HobbyCategoryActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.toolbar.setNavigationOnClickListener { onBackPressed() }
    }

    private fun setupFingerprintToggle() {
        if (userId != null) {
            lifecycleScope.launch {
                val preferences = preferencesViewModel.get(userId!!)
                if (preferences != null) {
                    binding.switchFingerprint.setOnCheckedChangeListener(null)
                    binding.switchFingerprint.isChecked = preferences.enableFingerprint
                }

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
}
