package com.example.miniproject.payment

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.miniproject.auth.AuthRepository
import com.example.miniproject.auth.FirebaseManager
import com.example.miniproject.facility.FacilityRepository
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingConfirmationScreen(
    navController: NavController,
    viewModel: PaymentViewModel = viewModel()
) {
    val primaryColor = Color(0xFF5553DC)
    val scope = rememberCoroutineScope()
    
    // State
    var userName by remember { mutableStateOf("User") }
    var facilityName by remember { mutableStateOf("") }
    var facilityLocation by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    // Get booking data from ViewModel
    val facility = viewModel.facility
    val facilityInd = viewModel.facilityInd
    val equipmentItems = viewModel.equipmentItems
    val startTime = viewModel.startTime
    val bookedHours = viewModel.bookedHours
    val endTime = viewModel.endTime
    
    // Calculate total amount
    val facilityPrice = (facilityInd?.get("price") as? Number)?.toDouble() ?: 0.0
    val equipmentTotal = equipmentItems.sumOf { it.unitPrice * it.purchaseQuantity }
    val totalAmount = (facilityPrice * bookedHours) + equipmentTotal
    
    // Date formatters
    val dateFormat = remember { SimpleDateFormat("d MMM yyyy", Locale.getDefault()) }
    val timeFormat = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    
    // Load user data and facility details
    LaunchedEffect(Unit) {
        scope.launch {
            try {
                isLoading = true
                
                // Get user name
                val currentUser = FirebaseManager.auth.currentUser
                if (currentUser != null) {
                    val authRepository = AuthRepository()
                    val userData = authRepository.getUserData(currentUser.uid)
                    userName = userData?.name ?: "User"
                }
                
                // Get facility name and location
                val facilityId = facilityInd?.get("facilityID") as? String
                if (facilityId != null) {
                    val facilityRepository = FacilityRepository()
                    val facilityData = facilityRepository.getFacility(facilityId)
                    facilityName = facilityData?.name ?: (facilityInd["name"] as? String ?: "Unknown Facility")
                    facilityLocation = facilityData?.location ?: ""
                } else {
                    facilityName = facilityInd?.get("name") as? String ?: "Unknown Facility"
                }
                
            } catch (e: Exception) {
                errorMessage = "Failed to load booking details: ${e.message}"
                println("❌ Error loading confirmation data: ${e.message}")
            } finally {
                isLoading = false
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Booking Confirmation", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            navController.navigate("main") {
                                popUpTo("main") { inclusive = true }
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
                            Text("Loading booking details...", color = Color.Gray)
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
                                color = Color.Red,
                                textAlign = TextAlign.Center
                            )
                            Button(
                                onClick = {
                                    navController.navigate("main") {
                                        popUpTo("main") { inclusive = true }
                                    }
                                }
                            ) {
                                Text("Go to Home")
                            }
                        }
                    }
                }
                
                else -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Success Icon
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Card(
                                modifier = Modifier.size(80.dp),
                                shape = RoundedCornerShape(40.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = Color(0xFF4CAF50).copy(alpha = 0.1f)
                                )
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.CheckCircle,
                                        contentDescription = "Success",
                                        tint = Color(0xFF4CAF50),
                                        modifier = Modifier.size(48.dp)
                                    )
                                }
                            }
                        }
                        
                        // Success Message
                        Text(
                            text = "Booking Confirmed!",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                        
                        Text(
                            text = "Your booking has been successfully confirmed. Details are shown below.",
                            fontSize = 14.sp,
                            color = Color.Gray,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // User Information Card
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFFF8F8F8)
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(bottom = 12.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Person,
                                        contentDescription = "User",
                                        tint = primaryColor,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "User Information",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Black
                                    )
                                }
                                
                                Divider(color = Color.LightGray)
                                Spacer(modifier = Modifier.height(12.dp))
                                
                                BookingDetailRow(
                                    label = "Name",
                                    value = userName,
                                    primaryColor = primaryColor
                                )
                            }
                        }
                        
                        // Facility Information Card
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFFF8F8F8)
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(bottom = 12.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Place,
                                        contentDescription = "Facility",
                                        tint = primaryColor,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Facility Information",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Black
                                    )
                                }
                                
                                Divider(color = Color.LightGray)
                                Spacer(modifier = Modifier.height(12.dp))
                                
                                BookingDetailRow(
                                    label = "Facility Name",
                                    value = facilityName,
                                    primaryColor = primaryColor
                                )
                                
                                if (facilityLocation.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    BookingDetailRow(
                                        label = "Location",
                                        value = facilityLocation,
                                        primaryColor = primaryColor
                                    )
                                }
                                
                                // Room/Venue name if available
                                val roomName = facilityInd?.get("name") as? String
                                if (roomName != null && roomName != facilityName) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    BookingDetailRow(
                                        label = "Room/Venue",
                                        value = roomName,
                                        primaryColor = primaryColor
                                    )
                                }
                            }
                        }
                        
                        // Time Slot Information Card
                        if (startTime != null) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = Color(0xFFF8F8F8)
                                ),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(bottom = 12.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Schedule,
                                            contentDescription = "Time",
                                            tint = primaryColor,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Time Slot",
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.Black
                                        )
                                    }
                                    
                                    Divider(color = Color.LightGray)
                                    Spacer(modifier = Modifier.height(12.dp))
                                    
                                    val startDate = startTime.toDate()
                                    val endDate = endTime?.toDate()
                                    
                                    BookingDetailRow(
                                        label = "Date",
                                        value = dateFormat.format(startDate),
                                        primaryColor = primaryColor
                                    )
                                    
                                    Spacer(modifier = Modifier.height(8.dp))
                                    BookingDetailRow(
                                        label = "Start Time",
                                        value = timeFormat.format(startDate),
                                        primaryColor = primaryColor
                                    )
                                    
                                    if (endDate != null) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        BookingDetailRow(
                                            label = "End Time",
                                            value = timeFormat.format(endDate),
                                            primaryColor = primaryColor
                                        )
                                    }
                                    
                                    Spacer(modifier = Modifier.height(8.dp))
                                    BookingDetailRow(
                                        label = "Duration",
                                        value = if (bookedHours == 0.5 || bookedHours == 1.5 || bookedHours == 2.5) {
                                            "${bookedHours} Hours"
                                        } else {
                                            "${bookedHours.toInt()} Hours"
                                        },
                                        primaryColor = primaryColor
                                    )
                                }
                            }
                        }
                        
                        // Rental Equipment Card (if any)
                        if (equipmentItems.isNotEmpty()) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = Color(0xFFF8F8F8)
                                ),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(bottom = 12.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.ShoppingCart,
                                            contentDescription = "Equipment",
                                            tint = primaryColor,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Rental Equipment",
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.Black
                                        )
                                    }
                                    
                                    Divider(color = Color.LightGray)
                                    Spacer(modifier = Modifier.height(12.dp))
                                    
                                    equipmentItems.forEach { equipment ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 4.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = equipment.name,
                                                    fontSize = 14.sp,
                                                    fontWeight = FontWeight.Medium,
                                                    color = Color.Black
                                                )
                                                Text(
                                                    text = "Quantity: ${equipment.purchaseQuantity} × RM ${String.format("%.2f", equipment.unitPrice)}",
                                                    fontSize = 12.sp,
                                                    color = Color.Gray
                                                )
                                            }
                                            Text(
                                                text = "RM ${String.format("%.2f", equipment.unitPrice * equipment.purchaseQuantity)}",
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = primaryColor
                                            )
                                        }
                                        
                                        if (equipment != equipmentItems.last()) {
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Divider(color = Color.LightGray.copy(alpha = 0.5f))
                                        }
                                    }
                                    
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Divider(color = Color.LightGray)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "Equipment Total",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.Black
                                        )
                                        Text(
                                            text = "RM ${String.format("%.2f", equipmentTotal)}",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = primaryColor
                                        )
                                    }
                                }
                            }
                        }
                        
                        // Payment Summary Card
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
                                        Icons.Default.Payment,
                                        contentDescription = "Payment",
                                        tint = primaryColor,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Payment Summary",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Black
                                    )
                                }
                                
                                Divider(color = Color.LightGray)
                                Spacer(modifier = Modifier.height(12.dp))
                                
                                if (facilityPrice > 0) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "Facility Rental (${if (bookedHours == 0.5 || bookedHours == 1.5 || bookedHours == 2.5) "${bookedHours} hrs" else "${bookedHours.toInt()} hrs"})",
                                            fontSize = 14.sp,
                                            color = Color.Gray
                                        )
                                        Text(
                                            text = "RM ${String.format("%.2f", facilityPrice * bookedHours)}",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = Color.Black
                                        )
                                    }
                                    
                                    if (equipmentTotal > 0) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                    }
                                }
                                
                                if (equipmentTotal > 0) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "Equipment Rental",
                                            fontSize = 14.sp,
                                            color = Color.Gray
                                        )
                                        Text(
                                            text = "RM ${String.format("%.2f", equipmentTotal)}",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = Color.Black
                                        )
                                    }
                                }
                                
                                Spacer(modifier = Modifier.height(12.dp))
                                Divider(color = Color.LightGray)
                                Spacer(modifier = Modifier.height(12.dp))
                                
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Total Amount",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Black
                                    )
                                    Text(
                                        text = "RM ${String.format("%.2f", totalAmount)}",
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = primaryColor
                                    )
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Action Buttons
                        Button(
                            onClick = {
                                navController.navigate("main") {
                                    popUpTo("main") { inclusive = true }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = primaryColor
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    "Go to Home",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(
                                    Icons.Default.Home,
                                    contentDescription = "Home",
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        
                        TextButton(
                            onClick = {
                                navController.navigate("user_booking") {
                                    popUpTo("main") { inclusive = false }
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                "View My Bookings",
                                fontSize = 16.sp,
                                color = primaryColor
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun BookingDetailRow(
    label: String,
    value: String,
    primaryColor: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
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

