package com.example.hobbyhub

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.hobbyhub.authentication.model.User
import com.example.hobbyhub.authentication.view.AuthenticationActivity
import com.example.hobbyhub.databinding.ActivityLandingBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore

class LandingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLandingBinding
    private lateinit var auth: FirebaseAuth
    private val db = FirebaseFirestore.getInstance()
    private val usersCollection = db.collection("user")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLandingBinding.inflate(layoutInflater)
        auth = Firebase.auth
        setContentView(binding.root)

        Handler().postDelayed({
            val currentUser = auth.currentUser
            if (currentUser != null) {
                getUserByUid(currentUser.uid) { user ->
                    if (user != null) {
                        Log.d("LandingActivity", "User found: ${user.email}")
                        // 'admin' is guaranteed to be Boolean
                        navigateToMainActivity(user.id, user.admin)
                    } else {
                        Log.d("LandingActivity", "User document not found, navigating to AuthenticationActivity")
                        navigateToAuthenticationActivity()
                    }
                }
            } else {
                Log.d("LandingActivity", "No current user, navigating to AuthenticationActivity")
                navigateToAuthenticationActivity()
            }
        }, 2000)
    }


    private fun navigateToMainActivity(userId: String, isAdmin: Boolean) {
        val intent = Intent(this, MainActivity::class.java).apply {
            putExtra("userId", userId)
            putExtra("isAdmin", isAdmin)
        }
        startActivity(intent)
        finish()
    }

    // navigation
    private fun navigateToAuthenticationActivity(){
        val intent = Intent(this, AuthenticationActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun getUserByUid(uid: String, callback: (User?) -> Unit) {
        usersCollection.document(uid)
            .get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    val user = documentSnapshot.toObject(User::class.java)
                    if (user != null) {
                        // Handle 'admin' field explicitly as Boolean
                        user.admin = documentSnapshot.getBoolean("admin") ?: false
                        user.id = documentSnapshot.id
                    }
                    callback(user)
                } else {
                    callback(null) // User document not found
                }
            }
            .addOnFailureListener { e ->
                Log.e("LandingActivity", "Error fetching user by UID", e)
                callback(null)
            }
    }
}