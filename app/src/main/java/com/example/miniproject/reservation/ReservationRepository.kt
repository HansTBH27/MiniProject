package com.example.miniproject.reservation

import com.example.miniproject.auth.FirebaseManager
import com.google.firebase.firestore.toObject
import kotlinx.coroutines.tasks.await

class ReservationRepository {

    private val reservationsCollection = FirebaseManager.firestore.collection("reservations")

    // Function to add a new reservation to Firestore
    suspend fun addReservation(reservation: Reservation) {
        reservationsCollection.add(reservation).await()
    }

    // Function to get a single reservation by its ID
    suspend fun getReservation(id: String): Reservation? {
        val documentSnapshot = reservationsCollection.document(id).get().await()
        return documentSnapshot.toObject<Reservation>()
    }

    // Function to get all reservations from the collection
    suspend fun getAllReservations(): List<Reservation> {
        val querySnapshot = reservationsCollection.get().await()
        return querySnapshot.documents.mapNotNull { it.toObject<Reservation>() }
    }

    // You can add more specific queries here, like getting all reservations for a user
}
