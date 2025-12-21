package com.example.miniproject.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.miniproject.reservation.Reservation
import com.example.miniproject.reservation.ReservationRepository
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingStatisticsScreen(navController: NavController) {
    val primaryColor = Color(0xFF5553DC)
    val reservationRepository = remember { ReservationRepository() }
    var reservations by remember { mutableStateOf<List<Reservation>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()
    
    LaunchedEffect(Unit) {
        scope.launch {
            try {
                reservations = reservationRepository.getAllReservations()
                isLoading = false
            } catch (e: Exception) {
                println("Error loading reservations: ${e.message}")
                isLoading = false
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Booking Statistics", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "Back")
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
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = primaryColor)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Statistics Summary Cards
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        title = "Total Bookings",
                        value = reservations.size.toString(),
                        color = primaryColor,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "This Month",
                        value = getMonthlyBookings(reservations).toString(),
                        color = Color(0xFF4CAF50),
                        modifier = Modifier.weight(1f)
                    )
                }
                
                // Bar Chart
                Text(
                    "Booking Usage by Facility",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                
                BookingBarChart(
                    reservations = reservations,
                    primaryColor = primaryColor
                )
                
                // Recent Bookings List
                Text(
                    "Recent Bookings",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                
                reservations.take(10).forEach { reservation ->
                    BookingListItem(reservation = reservation, primaryColor = primaryColor)
                }
            }
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                title,
                fontSize = 14.sp,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                value,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

@Composable
fun BookingBarChart(
    reservations: List<Reservation>,
    primaryColor: Color
) {
    val facilityCounts = reservations.groupBy { it.facilityID }
        .mapValues { it.value.size }
        .toList()
        .sortedByDescending { it.second }
        .take(10)
    
    val maxCount = facilityCounts.maxOfOrNull { it.second } ?: 1
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            facilityCounts.forEach { (facilityId, count) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        facilityId,
                        modifier = Modifier.width(100.dp),
                        fontSize = 12.sp,
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(24.dp)
                            .background(
                                color = primaryColor.copy(alpha = 0.3f),
                                shape = RoundedCornerShape(4.dp)
                            )
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth((count.toFloat() / maxCount))
                                .background(
                                    color = primaryColor,
                                    shape = RoundedCornerShape(4.dp)
                                )
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        count.toString(),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = primaryColor
                    )
                }
            }
        }
    }
}

@Composable
fun BookingListItem(
    reservation: Reservation,
    primaryColor: Color
) {
    val dateFormat = SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault())
    val bookedDate = reservation.bookedTime?.toDate()
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    reservation.facilityID,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                if (bookedDate != null) {
                    Text(
                        dateFormat.format(bookedDate),
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }
            Text(
                "${reservation.bookedHours}h",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = primaryColor
            )
        }
    }
}

fun getMonthlyBookings(reservations: List<Reservation>): Int {
    val calendar = Calendar.getInstance()
    val currentMonth = calendar.get(Calendar.MONTH)
    val currentYear = calendar.get(Calendar.YEAR)
    
    return reservations.count { reservation ->
        val bookedDate = reservation.bookedTime?.toDate()
        if (bookedDate != null) {
            val bookingCalendar = Calendar.getInstance().apply { time = bookedDate }
            bookingCalendar.get(Calendar.MONTH) == currentMonth &&
            bookingCalendar.get(Calendar.YEAR) == currentYear
        } else {
            false
        }
    }
}

