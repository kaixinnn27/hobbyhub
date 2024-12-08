package com.example.hobbyhub.authentication.viewmodel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.hobbyhub.authentication.model.User
import com.example.hobbyhub.chatroom.model.Friend
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.Blob
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.toObject
import com.google.firebase.firestore.toObjects
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class AuthViewModel : ViewModel() {
    private val col = Firebase.firestore.collection("user")
    private val users = MutableLiveData<List<User>>()

    suspend fun get(id: String): User? {
        return col.document(id).get().await().toObject<User>()
    }

    suspend fun getFriendSize(id: String): Int {
        return try {
            val documentSnapshot = col.document(id).get().await()
            val userFriends = documentSnapshot["friends"] as? List<String> ?: emptyList()
            userFriends.size
        } catch (e: Exception) {
            Log.e("AuthViewModel", "Error fetching friend details: $e")
            0
        }
    }

    suspend fun getFriendsLeaderboard(): List<User> {
        return withContext(Dispatchers.IO) {
            try {
                // Fetch all users
                val querySnapshot = col.get().await()
                val users = querySnapshot.toObjects<User>()

                // Sort by streaks and friends
                val topFriends = users.sortedByDescending { it.friends.size }.take(3)

                topFriends
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Error fetching leaderboard data: $e")
                emptyList()
            }
        }
    }

    fun getCurrentUserId(): String? {
        val firebaseUser = FirebaseAuth.getInstance().currentUser
        return firebaseUser?.uid
    }

    fun signOut() {
        FirebaseAuth.getInstance().signOut()
    }

    suspend fun getUserByEmail(email: String): User? {
        return withContext(Dispatchers.IO) {
            try {
                val querySnapshot = col.whereEqualTo("email", email).limit(1).get().await()

                val document = querySnapshot.documents.firstOrNull()
                if (document != null && document.exists()) {
                    val user = document.toObject<User>()
                    user?.id = document.id
                    user
                } else {
                    null
                }
            } catch (e: Exception) {
                Log.e("FireStore", "Error fetching user by email: $e")
                null
            }
        }
    }

    suspend fun set(user: User): Boolean {
        return try {
            col.document(user.id).set(user).await()  // Directly use await() here
            Log.i("FireStore", "User saved successfully: $user")
            true
        } catch (e: Exception) {
            Log.e("FireStore", "Error saving user: $e")
            false
        }
    }
}