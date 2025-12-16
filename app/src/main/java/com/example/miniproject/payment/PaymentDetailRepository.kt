package com.example.miniproject.payment

import com.example.miniproject.auth.FirebaseManager
import com.google.firebase.firestore.toObject
import kotlinx.coroutines.tasks.await

class PaymentDetailRepository {

    private val detailsCollection = FirebaseManager.firestore.collection("paymentdetails")

    // Function to add a single payment detail item
    suspend fun addPaymentDetail(detail: PaymentDetail) {
        detailsCollection.add(detail).await()
    }

    // CRITICAL: Function to get all detail items for a specific payment
    suspend fun getDetailsForPayment(paymentId: String): List<PaymentDetail> {
        // Updated to use the correct "paymentID" field name
        val querySnapshot = detailsCollection.whereEqualTo("paymentID", paymentId).get().await()
        return querySnapshot.documents.mapNotNull { it.toObject<PaymentDetail>() }
    }

    // Function to get all payment details (less common, but can be useful)
    suspend fun getAllPaymentDetails(): List<PaymentDetail> {
        val querySnapshot = detailsCollection.get().await()
        return querySnapshot.documents.mapNotNull { it.toObject<PaymentDetail>() }
    }
}
