package com.example.miniproject.user.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.miniproject.R
import com.example.miniproject.auth.AuthRepository
import com.example.miniproject.auth.FirebaseManager
import com.example.miniproject.facility.FacilityRepository
import com.example.miniproject.reservation.Reservation
import com.example.miniproject.reservation.ReservationRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun UserHomeScreen(
    navController: NavController,
    onNavigateToBooking: () -> Unit = {}
) {
    val primaryColor = Color(0xFF5553DC)
    val scope = rememberCoroutineScope()
    
    var userName by remember { mutableStateOf("User") }
    var userBookings by remember { mutableStateOf<List<Reservation>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var currentTime by remember { mutableStateOf(getCurrentTime()) }
    var currentDate by remember { mutableStateOf(getCurrentDate()) }
    
    // Load user data and bookings
    LaunchedEffect(Unit) {
        scope.launch {
            try {
                val currentUser = FirebaseManager.auth.currentUser
                if (currentUser != null) {
                    val authRepository = AuthRepository()
                    val userData = authRepository.getUserData(currentUser.uid)
                    userName = userData?.name ?: "User"
                    
                    // Get displayId to query reservations (admin saves with displayId as userID)
                    val displayId = userData?.displayId?.takeIf { it.isNotBlank() } ?: currentUser.uid
                    
                    val reservationRepository = ReservationRepository()
                    // Query by displayId (which is stored as userID in reservation)
                    userBookings = reservationRepository.findReservationsByUserId(displayId)
                        .sortedByDescending { it.bookedTime?.seconds ?: 0L }
                        .take(5)
                    
                    println("✅ Loaded ${userBookings.size} bookings for user: $displayId")
                }
            } catch (e: Exception) {
                println("❌ Error loading home data: ${e.message}")
                e.printStackTrace()
            } finally {
                isLoading = false
            }
        }
    }
    
    // Update time every minute
    LaunchedEffect(Unit) {
        while (true) {
            delay(60000)
            currentTime = getCurrentTime()
            currentDate = getCurrentDate()
        }
    }
    
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.White
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    // Header with username (left) and time/date (right) - Aligned properly
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Left: Welcome username - Aligned to match right side structure
                        Column(
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text(
                                text = "Welcome,",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = primaryColor.copy(alpha = 0.8f)
                            )
                            Text(
                                text = userName,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = primaryColor
                            )
                        }
                        
                        // Right: Time and date
                        Column(
                            horizontalAlignment = Alignment.End,
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text(
                                text = currentTime,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = primaryColor
                            )
                            Text(
                                text = currentDate,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = primaryColor.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
                
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                }
                
                item {
                    // Quick Actions
                    Text(
                        text = "Quick Actions",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }
                
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        QuickActionCard(
                            title = "Book Now",
                            onClick = { onNavigateToBooking() },
                            primaryColor = primaryColor,
                            modifier = Modifier.weight(1f)
                        )
                        QuickActionCard(
                            title = "My Bookings",
                            onClick = { navController.navigate("my_booking") },
                            primaryColor = primaryColor,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                }
                
                item {
                    Text(
                        text = "Recent Bookings",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }
                
                if (isLoading) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = primaryColor)
                        }
                    }
                } else if (userBookings.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = primaryColor.copy(alpha = 0.1f)
                            ),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "No recent bookings",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color.Gray
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Book your first facility to get started",
                                    fontSize = 14.sp,
                                    color = Color.Gray,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                } else {
                    items(userBookings) { booking ->
                        BookingCard(booking = booking, primaryColor = primaryColor)
                    }
                }
                
                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
            
            // Floating Action Button - Properly aligned at bottom right
            FloatingActionButton(
                onClick = { onNavigateToBooking() },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(bottom = 90.dp, end = 20.dp), // Adjusted for bottom nav bar (70dp height + 20dp padding)
                containerColor = primaryColor,
                shape = CircleShape
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Add Booking",
                    modifier = Modifier.size(24.dp),
                    tint = Color.White
                )
            }
        }
    }
}

@Composable
fun QuickActionCard(
    title: String,
    onClick: () -> Unit,
    primaryColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.height(100.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = primaryColor
            )
        }
    }
}

@Composable
fun BookingCard(booking: Reservation, primaryColor: Color) {
    var facilityName by remember { mutableStateOf(booking.facilityID) }
    val scope = rememberCoroutineScope()
    
    LaunchedEffect(booking.facilityID) {
        scope.launch {
            try {
                val repository = FacilityRepository()
                val facility = repository.getFacility(booking.facilityID)
                facilityName = facility?.name ?: booking.facilityID
            } catch (e: Exception) {
                println("Error loading facility name: ${e.message}")
            }
        }
    }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(primaryColor.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = facilityName.take(1).uppercase(),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = primaryColor
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = facilityName,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(4.dp))
                booking.bookedTime?.toDate()?.let { date ->
                    Text(
                        text = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(date),
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
                Text(
                    text = "Duration: ${booking.bookedHours} hours",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
        }
    }
}

private fun getCurrentTime(): String {
    val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
    return sdf.format(Date())
}

private fun getCurrentDate(): String {
    val dateFormat = SimpleDateFormat("EEE, MMM d", Locale.getDefault())
    return dateFormat.format(Date())
}
