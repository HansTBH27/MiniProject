package com.example.miniproject.booking.Navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.miniproject.reservation.Reservation
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*

class BookingHistoryViewModel : ViewModel() {

    private val _items = MutableStateFlow<List<BookingHistoryItem>>(emptyList())
    val items = _items.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val db = FirebaseFirestore.getInstance()

    // Current logged in user ID
    private var currentUserId: String = ""

    fun setCurrentUserId(userId: String) {
        currentUserId = userId
    }

    // Function to load reservations from Firebase for the current user
    fun loadUserReservations(userId: String? = null) {
        viewModelScope.launch {
            try {
                _isLoading.value = true

                val targetUserId = userId ?: currentUserId
                if (targetUserId.isEmpty()) {
                    _items.value = emptyList()
                    return@launch
                }

                // Query Firebase for reservations by userID
                val reservation = db.collection("reservation")
                    .whereEqualTo("userID", targetUserId)
                    .orderBy("bookedTime", Query.Direction.DESCENDING)
                    .get()
                    .await()
                    .toObjects(Reservation::class.java)

                // Convert Reservation to BookingHistoryItem
                val bookingHistoryItems = reservation.map { reservation ->
                    convertReservationToBookingItem(reservation)
                }

                _items.value = bookingHistoryItems

            } catch (e: Exception) {
                e.printStackTrace()
                _items.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Helper function to convert Reservation to BookingHistoryItem
    private suspend fun convertReservationToBookingItem(reservation: Reservation): BookingHistoryItem {
        // Format the timestamp
        val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

        val date = if (reservation.bookedTime != null) {
            dateFormat.format(reservation.bookedTime.toDate())
        } else {
            "Unknown date"
        }

        val time = if (reservation.bookedTime != null) {
            timeFormat.format(reservation.bookedTime.toDate())
        } else {
            "Unknown time"
        }

        // Calculate end time based on bookedHours
        val endTime = if (reservation.bookedTime != null && reservation.bookedHours > 0) {
            val startMillis = reservation.bookedTime.toDate().time
            val endMillis = startMillis + (reservation.bookedHours * 60 * 60 * 1000)
            timeFormat.format(Date(endMillis))
        } else {
            ""
        }

        val timeRange = if (endTime.isNotEmpty()) {
            "$time - $endTime"
        } else {
            time
        }

        // Fetch facility name from facilities collection
        val facilityName = try {
            val facilityDoc = db.collection("facilities")
                .document(reservation.facilityID)
                .get()
                .await()
            facilityDoc.getString("name") ?: "Facility ${reservation.facilityID}"
        } catch (e: Exception) {
            "Facility ${reservation.facilityID}"
        }

        return BookingHistoryItem(
            id = reservation.id,
            facilityName = facilityName,
            date = date,
            time = timeRange,
            bookedHours = reservation.bookedHours,
            status = "Confirmed"
        )
    }

    // Optional: Refresh function
    fun refresh() {
        if (currentUserId.isNotEmpty()) {
            loadUserReservations()
        }
    }

    // Clear data when user logs out
    fun clear() {
        _items.value = emptyList()
        currentUserId = ""
    }
}