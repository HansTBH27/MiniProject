package com.example.miniproject.equipment

import com.example.miniproject.auth.FirebaseManager
import com.google.firebase.firestore.toObject
import kotlinx.coroutines.tasks.await

class EquipmentRepository {

    private val equipmentCollection = FirebaseManager.firestore.collection("equipment")

    /**
     * Creates a new equipment document using the ID provided within the equipment object.
     * It is your responsibility to ensure equipment.id is unique.
     * This will overwrite any existing document with the same ID.
     */
    suspend fun createEquipment(equipment: Equipment) {
        equipmentCollection.document(equipment.id).set(equipment).await()
    }

    // Function to get a single piece of equipment by its ID
    suspend fun getEquipment(id: String): Equipment? {
        val documentSnapshot = equipmentCollection.document(id).get().await()
        return documentSnapshot.toObject<Equipment>()
    }

    // Function to get all equipment from the collection
    suspend fun getAllEquipment(): List<Equipment> {
        val querySnapshot = equipmentCollection.get().await()
        return querySnapshot.documents.mapNotNull { it.toObject<Equipment>() }
    }

    // You can add more specific queries here, like getting all equipment for a facilityID
}
