package com.example.martclinic_videochat.presentation.ui

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.martclinic_videochat.presentation.viewmodel.BookingViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun BookingScreen(
    viewModel: BookingViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val patients by viewModel.patients.collectAsState()
    val selectedPatient by viewModel.selectedPatient.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val bookingSuccess by viewModel.bookingSuccess.collectAsState()

    var symptomsText by remember { mutableStateOf("") }
    var showConfirmationDialog by remember { mutableStateOf(false) }

    LaunchedEffect(bookingSuccess) {
        if (bookingSuccess == true) {
            Toast.makeText(context, "대기 신청이 완료되었습니다. 홈에서 순서를 확인해주세요.", Toast.LENGTH_LONG).show()
            symptomsText = ""
            viewModel.resetBookingStatus()
        } else if (bookingSuccess == false) {
            Toast.makeText(context, "신청에 실패했습니다. 다시 시도해 주세요.", Toast.LENGTH_SHORT).show()
            viewModel.resetBookingStatus()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("진료 접수 (ASAP)", fontWeight = FontWeight.Bold) })
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
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // 1. ASAP Banner
                AsapHeroBanner()

                // 2. Patient Selector
                Column {
                    Text(
                        text = "진료 받으실 분",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(patients) { p ->
                            FilterChip(
                                selected = selectedPatient?.id == p.id,
                                onClick = { viewModel.selectPatient(p) },
                                label = { Text(p.name) },
                                leadingIcon = if (selectedPatient?.id == p.id) {
                                    { Icon(Icons.Default.Check, null, modifier = Modifier.size(16.dp)) }
                                } else {
                                    { Icon(Icons.Default.Person, null, modifier = Modifier.size(16.dp)) }
                                }
                            )
                        }
                    }
                }

                // 3. Symptoms
                Column {
                    Text(
                        text = "현재 증상",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = symptomsText,
                        onValueChange = { symptomsText = it },
                        placeholder = { Text("어디가 어떻게 아프신가요? (예: 갑자기 열이 나고 목이 아파요)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp),
                        shape = RoundedCornerShape(16.dp)
                    )
                }

                // 4. Information Card
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.AccessTime, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "현재 접수 시 예상 대기 시간: 약 10~20분\n(진료 상황에 따라 변동될 수 있습니다)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // 5. Action Button
                Button(
                    onClick = { showConfirmationDialog = true },
                    enabled = symptomsText.isNotBlank() && !isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Default.FlashOn, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("지금 바로 진료 접수하기", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
            }

            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
        }
    }

    if (showConfirmationDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmationDialog = false },
            title = { Text("진료 접수 확인") },
            text = {
                Text("${selectedPatient?.name}님의 비대면 진료를 지금 바로 접수하시겠습니까?\n\n접수 후 순서가 되면 알림을 보내드립니다.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showConfirmationDialog = false
                        viewModel.requestAsapAppointment(symptomsText)
                    }
                ) {
                    Text("접수 확정")
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

@Composable
fun AsapHeroBanner() {
    val gradient = Brush.horizontalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.primary,
            MaterialTheme.colorScheme.secondary
        )
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .background(gradient, RoundedCornerShape(20.dp))
            .padding(20.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Column {
            Text(
                text = "ASAP 진료 대기",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )
            Text(
                text = "예약 없이 지금 바로 순서대로 진료받으세요",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.9f)
            )
        }
    }
}
