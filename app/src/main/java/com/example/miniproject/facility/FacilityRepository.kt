package com.example.miniproject.facility

import com.example.miniproject.auth.FirebaseManager
import com.google.firebase.firestore.toObject
import kotlinx.coroutines.tasks.await

class FacilityRepository {

    private val facilitiesCollection = FirebaseManager.firestore.collection("facilities")

    /**
     * Creates a new facility document using the ID provided within the facility object.
     * It is your responsibility to ensure facility.id is unique.
     * This will overwrite any existing document with the same ID.
     */
    suspend fun createFacility(facility: Facility) {
        facilitiesCollection.document(facility.id).set(facility).await()
    }

    // Function to get a single facility by its ID
    suspend fun getFacility(id: String): Facility? {
        val documentSnapshot = facilitiesCollection.document(id).get().await()
        return documentSnapshot.toObject<Facility>()
    }

    // Function to get all facilities from the collection
    suspend fun getAllFacilities(): List<Facility> {
        val querySnapshot = facilitiesCollection.get().await()
        return querySnapshot.documents.mapNotNull { it.toObject<Facility>() }
    }

    // You can add update and delete functions here as needed

}
