package com.example.hobbyhub.hobby.viewmodel

import android.util.Log
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
}
