package com.example.hobbyhub.findbuddy.model

import com.google.android.gms.maps.model.LatLng

data class UserLocation(
    val latLng: LatLng,
    val userId: String
)