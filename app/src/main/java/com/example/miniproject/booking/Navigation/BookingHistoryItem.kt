package com.example.miniproject.booking.Navigation

data class BookingHistoryItem(
    val id: String,
    val facilityName: String,
    val date: String,
    val time: String,
    val bookedHours: Int = 0,
    val status: String = "Confirmed"
)