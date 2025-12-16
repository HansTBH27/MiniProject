package com.example.miniproject.payment

import com.example.miniproject.auth.FirebaseManager
import com.google.firebase.firestore.toObject
import kotlinx.coroutines.tasks.await

class PaymentRepository {

    private val paymentsCollection = FirebaseManager.firestore.collection("payments")

    /**
     * Creates a new payment document using the ID provided within the payment object.
     * It is your responsibility to ensure payment.id is unique.
     * This will overwrite any existing document with the same ID.
     */
    suspend fun createPayment(payment: Payment) {
        paymentsCollection.document(payment.id).set(payment).await()
    }

    // Function to get a single payment by its ID
    suspend fun getPayment(id: String): Payment? {
        val documentSnapshot = paymentsCollection.document(id).get().await()
        return documentSnapshot.toObject<Payment>()
    }

    // Function to get all payments (e.g., for a user or for admin)
    suspend fun getAllPayments(): List<Payment> {
        val querySnapshot = paymentsCollection.get().await()
        return querySnapshot.documents.mapNotNull { it.toObject<Payment>() }
    }

    // You can add more specific queries here, like getting all payments for a certain reservationId
}
