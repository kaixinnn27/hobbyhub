package com.example.hobbyhub.authentication.model

import com.google.firebase.firestore.Blob
import com.google.firebase.firestore.DocumentId

data class User(
    @DocumentId
    var id: String = "",
    var name: String = "",
    var email: String = "",
    var photo: Blob = Blob.fromBytes(ByteArray(0)),
    var createdAt: String = "",
    var updatedAt: String = ""
)