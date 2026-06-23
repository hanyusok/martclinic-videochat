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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.martclinic_videochat.presentation.viewmodel.BookingViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun BookingScreen(
    onBack: () -> Unit = {},
    viewModel: BookingViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val patients by viewModel.patients.collectAsState()
    val selectedPatient by viewModel.selectedPatient.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val bookingSuccess by viewModel.bookingSuccess.collectAsState()

    val bookingError by viewModel.bookingError.collectAsState()

    var symptomsText by remember { mutableStateOf("") }
    var showConfirmationDialog by remember { mutableStateOf(false) }

    LaunchedEffect(bookingSuccess, bookingError) {
        if (bookingSuccess == true) {
            Toast.makeText(context, "대기 신청이 완료되었습니다. 홈에서 순서를 확인해주세요.", Toast.LENGTH_LONG).show()
            symptomsText = ""
            viewModel.resetBookingStatus()
            onBack()
        } else if (bookingSuccess == false) {
            val errorMsg = bookingError ?: "신청에 실패했습니다. 다시 시도해 주세요."
            Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show()
            viewModel.resetBookingStatus()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("실시간 진료 접수", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로가기")
                    }
                }
            )
        },
        bottomBar = {
            Surface(
                tonalElevation = 8.dp,
                shadowElevation = 8.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .imePadding()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .navigationBarsPadding()
                ) {
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
            }
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
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
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
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(patients) { p ->
                            FilterChip(
                                selected = selectedPatient?.id == p.id,
                                onClick = { viewModel.selectPatient(p) },
                                label = { Text(p.name ?: "알 수 없음") },
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
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = symptomsText,
                        onValueChange = { symptomsText = it },
                        placeholder = { Text("어디가 어떻게 아프신가요? (예: 갑자기 열이 나고 목이 아파요)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp),
                        shape = RoundedCornerShape(16.dp)
                    )
                }
 
                // 4. Information Card
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
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
                Text(
                    text = buildAnnotatedString {
                        append("${selectedPatient?.name ?: "알 수 없음"}님의 비대면 진료를 지금 바로 접수하시겠습니까?\n\n⚠️ 원활한 진료 진행을 위해 ")
                        withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)) {
                            append("★사전 결제★")
                        }
                        append("가 필수적으로 필요합니다.\n접수 후 순서가 되면 알림을 보내드립니다.")
                    }
                )
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
            .height(80.dp)
            .background(gradient, RoundedCornerShape(16.dp))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Column {
            Text(
                text = "실시간 빠른 진료",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )
            Text(
                text = "예약 대기 없이 접수 즉시 순서대로 빠르게 진료받으실 수 있습니다",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.9f)
            )
        }
    }
}
