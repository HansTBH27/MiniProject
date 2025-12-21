package com.example.miniproject.user.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.miniproject.R
import com.example.miniproject.facility.Facility
import com.example.miniproject.facility.FacilityRepository
import kotlinx.coroutines.launch

@Composable
fun UserBookingScreen(navController: NavController) {
    val primaryColor = Color(0xFF5553DC)
    val scope = rememberCoroutineScope()
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    
    var facilities by remember { mutableStateOf<List<Facility>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var searchText by remember { mutableStateOf("") }
    var isSearchFocused by remember { mutableStateOf(false) }
    var selectedFilter by remember { mutableStateOf("All") }
    
    // Load facilities from facility collection (matching admin format)
    // This ensures facilities created by admin appear in booking screen
    // Both admin and user use the same "facility" collection and Facility model
    LaunchedEffect(Unit) {
        scope.launch {
            try {
                isLoading = true
                val repository = FacilityRepository()
                facilities = repository.getAllFacilities()
                println("✅ UserBookingScreen: Loaded ${facilities.size} facilities from 'facility' collection")
                // Verify data format matches admin
                facilities.forEach { facility ->
                    println("  - Facility: ${facility.name} (ID: ${facility.id}), Location: ${facility.location}, Hours: ${facility.startTime}-${facility.endTime}")
                }
            } catch (e: Exception) {
                println("❌ Error loading facilities: ${e.message}")
                e.printStackTrace()
            } finally {
                isLoading = false
            }
        }
    }
    
    // Filter facilities
    val filteredFacilities = remember(searchText, selectedFilter, facilities) {
        var filtered = facilities
        
        if (searchText.isNotEmpty()) {
            filtered = filtered.filter {
                it.name.contains(searchText, ignoreCase = true) ||
                it.description.contains(searchText, ignoreCase = true) ||
                it.location.contains(searchText, ignoreCase = true)
            }
        }
        
        // Filter by availability status if needed (can be extended)
        // Currently showing all facilities
        
        filtered
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(bottom = 90.dp)
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) {
                focusManager.clearFocus()
            }
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                // Header
                Text(
                    text = "Booking",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = primaryColor,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
            
            item {
                // Search Bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(55.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color.White)
                        .padding(1.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .clip(RoundedCornerShape(4.dp))
                            .background(
                                if (isSearchFocused) Color.Black else Color.Black.copy(alpha = 0.8f)
                            )
                            .padding(if (isSearchFocused) 1.5.dp else 1.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color.White)
                                .padding(horizontal = 12.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "Search",
                                    tint = Color.Black,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                BasicTextField(
                                    value = searchText,
                                    onValueChange = { searchText = it },
                                    textStyle = TextStyle(
                                        color = Color.Black,
                                        fontSize = 16.sp
                                    ),
                                    cursorBrush = SolidColor(Color.Black),
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Text,
                                        imeAction = ImeAction.Done
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .focusRequester(focusRequester)
                                        .onFocusChanged { focusState ->
                                            isSearchFocused = focusState.isFocused
                                        },
                                    singleLine = true,
                                    decorationBox = { innerTextField ->
                                        Box(
                                            modifier = Modifier.fillMaxWidth(),
                                            contentAlignment = Alignment.CenterStart
                                        ) {
                                            if (searchText.isEmpty()) {
                                                Text(
                                                    text = "Search Facility",
                                                    color = Color.Gray,
                                                    fontSize = 16.sp
                                                )
                                            }
                                            innerTextField()
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
            
            item {
                Spacer(modifier = Modifier.height(8.dp))
            }
            
            item {
                // Filter Chips
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        FilterChip(
                            text = "All",
                            isSelected = selectedFilter == "All",
                            onClick = {
                                selectedFilter = "All"
                                focusManager.clearFocus()
                            },
                            primaryColor = primaryColor
                        )
                    }
                }
            }
            
            item {
                Spacer(modifier = Modifier.height(8.dp))
            }
            
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Available Facilities",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    // Refresh button to reload facilities
                    IconButton(
                        onClick = {
                            scope.launch {
                                try {
                                    isLoading = true
                                    val repository = FacilityRepository()
                                    facilities = repository.getAllFacilities()
                                    println("🔄 Manually refreshed: ${facilities.size} facilities")
                                } catch (e: Exception) {
                                    println("❌ Error refreshing: ${e.message}")
                                } finally {
                                    isLoading = false
                                }
                            }
                        }
                    ) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = "Refresh",
                            tint = primaryColor
                        )
                    }
                }
            }
            
            if (isLoading) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            CircularProgressIndicator(color = primaryColor)
                            Text(
                                text = "Loading facilities...",
                                fontSize = 16.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }
            } else if (filteredFacilities.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No facilities found",
                            fontSize = 18.sp,
                            color = Color.Gray,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        Text(
                            text = "Try changing your search term",
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                    }
                }
            } else {
                items(filteredFacilities) { facility ->
                    FacilityCard(
                        facility = facility,
                        primaryColor = primaryColor,
                        navController = navController,
                        onClick = {
                            navController.navigate("bookNow/${facility.id}")
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun FilterChip(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    primaryColor: Color
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(
                color = if (isSelected) primaryColor else Color(0xFFF5F5F5)
            )
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = text,
            color = if (isSelected) Color.White else Color.Black,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
        )
    }
}

@Composable
fun FacilityCard(
    facility: Facility,
    primaryColor: Color,
    navController: NavController,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Category badge (if available)
            if (facility.location.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .align(Alignment.Start)
                        .clip(RoundedCornerShape(8.dp))
                        .background(primaryColor.copy(alpha = 0.1f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = facility.location.uppercase(),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = primaryColor
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Facility Name
            Text(
                text = facility.name,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier.padding(bottom = 6.dp)
            )
            
            // Facility Description
            if (facility.description.isNotEmpty()) {
                Text(
                    text = facility.description,
                    fontSize = 14.sp,
                    color = Color.Gray,
                    maxLines = 2,
                    lineHeight = 18.sp,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Capacity
            Text(
                text = "Capacity: ${facility.minNum}-${facility.maxNum} people",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = primaryColor,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            
            // Action Buttons Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // View Button
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFF5F5F5))
                        .clickable {
                            navController.navigate("user_facility_detail/${facility.id}")
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = "View Details",
                            tint = primaryColor,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "View",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = primaryColor
                        )
                    }
                }
                
                // Book Now Button
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(primaryColor)
                        .clickable { onClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Book Now",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}
