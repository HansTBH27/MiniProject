package com.example.miniproject.payment

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

// Represents a single line item on the payment.
data class PaymentDetailItem( // Renamed for clarity
    val equipmentId: String = "",
    val quantityRented: Int = 0
)

// Represents the main payment document.
data class Payment(
    @DocumentId
    val paymentId: String = "",
    val paymentDate: Timestamp = Timestamp.now(),
    val totalAmount: Double = 0.0,
    val reservationId: String = "", // Foreign Key to Reservation

    // Embed the list of details here for efficiency
    val details: List<PaymentDetailItem> = emptyList()
)
