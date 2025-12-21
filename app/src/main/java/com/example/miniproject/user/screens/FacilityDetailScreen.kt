package com.example.miniproject.user.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.miniproject.R
import com.example.miniproject.equipment.Equipment
import com.example.miniproject.equipment.EquipmentRepository
import com.example.miniproject.facility.Facility
import com.example.miniproject.facility.FacilityRepository
import kotlinx.coroutines.launch

@Composable
fun FacilityDetailScreen(
    facilityId: String,
    navController: NavController
) {
    val primaryColor = Color(0xFF5553DC)
    val scope = rememberCoroutineScope()
    val facilityRepository = remember { FacilityRepository() }
    val equipmentRepository = remember { EquipmentRepository() }

    var facility by remember { mutableStateOf<Facility?>(null) }
    var availableEquipment by remember { mutableStateOf<List<Equipment>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    // Load facility data from Firebase (synced with admin)
    LaunchedEffect(facilityId) {
        scope.launch {
            try {
                isLoading = true
                facility = facilityRepository.getFacility(facilityId)
                
                // Load equipment for this facility
                if (facility != null) {
                    availableEquipment = equipmentRepository.getAllEquipment()
                        .filter { it.facilityID == facilityId && it.quantity > 0 }
                }
            } catch (e: Exception) {
                println("Error loading facility details: ${e.message}")
            } finally {
                isLoading = false
            }
        }
    }

    if (isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                CircularProgressIndicator(color = primaryColor)
                Text("Loading facility details...", color = Color.Gray)
            }
        }
        return
    }

    if (facility == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(
                    Icons.Default.Warning,
                    contentDescription = "Error",
                    tint = Color.Red,
                    modifier = Modifier.size(48.dp)
                )
                Text(
                    "Facility not found",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Red
                )
                TextButton(onClick = { navController.popBackStack() }) {
                    Text("Go Back", color = primaryColor)
                }
            }
        }
        return
    }

    val currentFacility = facility!!
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(scrollState)
    ) {
        // Header with Back Button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(
                    Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = primaryColor
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Facility Details",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
        }

        // Facility Image Placeholder
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(270.dp)
                .padding(horizontal = 16.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFF0F0F0)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.logo),
                        contentDescription = "Facility Image",
                        tint = primaryColor,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = currentFacility.name,
                        color = Color.Gray,
                        fontSize = 16.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Facility Info Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F8F8)),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                // Facility Name
                Text(
                    text = currentFacility.name,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Location Badge
                if (currentFacility.location.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.Start)
                            .clip(RoundedCornerShape(8.dp))
                            .background(primaryColor.copy(alpha = 0.1f))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.LocationOn,
                                contentDescription = "Location",
                                tint = primaryColor,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = currentFacility.location,
                                color = primaryColor,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Description Section
                Text(
                    text = "Description",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (currentFacility.description.isNotEmpty()) {
                        currentFacility.description
                    } else {
                        "No description available for this facility."
                    },
                    fontSize = 14.sp,
                    color = Color.Gray,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Capacity
                Row(
                    verticalAlignment = Alignment.CenterVertically
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
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                        Text(
                            text = "${currentFacility.minNum}-${currentFacility.maxNum} people",
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Operating Hours
                if (currentFacility.startTime.isNotEmpty() && currentFacility.endTime.isNotEmpty()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Schedule,
                            contentDescription = "Operating Hours",
                            tint = primaryColor,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Operating Hours",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                            Text(
                                text = "${formatTime(currentFacility.startTime)} - ${formatTime(currentFacility.endTime)}",
                                fontSize = 14.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Equipment Section
                if (availableEquipment.isNotEmpty()) {
                    Text(
                        text = "Available Equipment",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    availableEquipment.forEach { equipment ->
                        EquipmentInfoItem(equipment = equipment, primaryColor = primaryColor)
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Book Now Button
                    Button(
                        onClick = {
                            navController.navigate("bookNow/$facilityId")
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "Book Now",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun EquipmentInfoItem(
    equipment: Equipment,
    primaryColor: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = equipment.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "RM${"%.2f".format(equipment.price)}/unit",
                    fontSize = 14.sp,
                    color = primaryColor,
                    fontWeight = FontWeight.Medium
                )
            }
            Text(
                text = "Qty: ${equipment.quantity}",
                fontSize = 14.sp,
                color = Color.Gray
            )
        }
    }
}

// Helper function to format time from "0800" to "08:00"
private fun formatTime(time: String): String {
    return if (time.length == 4) {
        "${time.substring(0, 2)}:${time.substring(2, 4)}"
    } else {
        time
    }
}

