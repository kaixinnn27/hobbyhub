package com.example.hobbyhub.authentication.viewmodel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.hobbyhub.authentication.model.UserPreferences
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.toObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class UserPreferencesViewModel : ViewModel() {
    private val col = Firebase.firestore.collection("userPreferences")
    private val userPreferences = MutableLiveData<List<UserPreferences>>()

    suspend fun get(id: String): UserPreferences? {
        return col.document(id).get().await().toObject<UserPreferences>()
    }

    suspend fun set(userPreferences: UserPreferences): Boolean {
        return try {
            col.document(userPreferences.id).set(userPreferences)
                .await()  // Directly use await() here
            Log.i("FireStore", "UserPreferences saved successfully: $userPreferences")
            true
        } catch (e: Exception) {
            Log.e("FireStore", "Error saving UserPreferences: $e")
            false
        }
    }

    suspend fun update(userPreferences: UserPreferences): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val documentRef = col.document(userPreferences.id)
                val updates = mutableMapOf<String, Any?>()

                updates["enableFingerprint"] = userPreferences.enableFingerprint
                updates["locale"] = userPreferences.locale

                documentRef.update(updates)
                    .addOnSuccessListener {
                        Log.i("FireStore", "User fields updated successfully")
                    }
                    .addOnFailureListener { e ->
                        Log.e("FireStore", "Error updating user fields: $e")
                    }
                    .await()
                true
            } catch (e: Exception) {
                Log.e("FireStore", "Firestore operation failed: $e")
                false
            }
        }
    }
}