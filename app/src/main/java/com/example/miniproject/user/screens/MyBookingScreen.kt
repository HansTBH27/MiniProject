package com.example.miniproject.user.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.miniproject.auth.AuthRepository
import com.example.miniproject.auth.FirebaseManager
import com.example.miniproject.facility.FacilityRepository
import com.example.miniproject.reservation.Reservation
import com.example.miniproject.reservation.ReservationRepository
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyBookingScreen(navController: NavController) {
    val primaryColor = Color(0xFF5553DC)
    val scope = rememberCoroutineScope()
    
    var userBookings by remember { mutableStateOf<List<Reservation>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showCancelDialog by remember { mutableStateOf<Reservation?>(null) }
    var isCanceling by remember { mutableStateOf(false) }
    
    val dateFormat = remember { SimpleDateFormat("d MMM yyyy", Locale.getDefault()) }
    val timeFormat = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    
    val reservationRepository = remember { ReservationRepository() }
    val facilityRepository = remember { FacilityRepository() }
    
    // Load bookings
    fun loadBookings() {
        scope.launch {
            try {
                isLoading = true
                errorMessage = null
                
                val currentUser = FirebaseManager.auth.currentUser
                if (currentUser != null) {
                    val authRepository = AuthRepository()
                    val userData = authRepository.getUserData(currentUser.uid)
                    val displayId = userData?.displayId?.takeIf { it.isNotBlank() } ?: currentUser.uid
                    
                    println("🔍 Searching bookings for:")
                    println("   Display ID: $displayId")
                    println("   UID: ${currentUser.uid}")
                    
                    // Query by displayId (which is stored as userID in reservation)
                    val bookingsByDisplayId = reservationRepository.findReservationsByUserId(displayId)
                    
                    // Also query by uid in case some bookings use uid
                    val bookingsByUid = if (displayId != currentUser.uid) {
                        reservationRepository.findReservationsByUserId(currentUser.uid)
                    } else {
                        emptyList()
                    }
                    
                    // Combine and remove duplicates
                    userBookings = (bookingsByDisplayId + bookingsByUid)
                        .distinctBy { it.id }
                        .sortedByDescending { it.bookedTime?.seconds ?: 0L }
                    
                    println("✅ Loaded ${userBookings.size} bookings for user:")
                    println("   By displayId: ${bookingsByDisplayId.size}")
                    println("   By uid: ${bookingsByUid.size}")
                    userBookings.forEach { booking ->
                        println("   - Booking: ${booking.id}, Facility: ${booking.facilityID}, Time: ${booking.bookedTime}")
                    }
                }
            } catch (e: Exception) {
                errorMessage = "Failed to load bookings: ${e.message}"
                println("❌ Error loading bookings: ${e.message}")
            } finally {
                isLoading = false
            }
        }
    }
    
    // Cancel booking
    fun cancelBooking(reservation: Reservation) {
        scope.launch {
            try {
                isCanceling = true
                reservationRepository.deleteReservation(reservation.id)
                showCancelDialog = null
                loadBookings() // Reload bookings after cancellation
            } catch (e: Exception) {
                errorMessage = "Failed to cancel booking: ${e.message}"
                println("❌ Error canceling booking: ${e.message}")
            } finally {
                isCanceling = false
            }
        }
    }
    
    LaunchedEffect(Unit) {
        loadBookings()
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Bookings", fontWeight = FontWeight.Bold) },
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
                ),
                actions = {
                    IconButton(
                        onClick = { loadBookings() },
                        enabled = !isLoading
                    ) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = "Refresh",
                            tint = Color.White
                        )
                    }
                }
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
                            Text("Loading bookings...", color = Color.Gray)
                        }
                    }
                }
                
                errorMessage != null -> {
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
                                errorMessage ?: "Unknown error",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Red
                            )
                            Button(
                                onClick = { loadBookings() }
                            ) {
                                Text("Retry")
                            }
                        }
                    }
                }
                
                userBookings.isEmpty() -> {
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
                                Icons.Default.EventBusy,
                                contentDescription = "No Bookings",
                                tint = Color.Gray,
                                modifier = Modifier.size(64.dp)
                            )
                            Text(
                                "No Bookings",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                            Text(
                                "You haven't made any bookings yet",
                                fontSize = 14.sp,
                                color = Color.Gray
                            )
                            Button(
                                onClick = { navController.navigate("user_booking") }
                            ) {
                                Text("Browse Facilities")
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
                        items(userBookings) { booking ->
                            BookingDetailCard(
                                booking = booking,
                                facilityRepository = facilityRepository,
                                onCancel = { showCancelDialog = booking },
                                primaryColor = primaryColor,
                                dateFormat = dateFormat,
                                timeFormat = timeFormat
                            )
                        }
                    }
                }
            }
        }
    }
    
    // Cancel confirmation dialog
    if (showCancelDialog != null) {
        AlertDialog(
            onDismissRequest = { if (!isCanceling) showCancelDialog = null },
            title = { Text("Cancel Booking", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Are you sure you want to cancel this booking?")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "This action cannot be undone.",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { cancelBooking(showCancelDialog!!) },
                    enabled = !isCanceling,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Red
                    )
                ) {
                    if (isCanceling) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    } else {
                        Text("Cancel Booking")
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showCancelDialog = null },
                    enabled = !isCanceling
                ) {
                    Text("Keep Booking")
                }
            }
        )
    }
}

@Composable
fun BookingDetailCard(
    booking: Reservation,
    facilityRepository: FacilityRepository,
    onCancel: () -> Unit,
    primaryColor: Color,
    dateFormat: SimpleDateFormat,
    timeFormat: SimpleDateFormat
) {
    var facilityName by remember { mutableStateOf(booking.facilityID) }
    var isLoadingFacility by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()
    
    LaunchedEffect(booking.facilityID) {
        scope.launch {
            try {
                val facility = facilityRepository.getFacility(booking.facilityID)
                facilityName = facility?.name ?: booking.facilityID
            } catch (e: Exception) {
                println("Error loading facility: ${e.message}")
            } finally {
                isLoadingFacility = false
            }
        }
    }
    
    val bookedDate = booking.bookedTime?.toDate()
    val endDate = bookedDate?.let {
        Calendar.getInstance().apply {
            time = it
            add(Calendar.HOUR_OF_DAY, booking.bookedHours.toInt())
            add(Calendar.MINUTE, ((booking.bookedHours % 1) * 60).toInt())
        }.time
    }
    
    val isPastBooking = bookedDate?.before(Date()) ?: false
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isPastBooking) Color(0xFFF5F5F5) else Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    if (isLoadingFacility) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = primaryColor
                        )
                    } else {
                        Text(
                            text = facilityName,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    }
                    Text(
                        text = "Booking ID: ${booking.id}",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
                
                // Status badge
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isPastBooking) Color.Gray else primaryColor.copy(alpha = 0.1f)
                ) {
                    Text(
                        text = if (isPastBooking) "Past" else "Upcoming",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isPastBooking) Color.White else primaryColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            Divider(color = Color.LightGray)
            Spacer(modifier = Modifier.height(16.dp))
            
            // Booking details
            if (bookedDate != null) {
                BookingDetailRow(
                    icon = Icons.Default.CalendarToday,
                    label = "Date",
                    value = dateFormat.format(bookedDate),
                    primaryColor = primaryColor
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                BookingDetailRow(
                    icon = Icons.Default.Schedule,
                    label = "Time",
                    value = if (endDate != null) {
                        "${timeFormat.format(bookedDate)} - ${timeFormat.format(endDate)}"
                    } else {
                        timeFormat.format(bookedDate)
                    },
                    primaryColor = primaryColor
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                BookingDetailRow(
                    icon = Icons.Default.HourglassEmpty,
                    label = "Duration",
                    value = if (booking.bookedHours == 0.5 || booking.bookedHours == 1.5 || booking.bookedHours == 2.5) {
                        "${booking.bookedHours} Hours"
                    } else {
                        "${booking.bookedHours.toInt()} Hours"
                    },
                    primaryColor = primaryColor
                )
                
                // Show booking status
                Spacer(modifier = Modifier.height(8.dp))
                BookingDetailRow(
                    icon = Icons.Default.Info,
                    label = "Status",
                    value = if (isPastBooking) "Completed" else "Upcoming",
                    primaryColor = primaryColor
                )
            }
            
            // Cancel button (only for future bookings)
            if (!isPastBooking) {
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedButton(
                    onClick = onCancel,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color.Red
                    ),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        brush = androidx.compose.ui.graphics.SolidColor(Color.Red)
                    )
                ) {
                    Icon(
                        Icons.Default.Cancel,
                        contentDescription = "Cancel",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Cancel Booking")
                }
            }
        }
    }
}

@Composable
fun BookingDetailRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    primaryColor: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = label,
            tint = primaryColor,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = label,
                fontSize = 12.sp,
                color = Color.Gray
            )
            Text(
                text = value,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Black
            )
        }
    }
}

