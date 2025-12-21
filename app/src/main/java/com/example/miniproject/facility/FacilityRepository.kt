package com.example.miniproject.facility

import com.example.miniproject.auth.FirebaseManager
import com.google.firebase.firestore.toObject
import kotlinx.coroutines.tasks.await

class FacilityRepository {

    private val facilitiesCollection = FirebaseManager.firestore.collection("facility")

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
        return try {
            if (id.isBlank()) {
                println("❌ Empty facility ID provided")
                return null
            }
            
            val documentSnapshot = facilitiesCollection.document(id).get().await()
            
            if (!documentSnapshot.exists()) {
                println("❌ Facility document does not exist: $id")
                return null
            }
            
            val facility = documentSnapshot.toObject<Facility>()
            if (facility == null) {
                println("❌ Failed to convert document to Facility object: $id")
                null
            } else {
                // Ensure ID is set from document ID (matching admin format)
                val facilityWithId = facility.copy(id = id)
                println("✅ Successfully loaded facility: ${facilityWithId.name} (ID: $id)")
                facilityWithId
            }
        } catch (e: Exception) {
            println("❌ Exception loading facility $id: ${e.message}")
            e.printStackTrace()
            null
        }
    }

    // Function to get all facilities from the collection
    suspend fun getAllFacilities(): List<Facility> {
        return try {
            val querySnapshot = facilitiesCollection.get().await()
            val facilities = querySnapshot.documents.mapNotNull { doc ->
                try {
                    // Convert document to Facility object and ensure ID is set from document ID
                    // This matches admin format where document ID is the facility ID (e.g., "C1", "S1")
                    val facility = doc.toObject<Facility>()
                    if (facility != null) {
                        facility.copy(id = doc.id) // Ensure ID matches document ID
                    } else {
                        println("⚠️ Document ${doc.id} could not be converted to Facility")
                        null
                    }
                } catch (e: Exception) {
                    println("⚠️ Error converting document ${doc.id} to Facility: ${e.message}")
                    e.printStackTrace()
                    null
                }
            }
            println("✅ Loaded ${facilities.size} facilities from 'facility' collection")
            facilities
        } catch (e: Exception) {
            println("❌ Error loading facilities: ${e.message}")
            e.printStackTrace()
            emptyList()
        }
    }

    // You can add update and delete functions here as needed

}
