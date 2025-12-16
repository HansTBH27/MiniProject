package com.example.miniproject.equipment

import com.google.firebase.firestore.DocumentId

data class Equipment(
    @DocumentId
    val id: String = "",
    val name: String = "",
    val price: Double = 0.0,
    val quantity: Int = 0,
    val facilityID: String = ""
)