package com.example.hobbyhub.usermanagement.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.hobbyhub.authentication.model.User
import com.google.firebase.Firebase
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.toObject
import kotlinx.coroutines.tasks.await

class UserManagementViewModel : ViewModel() {

    // userId as documentId
    private val userCol = Firebase.firestore.collection("user")
    private val achievementsCol = Firebase.firestore.collection("achievements")
    private val chatsCol = Firebase.firestore.collection("chats")
    private val demographicsCol = Firebase.firestore.collection("demographics")
    private val locationCol = Firebase.firestore.collection("location")
    private val postsCol = Firebase.firestore.collection("posts")
    private val scheduleCol = Firebase.firestore.collection("schedule")
    private val userHobbiesCol = Firebase.firestore.collection("userHobbies")
    private val userPreferencesCol = Firebase.firestore.collection("userPreferences")

    // auto generated id
    private val groupsCol = Firebase.firestore.collection("groups")
    private val hobbiesCol = Firebase.firestore.collection("hobbies")

    // auto generated id + inside the fields have userId to map
    private val reviewsCol = Firebase.firestore.collection("reviews")

    suspend fun getUserById(id: String): User? {
        return userCol.document(id).get().await().toObject<User>()
    }

    suspend fun deleteUser(userId: String) {
        try {
            // Delete user document
            userCol.document(userId).delete().await()

            // Delete achievements
            deleteCollectionDocuments(achievementsCol, userId)

            // Delete chats
            deleteCollectionDocuments(chatsCol, userId)

            // Delete demographics
            deleteCollectionDocuments(demographicsCol, userId)

            // Delete location
            deleteCollectionDocuments(locationCol, userId)

            // Delete posts
            deleteCollectionDocuments(postsCol, userId)

            // Delete schedule
            deleteCollectionDocuments(scheduleCol, userId)

            // Delete user hobbies
            deleteCollectionDocuments(userHobbiesCol, userId)

            // Delete user preferences
            deleteCollectionDocuments(userPreferencesCol, userId)

            // Delete reviews where userId is a field
            val reviewsSnapshot = reviewsCol.whereEqualTo("userId", userId).get().await()
            for (document in reviewsSnapshot) {
                reviewsCol.document(document.id).delete().await()
            }

        } catch (e: Exception) {
            // Log or handle the exception as needed
            e.printStackTrace()
        }
    }

    private suspend fun deleteCollectionDocuments(collection: CollectionReference, userId: String) {
        try {
            val snapshot = collection.whereEqualTo("userId", userId).get().await()
            for (document in snapshot.documents) {
                collection.document(document.id).delete().await()
            }
        } catch (e: Exception) {
            // Log or handle the exception as needed
            e.printStackTrace()
        }
    }

    suspend fun getAllUsers(): List<Map<String, Any>> {
        return try {
            val snapshot = userCol.get().await()
            snapshot.documents.mapNotNull { it.data }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}