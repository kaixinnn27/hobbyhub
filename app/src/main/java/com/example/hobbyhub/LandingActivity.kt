package com.example.hobbyhub

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.hobbyhub.authentication.view.AuthenticationActivity
import com.example.hobbyhub.authentication.viewmodel.AuthViewModel
import com.example.hobbyhub.authentication.viewmodel.UserPreferencesViewModel
import com.example.hobbyhub.databinding.ActivityLandingBinding

class LandingActivity : BaseActivity() {

    private lateinit var binding: ActivityLandingBinding
    private val preferencesVm: UserPreferencesViewModel by viewModels()
    private val authVm: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLandingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Handler().postDelayed({
            navigateToAuthenticationActivity()
        }, 2000)
    }

    // navigation
    private fun navigateToAuthenticationActivity(){
        val intent = Intent(this, AuthenticationActivity::class.java)
        startActivity(intent)
        finish()
    }
}