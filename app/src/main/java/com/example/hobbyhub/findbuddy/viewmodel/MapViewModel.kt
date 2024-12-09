package com.example.hobbyhub.findbuddy.viewmodel

import androidx.lifecycle.ViewModel
import com.example.hobbyhub.findbuddy.model.UserLocation
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.google.android.gms.maps.model.LatLng

class MapViewModel : ViewModel() {

    private val locationCol = Firebase.firestore.collection("location")
    private val userCol = Firebase.firestore.collection("user")

    fun getUsersLocations(): MutableList<UserLocation> {
        val userLocations = mutableListOf<UserLocation>()
        locationCol.get()
            .addOnSuccessListener { querySnapshot ->
                querySnapshot.documents.forEach { document ->
                    val latitude = document.getDouble("latitude") ?: 0.0
                    val longitude = document.getDouble("longitude") ?: 0.0
                    val userId = document.id

                    userLocations.add(UserLocation(LatLng(latitude, longitude), userId))
                }
            }
            .addOnFailureListener { exception ->
                // Handle failure if necessary
            }
        return userLocations
    }
}