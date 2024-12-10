package com.example.hobbyhub.authentication.model

import com.google.firebase.firestore.Blob
import com.google.firebase.firestore.DocumentId

data class UserPreferences(
    @DocumentId
    var id: String = "",
    var enableFingerprint: Boolean = false,
    var firstTimeLogin: Boolean = true,
    var locale : String = "en",
)