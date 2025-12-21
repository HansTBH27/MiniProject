package com.example.miniproject.admin.bookingAdmin

import android.app.DatePickerDialog
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditReservationScreen(
    navController: NavController,
    reservationId: String? = null,
    viewModel: AddEditReservationViewModel = viewModel()
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    val reservationIdState by viewModel.reservationId.collectAsState()
    val displayId by viewModel.displayId.collectAsState()
    val displayIdValid by viewModel.displayIdValid.collectAsState()
    val displayIdChecking by viewModel.displayIdChecking.collectAsState()
    val selectedFacility by viewModel.selectedFacility.collectAsState()
    val facilities by viewModel.facilities.collectAsState()
    val selectedFacilityDetails by viewModel.selectedFacilityDetails.collectAsState()
    val bookedHours by viewModel.bookedHours.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val success by viewModel.success.collectAsState()
    val isEditMode by viewModel.isEditMode.collectAsState()
    val timeValidationError by viewModel.timeValidationError.collectAsState()
    val arenaAccessError by viewModel.arenaAccessError.collectAsState()
    val pastTimeError by viewModel.pastTimeError.collectAsState()
    val timeConflictError by viewModel.timeConflictError.collectAsState()

    val selectedDate by viewModel.selectedDate.collectAsState()
    val selectedTime by viewModel.selectedTime.collectAsState()

    var showFacilityDropdown by remember { mutableStateOf(false) }
    var showTimePickerDialog by remember { mutableStateOf(false) }

    val isSaveEnabled = remember {
        derivedStateOf {
            displayId.isNotBlank() &&
                    displayIdValid &&
                    selectedFacility != null &&
                    selectedDate != null &&
                    selectedTime != null &&
                    timeValidationError == null &&
                    arenaAccessError == null &&
                    pastTimeError == null &&
                    timeConflictError == null
        }
    }

    LaunchedEffect(reservationId) {
        if (reservationId != null) {
            viewModel.loadReservation(reservationId)
        }
    }

    LaunchedEffect(success) {
        if (success) {
            navController.popBackStack()
        }
    }

    // Time Picker Dialog
    if (showTimePickerDialog) {
        TimeWheelPickerDialog(
            currentTime = selectedTime,
            onDismiss = { showTimePickerDialog = false },
            onTimeSelected = { hour, minute ->
                viewModel.setSelectedTime(hour, minute)
                showTimePickerDialog = false
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF483D8B), Color(0xFF6A5ACD))
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 140.dp)
                .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                .background(Color.White)
                .padding(24.dp)
                .verticalScroll(scrollState)
        ) {
            if (error != null) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFFFEBEE)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Filled.Error,
                            contentDescription = "Error",
                            tint = Color(0xFFD32F2F)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            error!!,
                            color = Color(0xFFD32F2F),
                            fontSize = 14.sp
                        )
                    }
                }
            }

            OutlinedTextField(
                value = reservationIdState,
                onValueChange = { },
                label = { Text("Reservation ID") },
                leadingIcon = {
                    Icon(Icons.Filled.Badge, contentDescription = "ID")
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = false,
                colors = OutlinedTextFieldDefaults.colors(
                    disabledTextColor = MaterialTheme.colorScheme.onSurface,
                    disabledBorderColor = MaterialTheme.colorScheme.outline,
                    disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    disabledLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            Column {
                OutlinedTextField(
                    value = displayId,
                    onValueChange = { viewModel.setDisplayId(it) },
                    label = { Text("User Display ID") },
                    leadingIcon = {
                        Icon(Icons.Filled.Person, contentDescription = "User")
                    },
                    trailingIcon = {
                        if (displayIdChecking) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                        } else if (displayId.isNotBlank()) {
                            Icon(
                                imageVector = if (displayIdValid) Icons.Filled.CheckCircle else Icons.Filled.Error,
                                contentDescription = if (displayIdValid) "Valid" else "Invalid",
                                tint = if (displayIdValid) Color(0xFF4CAF50) else Color(0xFFF44336)
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = displayId.isNotBlank() && !displayIdValid && !displayIdChecking
                )

                if (displayId.isNotBlank() && !displayIdValid && !displayIdChecking) {
                    Row(
                        modifier = Modifier.padding(start = 16.dp, top = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Filled.Warning,
                            contentDescription = "Warning",
                            tint = Color(0xFFF44336),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            "Display ID not found in user collection",
                            color = Color(0xFFF44336),
                            fontSize = 12.sp
                        )
                    }
                } else if (displayId.isNotBlank() && displayIdValid) {
                    Row(
                        modifier = Modifier.padding(start = 16.dp, top = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Filled.CheckCircle,
                            contentDescription = "Valid",
                            tint = Color(0xFF4CAF50),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            "✓ Valid Display ID",
                            color = Color(0xFF4CAF50),
                            fontSize = 12.sp
                        )
                    }
                }

                if (displayId.isEmpty()) {
                    Text(
                        "Enter the user's Display ID (not the document ID)",
                        color = Color(0xFF757575),
                        fontSize = 11.sp,
                        modifier = Modifier.padding(start = 16.dp, top = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Column {
                ExposedDropdownMenuBox(
                    expanded = showFacilityDropdown,
                    onExpandedChange = { showFacilityDropdown = it }
                ) {
                    OutlinedTextField(
                        value = selectedFacility?.name ?: "Select Facility",
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("Facility") },
                        leadingIcon = {
                            Icon(Icons.Filled.LocationOn, contentDescription = "Facility")
                        },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = showFacilityDropdown)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )

                    ExposedDropdownMenu(
                        expanded = showFacilityDropdown,
                        onDismissRequest = { showFacilityDropdown = false }
                    ) {
                        facilities.forEach { facility ->
                            DropdownMenuItem(
                                text = { Text(facility.name) },
                                onClick = {
                                    viewModel.setSelectedFacility(facility)
                                    showFacilityDropdown = false
                                }
                            )
                        }
                    }
                }

                if (selectedFacilityDetails != null) {
                    Column(modifier = Modifier.padding(start = 16.dp, top = 8.dp)) {
                        val location = when {
                            selectedFacilityDetails!!.id.startsWith("S", ignoreCase = true) -> "Sports Complex"
                            selectedFacilityDetails!!.id.startsWith("CC", ignoreCase = true) -> "CITC"
                            selectedFacilityDetails!!.id.startsWith("L", ignoreCase = true) -> "Library"
                            selectedFacilityDetails!!.id.startsWith("AL", ignoreCase = true) ||
                                    selectedFacilityDetails!!.id.startsWith("AS", ignoreCase = true) -> "Arena TARUMT"
                            selectedFacilityDetails!!.id.startsWith("C", ignoreCase = true) -> "Clubhouse"
                            else -> "Unknown Location"
                        }
                        Text(
                            text = "Location: $location",
                            fontSize = 12.sp,
                            color = Color(0xFF483D8B),
                            fontWeight = FontWeight.Medium
                        )
                        val hours = viewModel.getFacilityHours()
                        if (hours.isNotEmpty()) {
                            Text(
                                text = hours,
                                fontSize = 12.sp,
                                color = Color(0xFF483D8B)
                            )
                        }
                    }
                }

                if (arenaAccessError != null) {
                    Row(
                        modifier = Modifier.padding(start = 16.dp, top = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Filled.Warning,
                            contentDescription = "Arena Access Error",
                            tint = Color(0xFFF44336),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            arenaAccessError!!,
                            color = Color(0xFFF44336),
                            fontSize = 12.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = viewModel.getFormattedDate(),
                onValueChange = { },
                label = { Text("Date") },
                leadingIcon = {
                    Icon(Icons.Filled.CalendarToday, contentDescription = "Date")
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
                                    viewModel.setSelectedDate(selectedCalendar.timeInMillis)
                                },
                                calendar.get(Calendar.YEAR),
                                calendar.get(Calendar.MONTH),
                                calendar.get(Calendar.DAY_OF_MONTH)
                            ).show()
                        }
                    ) {
                        Icon(Icons.Filled.DateRange, contentDescription = "Pick Date")
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                readOnly = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            // ✅ Time Picker with Wheel Dialog
            Column {
                OutlinedTextField(
                    value = viewModel.getFormattedTime(),
                    onValueChange = { },
                    label = { Text("Start Time") },
                    leadingIcon = {
                        Icon(Icons.Filled.AccessTime, contentDescription = "Time")
                    },
                    trailingIcon = {
                        IconButton(
                            onClick = { showTimePickerDialog = true }
                        ) {
                            Icon(Icons.Filled.Schedule, contentDescription = "Pick Time")
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    readOnly = true,
                    isError = timeValidationError != null || pastTimeError != null || timeConflictError != null
                )

                if (timeConflictError != null) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFFFF3E0)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Filled.Schedule,
                                contentDescription = "Time Conflict",
                                tint = Color(0xFFF57C00),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    "⏰ Time Slot Unavailable",
                                    color = Color(0xFFF57C00),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    timeConflictError!!,
                                    color = Color(0xFFF57C00),
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        }
                    }
                }

                if (pastTimeError != null) {
                    Row(
                        modifier = Modifier.padding(start = 16.dp, top = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Filled.Warning,
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

                if (timeValidationError != null && pastTimeError == null) {
                    Row(
                        modifier = Modifier.padding(start = 16.dp, top = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Filled.Warning,
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

                val endTime = viewModel.getReservationEndTime()
                if (endTime.isNotEmpty()) {
                    Text(
                        text = "Reservation End Time: $endTime",
                        fontSize = 12.sp,
                        color = if (timeValidationError == null && pastTimeError == null && timeConflictError == null)
                            Color(0xFF483D8B) else Color(0xFFF44336),
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                "Booked Hours",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF483D8B)
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(0.5, 1.0, 1.5).forEach { hours ->
                    FilterChip(
                        selected = bookedHours == hours,
                        onClick = { viewModel.setBookedHours(hours) },
                        label = {
                            Text(
                                text = when (hours) {
                                    0.5 -> "0.5 Hour"
                                    1.0 -> "1 Hour"
                                    1.5 -> "1.5 Hours"
                                    else -> "${hours} Hours"
                                },
                                fontSize = 13.sp
                            )
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(2.0, 2.5, 3.0).forEach { hours ->
                    FilterChip(
                        selected = bookedHours == hours,
                        onClick = { viewModel.setBookedHours(hours) },
                        label = {
                            Text(
                                text = when (hours) {
                                    2.0 -> "2 Hours"
                                    2.5 -> "2.5 Hours"
                                    3.0 -> "3 Hours"
                                    else -> "${hours} Hours"
                                },
                                fontSize = 13.sp
                            )
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    viewModel.saveReservation()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = isSaveEnabled.value && !isLoading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF483D8B),
                    disabledContainerColor = Color(0xFF483D8B).copy(alpha = 0.4f)
                )
            ) {
                if (isLoading) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            "Saving Reservation...",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                } else {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Filled.CheckCircle,
                            contentDescription = "Confirm",
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            if (isEditMode) "Update Reservation" else "Confirm Reservation",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            if (!isSaveEnabled.value && !isLoading) {
                Spacer(modifier = Modifier.height(12.dp))
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFFFF9C4)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Filled.Info,
                                contentDescription = "Info",
                                tint = Color(0xFFF57C00),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Please complete all requirements:",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFFF57C00)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))

                        if (displayId.isBlank()) {
                            Text("• Enter User Display ID", fontSize = 12.sp, color = Color(0xFF757575))
                        } else if (!displayIdValid) {
                            Text("• Valid User Display ID required", fontSize = 12.sp, color = Color(0xFF757575))
                        }

                        if (selectedFacility == null) {
                            Text("• Select a Facility", fontSize = 12.sp, color = Color(0xFF757575))
                        }

                        if (selectedDate == null) {
                            Text("• Select a Date", fontSize = 12.sp, color = Color(0xFF757575))
                        }

                        if (selectedTime == null) {
                            Text("• Select a Time", fontSize = 12.sp, color = Color(0xFF757575))
                        }

                        if (timeValidationError != null) {
                            Text("• Time must be within facility hours", fontSize = 12.sp, color = Color(0xFF757575))
                        }

                        if (arenaAccessError != null) {
                            Text("• Staff access required for Arena", fontSize = 12.sp, color = Color(0xFF757575))
                        }

                        if (pastTimeError != null) {
                            Text("• Select a future date/time", fontSize = 12.sp, color = Color(0xFF757575))
                        }

                        if (timeConflictError != null) {
                            Text("• Time slot already booked for this facility", fontSize = 12.sp, color = Color(0xFF757575))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

// ============================================
// TIME WHEEL PICKER DIALOG
// ============================================
@Composable
fun TimeWheelPickerDialog(
    currentTime: Pair<Int, Int>?,
    onDismiss: () -> Unit,
    onTimeSelected: (Int, Int) -> Unit
) {
    // Generate time slots: XX:00 and XX:30 for hours 0-23
    val timeSlots = remember {
        (0..23).flatMap { hour ->
            listOf(
                Pair(hour, 0),
                Pair(hour, 30)
            )
        }
    }

    val initialIndex = remember(currentTime) {
        if (currentTime != null) {
            timeSlots.indexOfFirst { it.first == currentTime.first && it.second == currentTime.second }
                .takeIf { it >= 0 } ?: 0
        } else {
            0
        }
    }

    var selectedIndex by remember { mutableIntStateOf(initialIndex) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Select Time",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF483D8B)
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Time Wheel
                TimeWheelPicker(
                    timeSlots = timeSlots,
                    selectedIndex = selectedIndex,
                    onIndexChanged = { selectedIndex = it }
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = {
                            val (hour, minute) = timeSlots[selectedIndex]
                            onTimeSelected(hour, minute)
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF483D8B)
                        )
                    ) {
                        Text("OK")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TimeWheelPicker(
    timeSlots: List<Pair<Int, Int>>,
    selectedIndex: Int,
    onIndexChanged: (Int) -> Unit
) {
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = selectedIndex)
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(selectedIndex) {
        coroutineScope.launch {
            listState.animateScrollToItem(selectedIndex)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
    ) {
        // Selection indicator
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .align(Alignment.Center)
                .background(Color(0xFF483D8B).copy(alpha = 0.1f), RoundedCornerShape(8.dp))
        )

        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = 75.dp),
            flingBehavior = rememberSnapFlingBehavior(lazyListState = listState)
        ) {
            items(timeSlots.size) { index ->
                val (hour, minute) = timeSlots[index]
                val isSelected = index == listState.firstVisibleItemIndex

                LaunchedEffect(listState.firstVisibleItemIndex) {
                    if (listState.firstVisibleItemIndex == index) {
                        onIndexChanged(index)
                    }
                }

                Text(
                    text = String.format("%02d:%02d", hour, minute),
                    fontSize = if (isSelected) 28.sp else 20.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = if (isSelected) Color(0xFF483D8B) else Color.Gray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .wrapContentHeight()
                        .alpha(if (isSelected) 1f else 0.5f)
                )
            }
        }
    }
}