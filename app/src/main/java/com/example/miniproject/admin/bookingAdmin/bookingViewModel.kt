package com.example.miniproject.admin.bookingAdmin
import com.example.miniproject.reservation.Reservation
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class ReservationUIState(
    val facilityID: String = "",
    val userID: String = "",
    val bookedHours: Double = 1.0,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false,
    val validationErrors: ValidationErrors = ValidationErrors()
)

data class ValidationErrors(
    val facilityIDError: String? = null,
    val userIDError: String? = null,
    val bookedHoursError: String? = null
)

class ReservationViewModel : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()

    private val _uiState = MutableStateFlow(ReservationUIState())
    val uiState: StateFlow<ReservationUIState> = _uiState.asStateFlow()

    // Update facility ID
    fun updateFacilityID(facilityID: String) {
        _uiState.value = _uiState.value.copy(
            facilityID = facilityID,
            validationErrors = _uiState.value.validationErrors.copy(
                facilityIDError = validateFacilityID(facilityID)
            )
        )
    }

    // Update user ID
    fun updateUserID(userID: String) {
        _uiState.value = _uiState.value.copy(
            userID = userID,
            validationErrors = _uiState.value.validationErrors.copy(
                userIDError = validateUserID(userID)
            )
        )
    }

    // Update booked hours
    fun updateBookedHours(hours: Double) {
        _uiState.value = _uiState.value.copy(
            bookedHours = hours,
            validationErrors = _uiState.value.validationErrors.copy(
                bookedHoursError = validateBookedHours(hours)
            )
        )
    }

    // Validation functions
    private fun validateFacilityID(facilityID: String): String? {
        return when {
            facilityID.isBlank() -> "Facility ID is required"
            facilityID.length < 3 -> "Facility ID must be at least 3 characters"
            else -> null
        }
    }

    private fun validateUserID(userID: String): String? {
        return when {
            userID.isBlank() -> "User ID is required"
            userID.length < 3 -> "User ID must be at least 3 characters"
            else -> null
        }
    }

    private fun validateBookedHours(hours: Double): String? {
        return when {
            hours <= 0 -> "Booked hours must be greater than 0"
            hours > 24 -> "Booked hours cannot exceed 24 hours"
            else -> null
        }
    }

    // Check if all fields are valid
    fun isFormValid(): Boolean {
        val state = _uiState.value
        val validationErrors = ValidationErrors(
            facilityIDError = validateFacilityID(state.facilityID),
            userIDError = validateUserID(state.userID),
            bookedHoursError = validateBookedHours(state.bookedHours)
        )

        _uiState.value = state.copy(validationErrors = validationErrors)

        return validationErrors.facilityIDError == null &&
                validationErrors.userIDError == null &&
                validationErrors.bookedHoursError == null
    }

    // Verify facility exists in Firestore
    private suspend fun verifyFacilityExists(facilityID: String): Boolean {
        return try {
            val doc = firestore.collection("facility")
                .document(facilityID)
                .get()
                .await()
            doc.exists()
        } catch (e: Exception) {
            false
        }
    }

    // Verify user exists in Firestore
    private suspend fun verifyUserExists(userID: String): Boolean {
        return try {
            val doc = firestore.collection("user")
                .document(userID)
                .get()
                .await()
            doc.exists()
        } catch (e: Exception) {
            false
        }
    }

    // Create reservation with full validation
    fun createReservation() {
        viewModelScope.launch {
            // First validate form fields
            if (!isFormValid()) {
                _uiState.value = _uiState.value.copy(
                    error = "Please fix all validation errors"
                )
                return@launch
            }

            _uiState.value = _uiState.value.copy(
                isLoading = true,
                error = null,
                isSuccess = false
            )

            try {
                val state = _uiState.value

                // Verify facility exists
                if (!verifyFacilityExists(state.facilityID)) {
                    _uiState.value = state.copy(
                        isLoading = false,
                        error = "Facility ID '${state.facilityID}' does not exist",
                        validationErrors = state.validationErrors.copy(
                            facilityIDError = "Facility not found in database"
                        )
                    )
                    return@launch
                }

                // Verify user exists
                if (!verifyUserExists(state.userID)) {
                    _uiState.value = state.copy(
                        isLoading = false,
                        error = "User ID '${state.userID}' does not exist",
                        validationErrors = state.validationErrors.copy(
                            userIDError = "User not found in database"
                        )
                    )
                    return@launch
                }

                // Create reservation entity
                val reservation = Reservation(
                    bookedTime = Timestamp.now(),
                    facilityID = state.facilityID,
                    userID = state.userID,
                    bookedHours = state.bookedHours
                )

                // Convert to Firestore document (map)
                val reservationMap = hashMapOf(
                    "bookedTime" to reservation.bookedTime,
                    "facilityID" to reservation.facilityID,
                    "userID" to reservation.userID,
                    "bookedHours" to reservation.bookedHours
                )

                // Save to Firestore
                firestore.collection("reservations")
                    .add(reservationMap)
                    .await()

                _uiState.value = state.copy(
                    isLoading = false,
                    isSuccess = true,
                    error = null
                )

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to create reservation: ${e.message}",
                    isSuccess = false
                )
            }
        }
    }

    // Reset form
    fun resetForm() {
        _uiState.value = ReservationUIState()
    }

    // Clear error
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}