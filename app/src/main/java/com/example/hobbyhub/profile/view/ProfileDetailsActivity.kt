package com.example.hobbyhub.profile.view

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.hobbyhub.BaseActivity
import com.example.hobbyhub.R
import com.example.hobbyhub.authentication.model.User
import com.example.hobbyhub.authentication.viewmodel.AuthViewModel
import com.example.hobbyhub.databinding.ActivityProfileDetailsBinding
import com.example.hobbyhub.profile.viewmodel.ProfileViewModel
import com.example.hobbyhub.utility.cropToBlob
import com.example.hobbyhub.utility.toBitmap
import kotlinx.coroutines.launch

class ProfileDetailsActivity : BaseActivity() {

    private lateinit var binding: ActivityProfileDetailsBinding
    private val vm: ProfileViewModel by viewModels()
    private val authVm: AuthViewModel by viewModels()

    private val launcher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {
                binding.imgProfile.setImageURI(it.data?.data)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupToolbar()

        binding.cancelBtn.setOnClickListener {
            onBackPressed()
        }

        binding.imgProfileBtn.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "image/*"
            launcher.launch(intent)
        }

        binding.saveBtn.setOnClickListener {
            submit()
        }

        val userId = authVm.getCurrentUserId()
        if (userId != null) {
            lifecycleScope.launch {
                val user = vm.get(userId)
                user?.let { populateUserData(it) }
            }
        }
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.toolbar.setNavigationOnClickListener { onBackPressed() }
    }

    private fun submit() {
        val user = User(
            name = binding.editTextName.text.toString().trim(),
            photo = binding.imgProfile.cropToBlob(300, 300),
        )

        lifecycleScope.launch {
            val err = vm.validate(user)
            if (err.isNotEmpty()) {
                AlertDialog.Builder(this@ProfileDetailsActivity)
                    .setIcon(R.drawable.ic_error)
                    .setTitle("Error")
                    .setMessage(err)
                    .setPositiveButton("Dismiss", null)
                    .show()
                return@launch
            }

            // Update user fields in Firestore
            val updated = vm.update(user)
            if (updated) {
                Toast.makeText(
                    this@ProfileDetailsActivity,
                    "Profile updated successfully",
                    Toast.LENGTH_SHORT
                ).show()
                onBackPressed() // Navigate back
            } else {
                Toast.makeText(
                    this@ProfileDetailsActivity,
                    "Failed to update profile",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun populateUserData(user: User) {
        with(binding) {
            editTextName.setText(user.name)
            editTextEmail.setText(user.email)
            if (user.photo.toBitmap() != null) {
                // Set the user's photo if it's not null
                binding.imgProfile.setImageBitmap(user.photo.toBitmap())
            }
            else{
                binding.imgProfile.setImageResource(R.drawable.profile)
            }
        }
    }
}