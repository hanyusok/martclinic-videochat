package com.example.martclinic_videochat.presentation.ui

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.martclinic_videochat.domain.model.Schedule
import com.example.martclinic_videochat.presentation.viewmodel.BookingViewModel
import com.example.martclinic_videochat.util.DateTimeUtil

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun BookingScreen(
    viewModel: BookingViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val dates by viewModel.availableDates.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()
    val schedules by viewModel.schedules.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val bookingSuccess by viewModel.bookingSuccess.collectAsState()

    var selectedSchedule by remember { mutableStateOf<Schedule?>(null) }
    var symptomsText by remember { mutableStateOf("") }
    var showConfirmationDialog by remember { mutableStateOf(false) }

    // Reset fields upon successful booking
    LaunchedEffect(bookingSuccess) {
        if (bookingSuccess == true) {
            Toast.makeText(context, "진료 예약이 완료되었습니다!", Toast.LENGTH_SHORT).show()
            selectedSchedule = null
            symptomsText = ""
            viewModel.resetBookingStatus()
        } else if (bookingSuccess == false) {
            Toast.makeText(context, "예약에 실패했습니다. 다시 시도해 주세요.", Toast.LENGTH_SHORT).show()
            viewModel.resetBookingStatus()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("진료 예약", fontWeight = FontWeight.Bold) })
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                // Section: Date Selector
                Text(
                    text = "진료 날짜 선택",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)
                ) {
                    items(dates) { date ->
                        val isSelected = date == selectedDate
                        val formattedDate = DateTimeUtil.formatDateToKorean(date)
                        FilterChip(
                            selected = isSelected,
                            onClick = { 
                                viewModel.selectDate(date)
                                selectedSchedule = null // Reset schedule on date change
                            },
                            label = { Text(formattedDate) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }
                }

                // Section: Time Slots Grid
                Text(
                    text = "진료 시간 선택",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                if (schedules.isEmpty()) {
                    Text(
                        text = "해당 날짜에 선택 가능한 진료 일정이 없습니다.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 24.dp)
                    )
                } else {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        maxItemsInEachRow = 4,
                        modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)
                    ) {
                        schedules.forEach { schedule ->
                            val isSelected = schedule.id == selectedSchedule?.id
                            val isBooked = !schedule.is_available
                            val formattedTime = DateTimeUtil.formatTimeToKorean(schedule.start_time)
                            
                            SuggestionChip(
                                onClick = { 
                                    if (!isBooked) {
                                        selectedSchedule = schedule
                                    }
                                },
                                label = { 
                                    Text(
                                        text = if (isBooked) "$formattedTime (예약완료)" else formattedTime,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                enabled = !isBooked,
                                colors = SuggestionChipDefaults.suggestionChipColors(
                                    containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                                    labelColor = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else if (isBooked) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.onSurface
                                )
                            )
                        }
                    }
                }

                // Section: Symptoms description
                Text(
                    text = "증상 설명 입력",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                OutlinedTextField(
                    value = symptomsText,
                    onValueChange = { symptomsText = it },
                    placeholder = { Text("증상을 자세히 입력해주세요. (예: 어제부터 기침이 나고 열이 있습니다.)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .padding(bottom = 24.dp),
                    shape = RoundedCornerShape(12.dp)
                )

                // Section: Book Action Button
                Button(
                    onClick = { showConfirmationDialog = true },
                    enabled = selectedSchedule != null && symptomsText.isNotBlank(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("예약 신청하기", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                }
            }

            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
        }
    }

    // Confirmation dialog
    if (showConfirmationDialog) {
        val schedule = selectedSchedule
        if (schedule != null) {
            val dateFormatted = DateTimeUtil.formatDateToKorean(schedule.date)
            val timeFormatted = DateTimeUtil.formatTimeToKorean(schedule.start_time)
            
            AlertDialog(
                onDismissRequest = { showConfirmationDialog = false },
                title = { Text("진료 예약 확인") },
                text = {
                    Text("일시: $dateFormatted $timeFormatted\n\n위 일정으로 비대면 진료 예약을 진행하시겠습니까?")
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showConfirmationDialog = false
                            viewModel.bookAppointment(schedule, symptomsText)
                        }
                    ) {
                        Text("예약 확정")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showConfirmationDialog = false }) {
                        Text("취소")
                    }
                }
            )
        }
    }
}
