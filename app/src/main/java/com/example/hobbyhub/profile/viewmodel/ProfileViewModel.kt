package com.example.hobbyhub.profile.viewmodel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.hobbyhub.authentication.model.User
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.toObject
import com.google.firebase.firestore.toObjects
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class ProfileViewModel : ViewModel() {
    private val col = Firebase.firestore.collection("user")
    private val users = MutableLiveData<List<User>>()
    private lateinit var auth: FirebaseAuth

    suspend fun get(id: String): User? {
        return col.document(id).get().await().toObject<User>()
    }

    suspend fun set(user: User): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                auth = Firebase.auth
                val currentUser = auth.currentUser
                if (currentUser != null) {
                    col.document(currentUser.uid).set(user)
                        .addOnSuccessListener {
                            Log.i("FireStore", "User saved successfully: $user")
                        }
                        .addOnFailureListener { e ->
                            Log.e("FireStore", "Error saving user: $e")
                        }
                        .await()
                }
                true
            } catch (e: Exception) {
                Log.e("FireStore", "FireStore operation failed: $e")
                false
            }
        }
    }

    suspend fun update(user: User): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                auth = Firebase.auth
                val currentUser = auth.currentUser
                if (currentUser != null) {
                    val documentRef = col.document(currentUser.uid)
                    val updates = mutableMapOf<String, Any?>()

                    // Update specific fields of the user document
                    if (user.name.isNotEmpty()) {
                        updates["name"] = user.name
                    }

                    updates["photo"] = user.photo

                    documentRef.update(updates)
                        .addOnSuccessListener {
                            Log.i("FireStore", "User fields updated successfully")
                        }
                        .addOnFailureListener { e ->
                            Log.e("FireStore", "Error updating user fields: $e")
                        }
                        .await()
                }
                true
            } catch (e: Exception) {
                Log.e("FireStore", "Firestore operation failed: $e")
                false
            }
        }
    }

    fun validate(user: User): String {
        var e = ""

        e += if (user.name == "") "- Name is required.\n"
        else if (user.name.length < 3) "- Name is too short (at least 3 letters).\n"
        else ""

        return e
    }
}