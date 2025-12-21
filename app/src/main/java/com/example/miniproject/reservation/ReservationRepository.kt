package com.example.miniproject.reservation

import com.example.miniproject.auth.FirebaseManager
import com.google.firebase.firestore.toObject
import kotlinx.coroutines.tasks.await

class ReservationRepository {

    private val reservationsCollection = FirebaseManager.firestore.collection("reservation")

    suspend fun createReservation(reservation: Reservation) {
        try {
            // Validate reservation data
            if (reservation.id.isBlank()) {
                throw IllegalArgumentException("Reservation ID cannot be blank")
            }
            if (reservation.facilityID.isBlank()) {
                throw IllegalArgumentException("Facility ID cannot be blank")
            }
            if (reservation.userID.isBlank()) {
                throw IllegalArgumentException("User ID cannot be blank")
            }
            if (reservation.bookedTime == null) {
                throw IllegalArgumentException("Booked time cannot be null")
            }
            
            println("📝 Creating reservation:")
            println("   ID: ${reservation.id}")
            println("   Facility: ${reservation.facilityID}")
            println("   User: ${reservation.userID}")
            println("   Time: ${reservation.bookedTime}")
            println("   Hours: ${reservation.bookedHours}")
            
            // Save to Firestore (matching admin format - uses same collection "reservation")
            reservationsCollection.document(reservation.id).set(reservation).await()
            
            // Update facility availability to prevent double booking
            updateFacilityAvailability(reservation.facilityID, reservation.bookedTime, reservation.bookedHours, isBooked = true)
            
            println("✅ Reservation created successfully: ${reservation.id}")
        } catch (e: Exception) {
            println("❌ Error creating reservation: ${e.message}")
            println("   Reservation ID: ${reservation.id}")
            println("   Facility ID: ${reservation.facilityID}")
            println("   User ID: ${reservation.userID}")
            e.printStackTrace()
            throw e // Re-throw to allow caller to handle
        }
    }

    suspend fun getReservation(id: String): Reservation? {
        return try {
            if (id.isBlank()) {
                println("⚠️ Empty reservation ID provided")
                return null
            }
            val documentSnapshot = reservationsCollection.document(id).get().await()
            if (!documentSnapshot.exists()) {
                println("⚠️ Reservation document does not exist: $id")
                return null
            }
            val reservation = documentSnapshot.toObject<Reservation>()
            if (reservation != null) {
                println("✅ Loaded reservation: $id")
            } else {
                println("⚠️ Failed to convert document to Reservation: $id")
            }
            reservation
        } catch (e: Exception) {
            println("❌ Error loading reservation $id: ${e.message}")
            e.printStackTrace()
            null
        }
    }

    suspend fun getAllReservations(): List<Reservation> {
        return try {
            val querySnapshot = reservationsCollection.get().await()
            val reservations = querySnapshot.documents.mapNotNull { doc ->
                try {
                    doc.toObject<Reservation>()?.copy(id = doc.id) // Ensure ID is set from document ID
                } catch (e: Exception) {
                    println("⚠️ Error converting document ${doc.id} to Reservation: ${e.message}")
                    null
                }
            }
            println("✅ Loaded ${reservations.size} reservations from 'reservation' collection")
            reservations
        } catch (e: Exception) {
            println("❌ Error loading reservations: ${e.message}")
            e.printStackTrace()
            emptyList()
        }
    }

    // Simple, direct search for reservations by the userID field.
    suspend fun findReservationsByUserId(userId: String): List<Reservation> {
        return try {
            if (userId.isBlank()) {
                println("⚠️ Empty user ID provided for reservation search")
                return emptyList()
            }
            val querySnapshot = reservationsCollection.whereEqualTo("userID", userId).get().await()
            val reservations = querySnapshot.documents.mapNotNull { doc ->
                try {
                    doc.toObject<Reservation>()?.copy(id = doc.id) // Ensure ID is set
                } catch (e: Exception) {
                    println("⚠️ Error converting reservation document ${doc.id}: ${e.message}")
                    null
                }
            }
            println("✅ Found ${reservations.size} reservations for user: $userId")
            reservations
        } catch (e: Exception) {
            println("❌ Error finding reservations for user $userId: ${e.message}")
            e.printStackTrace()
            emptyList()
        }
    }

    // This function is kept for potential future use, but is not used in the simple search.
    suspend fun findReservationsByUserIds(userIds: List<String>): List<Reservation> {
        if (userIds.isEmpty()) return emptyList()
        val querySnapshot = reservationsCollection.whereIn("userID", userIds).get().await()
        return querySnapshot.documents.mapNotNull { it.toObject<Reservation>() }
    }
    
    /**
     * Update facility availability when booking is created or cancelled
     */
    private suspend fun updateFacilityAvailability(
        facilityId: String,
        bookedTime: com.google.firebase.Timestamp?,
        bookedHours: Double,
        isBooked: Boolean
    ) {
        try {
            if (bookedTime == null) return
            
            val db = FirebaseManager.firestore
            val startTime = bookedTime.toDate()
            val endTime = java.util.Calendar.getInstance().apply {
                time = startTime
                add(java.util.Calendar.HOUR_OF_DAY, bookedHours.toInt())
                add(java.util.Calendar.MINUTE, ((bookedHours % 1) * 60).toInt())
            }.time
            
            // Check if it's a facilityind (subvenue) or main facility
            if (facilityId.contains("_")) {
                // It's a subvenue - update facilityind document
                val facilityIndDoc = db.collection("facilityind").document(facilityId)
                val currentData = facilityIndDoc.get().await().data ?: emptyMap()
                
                // Store booking time slots in facilityind document
                val bookings = (currentData["bookings"] as? List<Map<String, Any>>)?.toMutableList() ?: mutableListOf()
                
                if (isBooked) {
                    // Add booking slot
                    bookings.add(mapOf(
                        "startTime" to com.google.firebase.Timestamp(startTime),
                        "endTime" to com.google.firebase.Timestamp(endTime)
                    ))
                } else {
                    // Remove booking slot
                    bookings.removeAll { booking ->
                        val bookingStart = (booking["startTime"] as? com.google.firebase.Timestamp)?.toDate()
                        bookingStart == startTime
                    }
                }
                
                facilityIndDoc.update("bookings", bookings).await()
                println("✅ Updated facilityind availability: $facilityId")
            } else {
                // It's a main facility - update facility document
                val facilityDoc = db.collection("facility").document(facilityId)
                val currentData = facilityDoc.get().await().data ?: emptyMap()
                
                // Store booking time slots in facility document
                val bookings = (currentData["bookings"] as? List<Map<String, Any>>)?.toMutableList() ?: mutableListOf()
                
                if (isBooked) {
                    // Add booking slot
                    bookings.add(mapOf(
                        "startTime" to com.google.firebase.Timestamp(startTime),
                        "endTime" to com.google.firebase.Timestamp(endTime)
                    ))
                } else {
                    // Remove booking slot
                    bookings.removeAll { booking ->
                        val bookingStart = (booking["startTime"] as? com.google.firebase.Timestamp)?.toDate()
                        bookingStart == startTime
                    }
                }
                
                facilityDoc.update("bookings", bookings).await()
                println("✅ Updated facility availability: $facilityId")
            }
        } catch (e: Exception) {
            println("⚠️ Error updating facility availability: ${e.message}")
            // Don't throw - availability update is not critical for booking creation
        }
    }
    
    /**
     * Delete a reservation by ID
     */
    suspend fun deleteReservation(reservationId: String) {
        try {
            if (reservationId.isBlank()) {
                throw IllegalArgumentException("Reservation ID cannot be blank")
            }
            
            println("🗑️ Deleting reservation: $reservationId")
            
            // Get reservation before deleting to update availability
            val reservation = getReservation(reservationId)
            if (reservation != null) {
                // Update facility availability before deleting
                updateFacilityAvailability(reservation.facilityID, reservation.bookedTime, reservation.bookedHours, isBooked = false)
            }
            
            reservationsCollection.document(reservationId).delete().await()
            
            println("✅ Reservation deleted successfully: $reservationId")
        } catch (e: Exception) {
            println("❌ Error deleting reservation: ${e.message}")
            e.printStackTrace()
            throw e
        }
    }
}
