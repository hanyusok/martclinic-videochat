package com.example.martclinic_videochat.presentation.ui.admin

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.martclinic_videochat.domain.model.Appointment
import com.example.martclinic_videochat.domain.model.Patient
import com.example.martclinic_videochat.domain.model.Payment
import com.example.martclinic_videochat.presentation.viewmodel.AdminViewModel
import com.example.martclinic_videochat.util.MeetUtil
import java.text.NumberFormat
import java.util.Locale
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminQueueScreen(
    viewModel: AdminViewModel = hiltViewModel()
) {
    val stats by viewModel.stats.collectAsStateWithLifecycle()
    val appointments by viewModel.allAppointments.collectAsStateWithLifecycle()
    val patients by viewModel.allPatients.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()

    var selectedAppointment by remember { mutableStateOf<Appointment?>(null) }
    var currentPayment by remember { mutableStateOf<Payment?>(null) }
    val currentSelectedAppointment = selectedAppointment?.let { appt ->
        appointments.find { it.id == appt.id } ?: appt
    }

    LaunchedEffect(currentSelectedAppointment?.id, currentSelectedAppointment?.status, currentSelectedAppointment?.payment_amount) {
        if (currentSelectedAppointment?.id != null) {
            currentPayment = viewModel.getPaymentForAppointment(currentSelectedAppointment.id!!)
        } else {
            currentPayment = null
        }
    }

    BoxWithConstraints(
        modifier = Modifier.fillMaxSize()
    ) {
        val isWideScreen = maxWidth >= 600.dp

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("진료 대기열 관리", fontWeight = FontWeight.Bold) },
                    actions = {
                        IconButton(onClick = { viewModel.loadDashboardData() }) {
                            Icon(imageVector = Icons.Default.Refresh, contentDescription = "Refresh")
                        }
                    }
                )
            }
        ) { padding ->
            Box(modifier = Modifier.fillMaxSize().padding(padding)) {
                if (isWideScreen) {
                    Row(modifier = Modifier.fillMaxSize()) {
                        Column(
                            modifier = Modifier
                                .weight(1.2f)
                                .fillMaxHeight()
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
                        ) {
                            AdminStatsHeader(
                                totalPatients = stats.totalPatients,
                                activeQueue = stats.activeAppointments,
                                completedToday = stats.completedAppointmentsToday,
                                revenueToday = stats.totalRevenueToday
                            )
                            Text(
                                "실시간 대기열",
                                modifier = Modifier.padding(16.dp),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Box(modifier = Modifier.weight(1f)) {
                                LiveQueueList(
                                    appointments = appointments,
                                    onItemClick = { selectedAppointment = it })
                                if (isLoading) {
                                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                                }
                            }
                        }
                        VerticalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        Column(
                            modifier = Modifier.weight(1.8f).fillMaxHeight().padding(16.dp),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            val appt = currentSelectedAppointment
                            if (appt != null) {
                                AppointmentDetailPane(
                                    appointment = appt,
                                    patients = patients,
                                    payment = currentPayment,
                                    onUpdateAppointmentDetails = { id, status, meetLink, paymentAmount ->
                                        viewModel.updateAppointmentDetails(
                                            id,
                                            status,
                                            meetLink,
                                            paymentAmount
                                        )
                                        selectedAppointment = null
                                    },
                                    onFetchCost = { pat, callback ->
                                        viewModel.fetchCostForPatient(pat, callback)
                                    },
                                    onCancelPayment = { id ->
                                        viewModel.cancelPayment(id)
                                        selectedAppointment = null
                                    },
                                    onGenerateMeetLink = { id ->
                                        viewModel.generateMeetLink(id)
                                    },
                                    onDismiss = { selectedAppointment = null }
                                )
                            } else {
                                EmptyDetailPlaceholder("대기열에서 진료 건을 선택하면 이곳에 상세 정보와 조작 패널이 표시됩니다.")
                            }
                        }
                    }
                } else {
                    var showDetailBottomSheet by remember { mutableStateOf(false) }

                    Column(modifier = Modifier.fillMaxSize()) {
                        AdminStatsHeader(
                            totalPatients = stats.totalPatients,
                            activeQueue = stats.activeAppointments,
                            completedToday = stats.completedAppointmentsToday,
                            revenueToday = stats.totalRevenueToday
                        )
                        Text(
                            "실시간 대기열",
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Box(modifier = Modifier.weight(1f)) {
                            LiveQueueList(appointments = appointments, onItemClick = {
                                selectedAppointment = it
                                showDetailBottomSheet = true
                            })
                            if (isLoading) {
                                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                            }
                        }
                    }

                    if (showDetailBottomSheet) {
                        ModalBottomSheet(
                            onDismissRequest = {
                                showDetailBottomSheet = false
                                selectedAppointment = null
                            },
                            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
                        ) {
                            Box(
                                modifier = Modifier.padding(
                                    bottom = 32.dp,
                                    start = 16.dp,
                                    end = 16.dp
                                )
                            ) {
                                currentSelectedAppointment?.let { appt ->
                                    AppointmentDetailPane(
                                        appointment = appt,
                                        patients = patients,
                                        payment = currentPayment,
                                        onUpdateAppointmentDetails = { id, status, meet, amount ->
                                            viewModel.updateAppointmentDetails(id, status, meet, amount)
                                            selectedAppointment = null
                                        },
                                        onFetchCost = { pat, callback ->
                                            viewModel.fetchCostForPatient(pat, callback)
                                        },
                                        onCancelPayment = { id ->
                                            viewModel.cancelPayment(id)
                                            showDetailBottomSheet = false
                                            selectedAppointment = null
                                        },
                                        onGenerateMeetLink = { id ->
                                            viewModel.generateMeetLink(id)
                                        },
                                        onDismiss = {
                                            showDetailBottomSheet = false
                                            selectedAppointment = null
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// Below are components extracted from the old AdminDashboardScreen for Queue

@Composable
fun AdminStatsHeader(
    totalPatients: Int,
    activeQueue: Int,
    completedToday: Int,
    revenueToday: Int
) {
    Column(modifier = Modifier.padding(16.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatCard("총 환자 수", totalPatients.toString(), Icons.Default.People, MaterialTheme.colorScheme.primaryContainer, Modifier.weight(1f))
            StatCard("현재 대기", activeQueue.toString(), Icons.Default.AccessTime, MaterialTheme.colorScheme.secondaryContainer, Modifier.weight(1f))
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatCard("금일 완료", completedToday.toString(), Icons.Default.CheckCircle, MaterialTheme.colorScheme.tertiaryContainer, Modifier.weight(1f))
            StatCard("금일 매출", NumberFormat.getCurrencyInstance(Locale.KOREA).format(revenueToday), Icons.Default.Payments, Color(0xFFE8F5E9), Modifier.weight(1f))
        }
    }
}

@Composable
fun StatCard(title: String, value: String, icon: ImageVector, color: Color, modifier: Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = color),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(8.dp))
            Text(title, style = MaterialTheme.typography.labelMedium)
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun LiveQueueList(
    appointments: List<Appointment>,
    onItemClick: (Appointment) -> Unit
) {
    val activeList = appointments.filter { it.status in Appointment.ACTIVE_STATUSES }
    if (activeList.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("현재 진행 중인 진료가 없습니다.")
        }
    } else {
        // Group and sort active appointments
        val activeConsultations = activeList
            .filter { it.status == "calling" || it.status == "in_progress" }
            .sortedBy { it.created_at }

        val paidWaiting = activeList
            .filter { it.status == "waiting" }
            .sortedBy { it.created_at }

        val unpaidPending = activeList
            .filter { it.status == "payment_pending" }
            .sortedBy { it.created_at }

        LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            if (activeConsultations.isNotEmpty()) {
                item {
                    Text(
                        text = "진료 및 호출 중 (${activeConsultations.size}명)",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
                items(activeConsultations) { appointment ->
                    QueueItem(appointment, onClick = { onItemClick(appointment) })
                }
            }

            if (paidWaiting.isNotEmpty()) {
                item {
                    Text(
                        text = "진료 대기 중 (수납 완료) (${paidWaiting.size}명)",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)
                    )
                }
                items(paidWaiting) { appointment ->
                    QueueItem(appointment, onClick = { onItemClick(appointment) })
                }
            }

            if (unpaidPending.isNotEmpty()) {
                item {
                    Text(
                        text = "수납/결제 대기 중 (${unpaidPending.size}명)",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)
                    )
                }
                items(unpaidPending) { appointment ->
                    QueueItem(appointment, onClick = { onItemClick(appointment) })
                }
            }
        }
    }
}

@Composable
fun QueueItem(appointment: Appointment, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("대기번호 #${appointment.queue_number}", fontWeight = FontWeight.Bold)
                Text(appointment.symptoms, style = MaterialTheme.typography.bodySmall, maxLines = 1)
                val statusDisplayText = if (appointment.status == "payment_pending") {
                    if (appointment.payment_amount == null) {
                        "수납 대기 (EMR 금액 대기)"
                    } else {
                        val amountFormatted = java.text.NumberFormat.getNumberInstance(java.util.Locale.KOREA).format(appointment.payment_amount)
                        "수납 대기 (결제 요청: ${amountFormatted}원)"
                    }
                } else {
                    appointment.statusText
                }
                Text(
                    text = statusDisplayText,
                    color = appointment.getStatusColor(),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold
                )
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "상세 보기",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun EmptyDetailPlaceholder(message: String) {
    Card(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Inbox,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.outline
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "상세 내역 패널",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun AppointmentDetailPane(
    appointment: Appointment,
    patients: List<Patient>,
    payment: Payment?,
    onUpdateAppointmentDetails: (String, String, String?, Int?) -> Unit,
    onFetchCost: (Patient, (Int?) -> Unit) -> Unit,
    onCancelPayment: (String) -> Unit,
    onGenerateMeetLink: suspend (String) -> Unit,
    onDismiss: () -> Unit
) {
    val patient = patients.find { it.id == appointment.patient_id }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isGeneratingMeetLink by remember { mutableStateOf(false) }

    var selectedStatus by remember(appointment.status) { mutableStateOf(appointment.status) }
    var meetLink by remember(appointment.meet_link) { mutableStateOf(appointment.meet_link ?: "") }
    var paymentAmount by remember(appointment.payment_amount) { mutableStateOf(appointment.payment_amount?.toString() ?: "") }

    val isPaymentCompleted = payment?.status == "SUCCESS"

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "진료 및 결제 조작 패널",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }

            HorizontalDivider()

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("대기 번호", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("#${appointment.queue_number ?: "-"}", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold)
                }
                val statusDisplayText = if (appointment.status == "payment_pending") {
                    if (appointment.payment_amount == null) {
                        "수납 대기 (EMR 대기)"
                    } else {
                        "수납 대기 (결제 요청)"
                    }
                } else {
                    appointment.statusText
                }
                Surface(
                    color = appointment.getStatusColor().copy(alpha = 0.1f),
                    contentColor = appointment.getStatusColor(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = statusDisplayText,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Column {
                Text("환자 성명", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(patient?.name ?: "불명", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                if (patient != null) {
                    Text("전화번호: ${patient.phone}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    val maskedResident = if (patient.resident_number?.contains("-") == true) {
                        val parts = patient.resident_number?.split("-") ?: emptyList()
                        if (parts.size == 2) "${parts[0]}-*******" else "******-*******"
                    } else {
                        "******-*******"
                    }
                    Text("주민번호: $maskedResident", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Column {
                Text("환자 증상", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Card(
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                ) {
                    Text(
                        text = appointment.symptoms.ifBlank { "입력된 증상 내용이 없습니다." },
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            HorizontalDivider()

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("진료 진행 상태 변경", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("waiting", "calling", "in_progress", "payment_pending", "completed", "cancelled").forEach { statusKey ->
                        val text = when (statusKey) {
                            "waiting" -> "대기 중"
                            "calling" -> "입장 대기"
                            "in_progress" -> "진료 중"
                            "payment_pending" -> "수납 대기"
                            "completed" -> "완료"
                            "cancelled" -> "취소"
                            else -> statusKey
                        }
                        FilterChip(
                            selected = selectedStatus == statusKey,
                            onClick = { selectedStatus = statusKey },
                            label = { Text(text) }
                        )
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("화상 진료 방 링크 (Google Meet)", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (appointment.status == "waiting" && meetLink.isBlank()) {
                    Button(
                        onClick = {
                            appointment.id?.let { apptId ->
                                scope.launch {
                                    isGeneratingMeetLink = true
                                    try {
                                        onGenerateMeetLink(apptId)
                                        Toast.makeText(context, "환자를 호출하고 진료방을 생성했습니다.", Toast.LENGTH_SHORT).show()
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "생성 실패 (모의 링크 생성됨): ${e.message}", Toast.LENGTH_LONG).show()
                                    } finally {
                                        isGeneratingMeetLink = false
                                    }
                                }
                            }
                        },
                        enabled = !isGeneratingMeetLink,
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        if (isGeneratingMeetLink) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Default.VideoCall, contentDescription = null, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("진료 시작 (환자 호출 및 Meet 방 생성)", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = meetLink,
                            onValueChange = { meetLink = it },
                            placeholder = { Text("https://meet.google.com/...") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        Button(
                            onClick = {
                                appointment.id?.let { apptId ->
                                    scope.launch {
                                        isGeneratingMeetLink = true
                                        try {
                                            onGenerateMeetLink(apptId)
                                            Toast.makeText(context, "Google Meet 진료방이 재생성되었습니다.", Toast.LENGTH_SHORT).show()
                                        } catch (e: Exception) {
                                            Toast.makeText(context, "생성 실패 (모의 링크 생성됨): ${e.message}", Toast.LENGTH_LONG).show()
                                        } finally {
                                            isGeneratingMeetLink = false
                                        }
                                    }
                                }
                            },
                            enabled = !isGeneratingMeetLink && appointment.id != null,
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                        ) {
                            if (isGeneratingMeetLink) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    color = MaterialTheme.colorScheme.onSecondary,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text("링크 재발급")
                            }
                        }
                    }
                    if (meetLink.isNotBlank()) {
                        Button(
                            onClick = { MeetUtil.openGoogleMeet(context, meetLink) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
                        ) {
                            Icon(Icons.Default.VideoCall, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("생성된 진료방 입장하기")
                        }
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("진료 결제 금액 (원)", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    val (paymentStatusText, paymentStatusColor) = when {
                        isPaymentCompleted -> "결제 완료" to MaterialTheme.colorScheme.primary
                        appointment.status == "payment_pending" -> {
                            if (appointment.payment_amount == null) {
                                "EMR 금액 대기" to Color(0xFFE65100)
                            } else {
                                "결제 대기 중" to MaterialTheme.colorScheme.error
                            }
                        }
                        else -> "결제 미시작" to MaterialTheme.colorScheme.outline
                    }
                    Surface(
                        color = paymentStatusColor.copy(alpha = 0.1f),
                        contentColor = paymentStatusColor,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = paymentStatusText,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    if (patient != null) {
                        TextButton(
                            onClick = {
                                onFetchCost(patient) { cost ->
                                    if (cost != null) {
                                        paymentAmount = cost.toString()
                                        android.widget.Toast.makeText(context, "EMR에서 진료비를 불러왔습니다.", android.widget.Toast.LENGTH_SHORT).show()
                                    } else {
                                        android.widget.Toast.makeText(context, "EMR 진료비를 찾을 수 없습니다.", android.widget.Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                            contentPadding = PaddingValues(horizontal = 8.dp)
                        ) {
                            Text("EMR 금액 불러오기", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
                OutlinedTextField(
                    value = paymentAmount,
                    onValueChange = { input ->
                        if (input.all { it.isDigit() }) {
                            paymentAmount = input
                        }
                    },
                    placeholder = { Text("진료비 금액 입력 (예: 5000)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                if (appointment.status == "payment_pending" && appointment.payment_amount == null) {
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Warning, contentDescription = "Warning", tint = Color(0xFFE65100))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "환자 화면에 '진료비용 산정 중' 대기 스피너가 표시되고 있습니다. EMR 금액 불러오기 또는 직접 입력 후 저장해 주세요.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFFE65100)
                            )
                        }
                    }
                }

                if (appointment.status == "payment_pending" && appointment.payment_amount != null && !isPaymentCompleted) {
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Info, contentDescription = "Info", tint = MaterialTheme.colorScheme.onErrorContainer)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "환자 결제 대기 중입니다. 환자가 결제를 완료하면 자동으로 대기열(대기 중)로 복귀합니다.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }
                
                if (payment != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("결제 상세 내역", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("승인 번호: ${payment.transaction_id ?: "없음"}", style = MaterialTheme.typography.bodySmall)
                            Text("결제 수단: ${payment.pay_method ?: "없음"}", style = MaterialTheme.typography.bodySmall)
                            Text("결제 상태: ${payment.status}", style = MaterialTheme.typography.bodySmall)
                            
                            if (payment.status == "SUCCESS" && appointment.id != null) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = { onCancelPayment(appointment.id) },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                                ) {
                                    Text("결제 취소 (롤백)")
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("취소")
                }
                Button(
                    onClick = {
                        val amount = paymentAmount.toIntOrNull()
                        onUpdateAppointmentDetails(
                            appointment.id!!,
                            selectedStatus,
                            meetLink.ifBlank { null },
                            amount
                        )
                        Toast.makeText(context, "진료 및 결제 정보가 업데이트되었습니다.", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.weight(1.5f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("저장하기", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
