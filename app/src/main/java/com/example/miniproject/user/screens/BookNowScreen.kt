package com.example.miniproject.user.screens

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.miniproject.auth.AuthRepository
import com.example.miniproject.auth.FirebaseManager
import com.example.miniproject.equipment.Equipment
import com.example.miniproject.equipment.EquipmentRepository
import com.example.miniproject.facility.Facility
import com.example.miniproject.facility.FacilityInd
import com.example.miniproject.facility.FacilityIndRepository
import com.example.miniproject.facility.FacilityRepository
import com.example.miniproject.reservation.ReservationRepository
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.toObject
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookNowScreen(
    facilityId: String,
    navController: NavController
) {
    val primaryColor = Color(0xFF5553DC)
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    
    // Repositories
    val facilityRepository = remember { FacilityRepository() }
    val equipmentRepository = remember { EquipmentRepository() }
    val facilityIndRepository = remember { FacilityIndRepository() }
    val authRepository = remember { AuthRepository() }
    val reservationRepository = remember { ReservationRepository() }
    
    // State
    var facility by remember { mutableStateOf<Facility?>(null) }
    var equipmentList by remember { mutableStateOf<List<Equipment>>(emptyList()) }
    var subVenues by remember { mutableStateOf<List<FacilityInd>>(emptyList()) } // Changed to FacilityInd
    var existingBookings by remember { mutableStateOf<List<com.example.miniproject.reservation.Reservation>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isBooking by remember { mutableStateOf(false) }
    
    // Booking state (matching admin format)
    var selectedDate by rememberSaveable { mutableStateOf<Long?>(null) }
    var selectedTime by rememberSaveable { mutableStateOf<Pair<Int, Int>?>(null) }
    var selectedDuration by rememberSaveable { mutableStateOf(1.0) } // Double like admin
    var selectedEquipmentQuantities by rememberSaveable { mutableStateOf<Map<String, Int>>(emptyMap()) }
    var selectedSubVenue by rememberSaveable { mutableStateOf<FacilityInd?>(null) } // Changed to FacilityInd
    
    // Validation errors (matching admin)
    var timeValidationError by remember { mutableStateOf<String?>(null) }
    var pastTimeError by remember { mutableStateOf<String?>(null) }
    
    // Date formatter (matching admin)
    val dateFormat = remember { SimpleDateFormat("d MMM yyyy", Locale.getDefault()) }
    
    // Load facility and equipment
    LaunchedEffect(facilityId) {
        scope.launch {
            try {
                isLoading = true
                errorMessage = null
                
                if (facilityId.isBlank()) {
                    errorMessage = "Invalid facility ID"
                    isLoading = false
                    return@launch
                }
                
                // Load facility
                facility = facilityRepository.getFacility(facilityId)
                
                if (facility == null) {
                    errorMessage = "Facility not found"
                    isLoading = false
                    return@launch
                }
                
                // Load equipment for this facility
                // Try two approaches: by document ID prefix (e.g., "S1E1", "S1E2") and by facilityID field
                try {
                    val db = com.example.miniproject.auth.FirebaseManager.firestore
                    
                    // Approach 1: Query by document ID prefix (matching admin pattern)
                    val equipmentQueryById = try {
                        db.collection("equipment")
                            .whereGreaterThanOrEqualTo(FieldPath.documentId(), "${facilityId}E")
                            .whereLessThan(FieldPath.documentId(), "${facilityId}F")
                            .get()
                            .await()
                    } catch (e: Exception) {
                        println("⚠️ Query by document ID failed: ${e.message}")
                        null
                    }
                    
                    // Approach 2: Query by facilityID field
                    val equipmentQueryByField = try {
                        db.collection("equipment")
                            .whereEqualTo("facilityID", facilityId)
                            .get()
                            .await()
                    } catch (e: Exception) {
                        println("⚠️ Query by facilityID field failed: ${e.message}")
                        null
                    }
                    
                    // Combine results from both queries
                    val allEquipmentDocs = mutableSetOf<String>()
                    val equipmentMap = mutableMapOf<String, Equipment>()
                    
                    equipmentQueryById?.documents?.forEach { doc ->
                        if (!allEquipmentDocs.contains(doc.id)) {
                            try {
                                val equipment = doc.toObject<Equipment>()
                                equipment?.copy(id = doc.id)?.let { eq ->
                                    allEquipmentDocs.add(doc.id)
                                    equipmentMap[doc.id] = eq
                                }
                            } catch (e: Exception) {
                                println("⚠️ Error parsing equipment ${doc.id}: ${e.message}")
                            }
                        }
                    }
                    
                    equipmentQueryByField?.documents?.forEach { doc ->
                        if (!allEquipmentDocs.contains(doc.id)) {
                            try {
                                val equipment = doc.toObject<Equipment>()
                                equipment?.copy(id = doc.id)?.let { eq ->
                                    allEquipmentDocs.add(doc.id)
                                    equipmentMap[doc.id] = eq
                                }
                            } catch (e: Exception) {
                                println("⚠️ Error parsing equipment ${doc.id}: ${e.message}")
                            }
                        }
                    }
                    
                    // Filter by quantity > 0
                    equipmentList = equipmentMap.values.filter { it.quantity > 0 }
                    println("✅ Loaded ${equipmentList.size} equipment items for facility $facilityId")
                    equipmentList.forEach { eq ->
                        println("   - Equipment: ${eq.id} - ${eq.name} (qty: ${eq.quantity})")
                    }
                } catch (e: Exception) {
                    println("⚠️ Error loading equipment: ${e.message}")
                    e.printStackTrace()
                    equipmentList = emptyList()
                }
                
                // Load subvenues from facilityind collection (matching admin - SubVenuesViewModel)
                try {
                    val allFacilityInds = facilityIndRepository.getAllFacilityInds()
                    
                    // Filter FacilityInd objects that match the main facility (IDs like "facilityId_1", "facilityId_2")
                    subVenues = allFacilityInds.filter { facilityInd ->
                        val facilityIndId = facilityInd.id
                        if (facilityIndId.isBlank()) return@filter false
                        
                        // Strip to get prefix (e.g., "S1_1" → "S1")
                        val prefix = facilityIndId.substringBefore("_")
                        
                        // Check if it matches and has underscore
                        prefix == facilityId && facilityIndId.contains("_")
                    }.sortedBy { it.name }
                    
                    println("✅ Loaded ${subVenues.size} subvenues from facilityind for facility $facilityId")
                    subVenues.forEach { subVenue ->
                        println("   - Subvenue: ${subVenue.id} - ${subVenue.name}")
                    }
                } catch (e: Exception) {
                    println("⚠️ Error loading subvenues: ${e.message}")
                    e.printStackTrace()
                    subVenues = emptyList()
                }
                
                // Load existing bookings for this facility to filter time slots
                try {
                    val allReservations = reservationRepository.getAllReservations()
                    existingBookings = allReservations.filter { 
                        it.facilityID == facilityId || it.facilityID.startsWith("${facilityId}_")
                    }
                    println("✅ Loaded ${existingBookings.size} existing bookings for facility $facilityId")
                } catch (e: Exception) {
                    println("⚠️ Error loading existing bookings: ${e.message}")
                    existingBookings = emptyList()
                }
                
            } catch (e: Exception) {
                errorMessage = "Failed to load facility: ${e.message}"
                println("❌ Error loading facility: ${e.message}")
            } finally {
                isLoading = false
            }
        }
    }
    
    // Validate time (matching admin validation logic - based on facilityAdmin package)
    LaunchedEffect(selectedTime, facility, selectedDate, selectedDuration) {
        if (selectedTime != null && facility != null && selectedDate != null) {
            val (hour, minute) = selectedTime!!
            
            // Validate against facility operating hours (matching facilityAdmin - default to 0800-2200 if empty)
            val startTimeStr = if (facility!!.startTime.isNotBlank()) facility!!.startTime else "0800"
            val endTimeStr = if (facility!!.endTime.isNotBlank()) facility!!.endTime else "2200"
            
            val facilityStartTime = parseTimeString(startTimeStr)
            val facilityEndTime = parseTimeString(endTimeStr)
            
            if (facilityStartTime == null || facilityEndTime == null) {
                timeValidationError = "Facility operating hours are not properly configured"
                println("❌ Invalid facility times: start=$startTimeStr, end=$endTimeStr")
            } else {
                val selectedTotalMinutes = hour * 60 + minute
                val startTotalMinutes = facilityStartTime.first * 60 + facilityStartTime.second
                val endTotalMinutes = facilityEndTime.first * 60 + facilityEndTime.second
                
                println("🕐 Time validation: selected=$selectedTotalMinutes, start=$startTotalMinutes, end=$endTotalMinutes, hours=$selectedDuration")
                
                if (selectedTotalMinutes < startTotalMinutes) {
                    timeValidationError = "Reservation time is before facility opening time (${formatTime(facilityStartTime.first, facilityStartTime.second)})"
                } else {
                    val reservationEndMinutes = selectedTotalMinutes + (selectedDuration * 60).toInt()
                    if (reservationEndMinutes > endTotalMinutes) {
                        timeValidationError = "Reservation exceeds facility closing time (${formatTime(facilityEndTime.first, facilityEndTime.second)})"
                    } else if (reservationEndMinutes >= 1440) { // 24 * 60
                        timeValidationError = "Reservation cannot extend past midnight"
                    } else {
                        timeValidationError = null
                        println("✅ Time validation passed")
                    }
                }
            }
            
            // Validate past time
            val selectedCalendar = Calendar.getInstance().apply {
                timeInMillis = selectedDate!!
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            
            val currentCalendar = Calendar.getInstance()
            if (selectedCalendar.timeInMillis <= currentCalendar.timeInMillis) {
                pastTimeError = "Cannot book a reservation in the past. Please select a future date and time."
                println("❌ Past time selected: ${selectedCalendar.time} is before current time: ${currentCalendar.time}")
            } else {
                pastTimeError = null
                println("✅ Future time validated: ${selectedCalendar.time}")
            }
        } else {
            timeValidationError = null
            pastTimeError = null
        }
    }
    
    // Validation (matching admin)
    val isBookingValid = selectedDate != null && 
            selectedTime != null && 
            timeValidationError == null && 
            pastTimeError == null
    
    // UI
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Book Facility", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            if (navController.previousBackStackEntry != null) {
                                navController.popBackStack()
                            } else {
                                navController.navigate("main") {
                                    popUpTo("main") { inclusive = true }
                                }
                            }
                        }
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = primaryColor,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color.White)
        ) {
            when {
                isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            CircularProgressIndicator(color = primaryColor)
                            Text("Loading facility...", color = Color.Gray)
                        }
                    }
                }
                
                errorMessage != null || facility == null -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Icon(
                                Icons.Default.Error,
                                contentDescription = "Error",
                                tint = Color.Red,
                                modifier = Modifier.size(48.dp)
                            )
                            Text(
                                errorMessage ?: "Facility not found",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Red,
                                textAlign = TextAlign.Center
                            )
                            TextButton(
                                onClick = {
                                    if (navController.previousBackStackEntry != null) {
                                        navController.popBackStack()
                                    } else {
                                        navController.navigate("main") {
                                            popUpTo("main") { inclusive = true }
                                        }
                                    }
                                }
                            ) {
                                Text("Go Back", color = primaryColor)
                            }
                        }
                    }
                }
                
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Facility Info
                        item {
                            FacilityInfoCard(
                                facility = facility!!,
                                primaryColor = primaryColor
                            )
                        }
                        
                        // Subvenue Selection (if available)
                        if (subVenues.isNotEmpty()) {
                            item {
                                SubVenueSelectionCard(
                                    subVenues = subVenues,
                                    selectedSubVenue = selectedSubVenue,
                                    onSubVenueSelected = { subVenue ->
                                        selectedSubVenue = subVenue
                                    },
                                    primaryColor = primaryColor
                                )
                            }
                        }
                        
                        // Date Selection (matching admin - uses DatePickerDialog)
                        item {
                            DateSelectionCard(
                                selectedDate = selectedDate,
                                onDateSelected = { dateMillis ->
                                    selectedDate = dateMillis
                                    selectedTime = null // Reset time when date changes
                                },
                                primaryColor = primaryColor,
                                dateFormat = dateFormat,
                                context = context
                            )
                        }
                        
                        // Time Selection (matching admin - uses TimePickerDialog)
                        if (selectedDate != null) {
                            item {
                                TimeSelectionCard(
                                    selectedTime = selectedTime,
                                    onTimeSelected = { hour, minute ->
                                        selectedTime = Pair(hour, minute)
                                    },
                                    primaryColor = primaryColor,
                                    context = context,
                                    timeValidationError = timeValidationError,
                                    pastTimeError = pastTimeError,
                                    facility = facility,
                                    selectedDate = selectedDate,
                                    selectedDuration = selectedDuration,
                                    existingBookings = existingBookings
                                )
                            }
                        }
                        
                        // Duration Selection (matching admin - FilterChip in two rows)
                        item {
                            DurationSelectionCard(
                                selectedDuration = selectedDuration,
                                onDurationSelected = { duration ->
                                    selectedDuration = duration
                                },
                                primaryColor = primaryColor
                            )
                        }
                        
                        // Equipment Selection (if equipment available)
                        if (equipmentList.isNotEmpty()) {
                            item {
                                EquipmentSelectionCard(
                                    equipmentList = equipmentList,
                                    selectedEquipmentQuantities = selectedEquipmentQuantities,
                                    onQuantityChanged = { equipmentId, quantity ->
                                        val newMap = selectedEquipmentQuantities.toMutableMap()
                                        if (quantity > 0) {
                                            newMap[equipmentId] = quantity
                                        } else {
                                            newMap.remove(equipmentId)
                                        }
                                        selectedEquipmentQuantities = newMap
                                    },
                                    primaryColor = primaryColor
                                )
                            }
                        }
                        
                        // Booking Summary
                        if (isBookingValid) {
                            item {
                                BookingSummaryCard(
                                    facility = facility!!,
                                    selectedDate = selectedDate!!,
                                    selectedTime = selectedTime!!,
                                    selectedDuration = selectedDuration,
                                    selectedEquipmentQuantities = selectedEquipmentQuantities,
                                    equipmentList = equipmentList,
                                    primaryColor = primaryColor,
                                    dateFormat = dateFormat
                                )
                            }
                        }
                        
                        // Proceed to Payment Button
                        item {
                            Button(
                                onClick = {
                                    if (isBookingValid) {
                                        scope.launch {
                                            isBooking = true
                                            errorMessage = null
                                            
                                            try {
                                                val currentUser = FirebaseManager.auth.currentUser
                                                if (currentUser == null) {
                                                    errorMessage = "User not logged in"
                                                    isBooking = false
                                                    return@launch
                                                }
                                                
                                                // Get user's displayId (matching admin)
                                                val userData = try {
                                                    authRepository.getUserData(currentUser.uid)
                                                } catch (e: Exception) {
                                                    println("⚠️ Error fetching user data: ${e.message}")
                                                    null
                                                }
                                                
                                                val displayId = userData?.displayId?.takeIf { it.isNotBlank() } ?: currentUser.uid
                                                
                                                if (displayId.isBlank()) {
                                                    errorMessage = "User ID not found"
                                                    isBooking = false
                                                    return@launch
                                                }
                                                
                                                // Create booking calendar (matching admin)
                                                val bookingCalendar = Calendar.getInstance().apply {
                                                    timeInMillis = selectedDate!!
                                                    val (hour, minute) = selectedTime!!
                                                    set(Calendar.HOUR_OF_DAY, hour)
                                                    set(Calendar.MINUTE, minute)
                                                    set(Calendar.SECOND, 0)
                                                    set(Calendar.MILLISECOND, 0)
                                                }
                                                
                                                // Convert to timestamp (milliseconds)
                                                val startTimeMillis = bookingCalendar.timeInMillis
                                                
                                                // Use selected subvenue ID (from facilityind) if available, otherwise use facility ID
                                                val bookingFacilityId = selectedSubVenue?.id ?: facilityId
                                                
                                                println("💳 Booking facility ID: $bookingFacilityId")
                                                println("   Selected subvenue: ${selectedSubVenue?.name ?: "Main facility"}")
                                                
                                                // Build equipment data string (matching admin format)
                                                val equipmentData = if (selectedEquipmentQuantities.isEmpty()) {
                                                    "NONE"
                                                } else {
                                                    selectedEquipmentQuantities.map { (equipmentId, quantity) ->
                                                        "$equipmentId:$quantity"
                                                    }.joinToString(",")
                                                }
                                                
                                                // Navigate to payment screen
                                                // Route: payment/{userId}/{facilityIndId}/{equipmentData}/{startTime}/{bookedHours}
                                                val bookedHoursFloat = selectedDuration.toFloat()
                                                
                                                // URL encode the parameters
                                                val encodedUserId = java.net.URLEncoder.encode(displayId, "UTF-8")
                                                val encodedFacilityId = java.net.URLEncoder.encode(bookingFacilityId, "UTF-8")
                                                val encodedEquipmentData = java.net.URLEncoder.encode(equipmentData, "UTF-8")
                                                
                                                val paymentRoute = "payment/$encodedUserId/$encodedFacilityId/$encodedEquipmentData/$startTimeMillis/$bookedHoursFloat"
                                                
                                                println("💳 Using facility ID: $bookingFacilityId (subvenue: ${selectedSubVenue?.name ?: "main facility"})")
                                                
                                                println("💳 Navigating to payment: $paymentRoute")
                                                println("   User ID: $displayId")
                                                println("   Facility ID: $facilityId")
                                                println("   Equipment: $equipmentData")
                                                println("   Start Time: $startTimeMillis (${bookingCalendar.time})")
                                                println("   Duration: $selectedDuration hours")
                                                
                                                isBooking = false
                                                navController.navigate(paymentRoute)
                                                
                                            } catch (e: Exception) {
                                                errorMessage = "Failed to proceed: ${e.message ?: "Unknown error"}"
                                                println("❌ Error proceeding to payment: ${e.message}")
                                                e.printStackTrace()
                                                isBooking = false
                                            }
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp),
                                enabled = isBookingValid && !isBooking,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = primaryColor
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                if (isBooking) {
                                    CircularProgressIndicator(
                                        color = Color.White,
                                        modifier = Modifier.size(24.dp)
                                    )
                                } else {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Text(
                                            "Proceed to Payment",
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Icon(
                                            Icons.Default.Payment,
                                            contentDescription = "Payment",
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        }
                        
                        // Error Message
                        if (errorMessage != null) {
                            item {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(
                                        containerColor = Color(0xFFFFEBEE)
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            Icons.Default.Error,
                                            contentDescription = "Error",
                                            tint = Color.Red,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            errorMessage ?: "",
                                            color = Color.Red,
                                            fontSize = 14.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FacilityInfoCard(facility: Facility, primaryColor: Color) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF8F8F8)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // Facility Name
            Text(
                text = facility.name,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            
            // Facility ID
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "ID: ${facility.id}",
                fontSize = 12.sp,
                color = Color.Gray
            )
            
            // Description
            if (facility.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Description",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = facility.description,
                    fontSize = 14.sp,
                    color = Color.Gray,
                    lineHeight = 20.sp
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            Divider(color = Color.LightGray)
            Spacer(modifier = Modifier.height(16.dp))
            
            // Location
            if (facility.location.isNotBlank()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    Icon(
                        Icons.Default.LocationOn,
                        contentDescription = "Location",
                        tint = primaryColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Location",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                        Text(
                            text = facility.location,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.Black
                        )
                    }
                }
            }
            
            // Capacity
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Icon(
                    Icons.Default.People,
                    contentDescription = "Capacity",
                    tint = primaryColor,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "Capacity",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                    Text(
                        text = "${facility.minNum} - ${facility.maxNum} people",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Black
                    )
                }
            }
            
            // Operating Hours (matching facilityAdmin - default to 0800-2200 if empty)
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Schedule,
                    contentDescription = "Hours",
                    tint = primaryColor,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "Operating Hours",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                    val startTimeStr = if (facility.startTime.isNotBlank()) facility.startTime else "0800"
                    val endTimeStr = if (facility.endTime.isNotBlank()) facility.endTime else "2200"
                    Text(
                        text = "${formatTimeFromHHMM(startTimeStr)} - ${formatTimeFromHHMM(endTimeStr)}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Black
                    )
                }
            }
        }
    }
}

@Composable
fun DateSelectionCard(
    selectedDate: Long?,
    onDateSelected: (Long) -> Unit,
    primaryColor: Color,
    dateFormat: SimpleDateFormat,
    context: android.content.Context
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Select Date",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Date picker field (matching admin - uses DatePickerDialog)
            OutlinedTextField(
                value = selectedDate?.let { dateFormat.format(Date(it)) } ?: "",
                onValueChange = { },
                label = { Text("Date") },
                leadingIcon = {
                    Icon(Icons.Default.CalendarToday, contentDescription = "Date")
                },
                trailingIcon = {
                    IconButton(
                        onClick = {
                            val calendar = Calendar.getInstance()
                            DatePickerDialog(
                                context,
                                { _, year, month, dayOfMonth ->
                                    val selectedCalendar = Calendar.getInstance()
                                    selectedCalendar.set(year, month, dayOfMonth)
                                    onDateSelected(selectedCalendar.timeInMillis)
                                },
                                calendar.get(Calendar.YEAR),
                                calendar.get(Calendar.MONTH),
                                calendar.get(Calendar.DAY_OF_MONTH)
                            ).show()
                        }
                    ) {
                        Icon(Icons.Default.DateRange, contentDescription = "Pick Date")
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                readOnly = true
            )
        }
    }
}

@Composable
fun TimeSelectionCard(
    selectedTime: Pair<Int, Int>?,
    onTimeSelected: (Int, Int) -> Unit,
    primaryColor: Color,
    context: android.content.Context,
    timeValidationError: String?,
    pastTimeError: String?,
    facility: Facility?,
    selectedDate: Long?,
    selectedDuration: Double,
    existingBookings: List<com.example.miniproject.reservation.Reservation>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Select Time",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Time picker field (matching admin - uses TimePickerDialog)
            OutlinedTextField(
                value = selectedTime?.let { (hour, minute) ->
                    String.format("%02d:%02d", hour, minute)
                } ?: "",
                onValueChange = { },
                label = { Text("Start Time") },
                leadingIcon = {
                    Icon(Icons.Default.AccessTime, contentDescription = "Time")
                },
                trailingIcon = {
                    IconButton(
                        onClick = {
                            val calendar = Calendar.getInstance()
                            TimePickerDialog(
                                context,
                                { _, hourOfDay, minute ->
                                    onTimeSelected(hourOfDay, minute)
                                },
                                calendar.get(Calendar.HOUR_OF_DAY),
                                calendar.get(Calendar.MINUTE),
                                true // 24-hour format
                            ).show()
                        }
                    ) {
                        Icon(Icons.Default.Schedule, contentDescription = "Pick Time")
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                readOnly = true,
                isError = timeValidationError != null || pastTimeError != null
            )
            
            // Display past time error (priority)
            if (pastTimeError != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.padding(start = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = "Past Time Error",
                        tint = Color(0xFFF44336),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        pastTimeError!!,
                        color = Color(0xFFF44336),
                        fontSize = 12.sp
                    )
                }
            }
            
            // Display facility hours validation error
            if (timeValidationError != null && pastTimeError == null) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.padding(start = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = "Time Error",
                        tint = Color(0xFFF44336),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        timeValidationError!!,
                        color = Color(0xFFF44336),
                        fontSize = 12.sp
                    )
                }
            }
            
            // Display reservation end time (matching admin)
            if (selectedTime != null && facility != null && selectedDate != null) {
                val (hour, minute) = selectedTime
                val endTime = calculateEndTime(hour, minute, selectedDuration)
                if (endTime != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Reservation End Time: ${formatTime(endTime.first, endTime.second)}",
                        fontSize = 12.sp,
                        color = if (timeValidationError == null && pastTimeError == null)
                            primaryColor else Color(0xFFF44336),
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(start = 16.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DurationSelectionCard(
    selectedDuration: Double,
    onDurationSelected: (Double) -> Unit,
    primaryColor: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Booked Hours",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // First row: 0.5, 1.0, 1.5 (matching admin)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                listOf(0.5, 1.0, 1.5).forEach { hours ->
                    FilterChip(
                        selected = selectedDuration == hours,
                        onClick = { onDurationSelected(hours) },
                        label = {
                            Text(
                                if (hours == 0.5 || hours == 1.5) {
                                    "${hours.toString().replace(".0", "")} Hour${if (hours > 1) "s" else ""}"
                                } else {
                                    "${hours.toInt()} Hour${if (hours > 1) "s" else ""}"
                                }
                            )
                        },
                        modifier = Modifier.weight(1f),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = primaryColor,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Second row: 2.0, 2.5, 3.0 (matching admin)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                listOf(2.0, 2.5, 3.0).forEach { hours ->
                    FilterChip(
                        selected = selectedDuration == hours,
                        onClick = { onDurationSelected(hours) },
                        label = {
                            Text(
                                if (hours == 2.5) {
                                    "${hours} Hours"
                                } else {
                                    "${hours.toInt()} Hours"
                                }
                            )
                        },
                        modifier = Modifier.weight(1f),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = primaryColor,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun EquipmentSelectionCard(
    equipmentList: List<Equipment>,
    selectedEquipmentQuantities: Map<String, Int>,
    onQuantityChanged: (String, Int) -> Unit,
    primaryColor: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Equipment (Optional)",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            equipmentList.forEach { equipment ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = equipment.name,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.Black
                        )
                        Text(
                            text = "RM ${String.format("%.2f", equipment.price)} each",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                        Text(
                            text = "Available: ${equipment.quantity}",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                    
                    // Quantity selector
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        IconButton(
                            onClick = {
                                val current = selectedEquipmentQuantities[equipment.id] ?: 0
                                if (current > 0) {
                                    onQuantityChanged(equipment.id, current - 1)
                                }
                            },
                            enabled = (selectedEquipmentQuantities[equipment.id] ?: 0) > 0
                        ) {
                            Icon(
                                Icons.Default.Remove,
                                contentDescription = "Decrease",
                                tint = primaryColor
                            )
                        }
                        
                        Text(
                            text = "${selectedEquipmentQuantities[equipment.id] ?: 0}",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.width(30.dp),
                            textAlign = TextAlign.Center
                        )
                        
                        IconButton(
                            onClick = {
                                val current = selectedEquipmentQuantities[equipment.id] ?: 0
                                if (current < equipment.quantity) {
                                    onQuantityChanged(equipment.id, current + 1)
                                }
                            },
                            enabled = (selectedEquipmentQuantities[equipment.id] ?: 0) < equipment.quantity
                        ) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = "Increase",
                                tint = primaryColor
                            )
                        }
                    }
                }
                
                if (equipment != equipmentList.last()) {
                    Divider(color = Color.LightGray, modifier = Modifier.padding(vertical = 4.dp))
                }
            }
        }
    }
}

@Composable
fun BookingSummaryCard(
    facility: Facility,
    selectedDate: Long,
    selectedTime: Pair<Int, Int>,
    selectedDuration: Double,
    selectedEquipmentQuantities: Map<String, Int>,
    equipmentList: List<Equipment>,
    primaryColor: Color,
    dateFormat: SimpleDateFormat
) {
    val (hour, minute) = selectedTime
    val totalEquipmentPrice = selectedEquipmentQuantities.mapNotNull { (equipmentId, quantity) ->
        equipmentList.find { it.id == equipmentId }?.let { equipment ->
            equipment.price * quantity
        }
    }.sum()
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = primaryColor.copy(alpha = 0.1f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Icon(
                    Icons.Default.Summarize,
                    contentDescription = "Summary",
                    tint = primaryColor,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Booking Summary",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }
            
            Divider(color = Color.LightGray)
            Spacer(modifier = Modifier.height(12.dp))
            
            // Facility
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Facility:", fontSize = 14.sp, color = Color.Gray)
                Text(
                    facility.name,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Black
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Date
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Date:", fontSize = 14.sp, color = Color.Gray)
                Text(
                    dateFormat.format(Date(selectedDate)),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Black
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Time
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Time:", fontSize = 14.sp, color = Color.Gray)
                Text(
                    formatTime(hour, minute),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Black
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Duration
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Duration:", fontSize = 14.sp, color = Color.Gray)
                Text(
                    if (selectedDuration == 0.5 || selectedDuration == 1.5 || selectedDuration == 2.5) {
                        "${selectedDuration} Hours"
                    } else {
                        "${selectedDuration.toInt()} Hours"
                    },
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Black
                )
            }
            
            // Equipment
            if (selectedEquipmentQuantities.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Divider(color = Color.LightGray)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Equipment:",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(4.dp))
                selectedEquipmentQuantities.forEach { (equipmentId, quantity) ->
                    val equipment = equipmentList.find { it.id == equipmentId }
                    if (equipment != null && quantity > 0) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                "${equipment.name} x$quantity",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                            Text(
                                "RM ${String.format("%.2f", equipment.price * quantity)}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.Black
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "Equipment Total:",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Text(
                        "RM ${String.format("%.2f", totalEquipmentPrice)}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = primaryColor
                    )
                }
            }
        }
    }
}

// Helper functions (matching admin format)
fun parseTimeString(timeString: String): Pair<Int, Int>? {
    return try {
        if (timeString.length == 4) {
            val hour = timeString.substring(0, 2).toIntOrNull()
            val minute = timeString.substring(2, 4).toIntOrNull()
            if (hour != null && minute != null && hour in 0..23 && minute in 0..59) {
                Pair(hour, minute)
            } else {
                null
            }
        } else {
            null
        }
    } catch (e: Exception) {
        println("❌ Error parsing time string: $timeString - ${e.message}")
        null
    }
}

fun formatTime(hour: Int, minute: Int): String {
    return String.format("%02d:%02d", hour, minute)
}

fun formatTimeFromHHMM(timeStr: String): String {
    val parsed = parseTimeString(timeStr)
    return if (parsed != null) {
        formatTime(parsed.first, parsed.second)
    } else {
        timeStr
    }
}

fun calculateEndTime(startHour: Int, startMinute: Int, duration: Double): Pair<Int, Int>? {
    return try {
        val startTotalMinutes = startHour * 60 + startMinute
        val endTotalMinutes = startTotalMinutes + (duration * 60).toInt()
        val endHour = (endTotalMinutes / 60) % 24
        val endMinute = endTotalMinutes % 60
        Pair(endHour, endMinute)
    } catch (e: Exception) {
        null
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubVenueSelectionCard(
    subVenues: List<FacilityInd>,
    selectedSubVenue: FacilityInd?,
    onSubVenueSelected: (FacilityInd?) -> Unit,
    primaryColor: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Select Room/Venue",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Option: Use main facility
            FilterChip(
                selected = selectedSubVenue == null,
                onClick = { onSubVenueSelected(null) },
                label = { Text("Main Facility") },
                modifier = Modifier.fillMaxWidth(),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = primaryColor,
                    selectedLabelColor = Color.White
                )
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Subvenues (from facilityind)
            subVenues.forEach { subVenue ->
                Spacer(modifier = Modifier.height(8.dp))
                FilterChip(
                    selected = selectedSubVenue?.id == subVenue.id,
                    onClick = { onSubVenueSelected(subVenue) },
                    label = { 
                        Column {
                            Text(
                                text = subVenue.name,
                                fontWeight = FontWeight.Medium
                            )
                            if (subVenue.customMinNum > 0 || subVenue.customMaxNum > 0) {
                                Text(
                                    text = "Capacity: ${subVenue.customMinNum}-${subVenue.customMaxNum}",
                                    fontSize = 12.sp,
                                    color = Color.Gray
                                )
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = primaryColor,
                        selectedLabelColor = Color.White
                    )
                )
            }
        }
    }
}
