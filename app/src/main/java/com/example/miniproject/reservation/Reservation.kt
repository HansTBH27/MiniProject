package com.example.miniproject.reservation

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

data class Reservation(
    @DocumentId
    val reservationID: String = "",
    val availableTime: Timestamp = Timestamp.now(),
    val facilityID: String = "",
    val userID: String = ""
)