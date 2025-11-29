package com.example.miniproject.facility

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import java.util.Date


data class Facility(
    @DocumentId
    val id: String = "",

    val name: String = "",

    // 3. Correct syntax for default value assignment.
    val number: Int = 0,

    val startTime: Timestamp = Timestamp.now(),

    val endTime: Timestamp = Timestamp.now(),

    // 4. CRITICAL: Add the ownerId to create the relationship with the User.
    val ownerId: String = ""
)
