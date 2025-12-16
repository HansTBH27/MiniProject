package com.example.miniproject.payment

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

// Represents a single line item, stored in its own collection.
data class PaymentDetail(
    @DocumentId
    val id: String = "", // The unique ID of this detail item
    val paymentId: String = "", // Foreign Key to the main Payment
    val equipmentId: String = "",
    val quantityRented: Int = 0
)

// Represents the main payment document.
data class Payment(
    @DocumentId
    val id: String = "",
    val date: Timestamp = Timestamp.now(),
    val totalAmount: Double = 0.0,
    val reservationID: String = "" // Foreign Key to Reservation
)
