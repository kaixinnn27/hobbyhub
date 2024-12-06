package com.example.hobbyhub.hobby.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hobbyhub.authentication.model.User
import com.example.hobbyhub.hobby.model.Hobby
import com.example.hobbyhub.hobby.model.HobbyCategory
import com.example.hobbyhub.hobby.model.HobbyData
import com.example.hobbyhub.hobby.model.UserHobby
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.toObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class UserHobbyViewModel : ViewModel() {

    private val col = Firebase.firestore.collection("userHobbies")
    private val userHobbies = MutableLiveData<List<UserHobby>>()
    private val savedHobbies = MutableLiveData<List<UserHobby>>()
    private val _isFavorite = MutableLiveData<Boolean>()
    val isFavorite: LiveData<Boolean> get() = _isFavorite

    // get UserHobby by userId
    suspend fun get(id: String): UserHobby? {
        return col.document(id).get().await().toObject<UserHobby>()
    }

    suspend fun set(userHobby: UserHobby): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                col.document(userHobby.id).set(userHobby)
                    .addOnSuccessListener {
                        Log.i("FireStore", "UserHobby saved successfully: $userHobby")
                    }
                    .addOnFailureListener { e ->
                        Log.e("FireStore", "Error saving userHobby: $e")
                    }
                    .await()
                true
            } catch (e: Exception) {
                Log.e("FireStore", "FireStore operation failed: $e")
                false
            }
        }
    }

    suspend fun addFavorite(userId: String, hobbyId: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                // Get the current document
                val userHobby = col.document(userId).get().await()

                if (userHobby.exists()) {
                    // Extract the current savedHobbies list
                    val currentSavedHobbies = userHobby.get("savedHobbies") as? List<String> ?: emptyList()

                    // Check if the hobbyId is already in the list
                    if (!currentSavedHobbies.contains(hobbyId)) {
                        // Add the hobbyId to the list and update the field
                        val updatedSavedHobbies = currentSavedHobbies + hobbyId
                        col.document(userId).update("savedHobbies", updatedSavedHobbies).await()
                        Log.i("FireStore", "Hobby ID added to savedHobbies successfully")
                    } else {
                        Log.i("FireStore", "Hobby ID already exists in savedHobbies")
                    }
                    _isFavorite.postValue(true)
                    true
                } else {
                    Log.e("FireStore", "UserHobby document does not exist for userId: $userId")
                    false
                }
            } catch (e: Exception) {
                Log.e("FireStore", "Failed to add hobby ID to savedHobbies: $e")
                false
            }
        }
    }

    suspend fun removeFavorite(userId: String, hobbyId: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                // Fetch the current document
                val userHobbyDocument = col.document(userId).get().await()

                if (userHobbyDocument.exists()) {
                    // Extract the current savedHobbies list
                    val currentSavedHobbies = userHobbyDocument.get("savedHobbies") as? List<String> ?: emptyList()

                    // Check if the hobbyId exists in the list
                    if (currentSavedHobbies.contains(hobbyId)) {
                        // Remove the hobbyId and update the field
                        val updatedSavedHobbies = currentSavedHobbies.filter { it != hobbyId }
                        col.document(userId).update("savedHobbies", updatedSavedHobbies).await()
                        Log.i("FireStore", "Hobby ID removed from savedHobbies successfully")
                    } else {
                        Log.i("FireStore", "Hobby ID not found in savedHobbies")
                    }
                    _isFavorite.postValue(false)
                    true
                } else {
                    Log.e("FireStore", "UserHobby document does not exist for userId: $userId")
                    false
                }
            } catch (e: Exception) {
                Log.e("FireStore", "Failed to remove hobby ID from savedHobbies: $e")
                false
            }
        }
    }
    fun isFavorite(userId: String, hobbyId: String) {
        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) {
                try {
                    val userHobbyDocument = col.document(userId).get().await()
                    if (userHobbyDocument.exists()) {
                        val currentSavedHobbies = userHobbyDocument.get("savedHobbies") as? List<String> ?: emptyList()
                        currentSavedHobbies.contains(hobbyId)
                    } else {
                        false
                    }
                } catch (e: Exception) {
                    Log.e("FireStore", "Error checking if favorite: $e")
                    false
                }
            }
            _isFavorite.postValue(result)
        }
    }
}
