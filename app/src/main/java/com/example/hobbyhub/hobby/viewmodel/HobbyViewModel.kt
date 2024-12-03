package com.example.hobbyhub.hobby.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hobbyhub.hobby.model.Hobby
import com.example.hobbyhub.hobby.model.HobbyCategory
import com.example.hobbyhub.hobby.model.HobbyData
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.toObject
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class HobbyViewModel : ViewModel() {

    private val col = Firebase.firestore.collection("hobbies")
    private val hobbies = MutableLiveData<List<Hobby>>()

    suspend fun get(id: String): Hobby? {
        return col.document(id).get().await().toObject<Hobby>()
    }

    fun getAllHobbies() {
        viewModelScope.launch {
            try {
                val result = col.get().await()
                val hobbyList = result.documents.mapNotNull { it.toObject<Hobby>() }
                hobbies.postValue(hobbyList)
            } catch (e: Exception) {
                println("Error fetching hobbies: ${e.message}")
            }
        }
    }

    suspend fun getHobbyById(id: String): Hobby? {
        return try {
            col.document(id).get().await().toObject<Hobby>()
        } catch (e: Exception) {
            println("Error fetching hobby by ID ($id): ${e.message}")
            null
        }
    }

    fun getHobbyCategories(): List<HobbyCategory> {
        return HobbyCategory.entries
    }

    fun insertDummyHobbies() {
        viewModelScope.launch {
            HobbyData.hobbies.forEach { hobby ->
                col.document()
                    .set(hobby)
                    .addOnSuccessListener {
                        println("Successfully added hobby: ${hobby.name}")
                    }
                    .addOnFailureListener { e ->
                        println("Error adding hobby: ${e.message}")
                    }
            }
        }
    }
}
