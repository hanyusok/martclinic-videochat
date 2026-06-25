package com.example.martclinic_videochat.presentation.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.martclinic_videochat.R
import com.example.martclinic_videochat.domain.model.Appointment
import com.example.martclinic_videochat.presentation.navigation.Screen
import com.example.martclinic_videochat.presentation.ui.components.AppointmentCard
import com.example.martclinic_videochat.presentation.viewmodel.HomeViewModel
import com.example.martclinic_videochat.util.MeetUtil

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onNavigateToAdmin: () -> Unit = {},
    onNavigateToBooking: () -> Unit = {},
    onNavigateToMyPage: () -> Unit = {},
    onNavigateToPayment: (String, Int) -> Unit = { _, _ -> }
) {
    val patient by viewModel.patient.collectAsStateWithLifecycle()
    val allPatients by viewModel.allPatients.collectAsStateWithLifecycle()
    val appointments by viewModel.appointments.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val isAdmin by viewModel.isAdmin.collectAsStateWithLifecycle()
    val activeStandby by viewModel.activeStandby.collectAsStateWithLifecycle()
    val otherAppointments by viewModel.otherAppointments.collectAsStateWithLifecycle()
    val needsProfileUpdate by viewModel.needsProfileUpdate.collectAsStateWithLifecycle()
    val queuePosition by viewModel.queuePosition.collectAsStateWithLifecycle()

    val context = LocalContext.current

    // Refresh data when entering or resuming the screen
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                viewModel.loadActivePatientAndAppointments()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Admin Auto-Navigation Trigger
    LaunchedEffect(isAdmin) {
        if (isAdmin) {
            onNavigateToAdmin()
        }
    }
    
    // Statistics
    val stats = remember(appointments) {
        object {
            val total = appointments.size
            val completed = appointments.count { it.status == Appointment.STATUS_COMPLETED }
            val active = appointments.count { it.status in Appointment.ACTIVE_STATUSES }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.app_name), fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = {
                        viewModel.loadActivePatientAndAppointments(
                            onComplete = { success ->
                                if (success) {
                                    android.widget.Toast.makeText(context, "최신 정보로 업데이트되었습니다.", android.widget.Toast.LENGTH_SHORT).show()
                                } else {
                                    android.widget.Toast.makeText(context, "네트워크 동기화에 실패했습니다.", android.widget.Toast.LENGTH_SHORT).show()
                                }
                            }
                        )
                    }) {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = "새로고침")
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onNavigateToBooking,
                icon = { Icon(Icons.Default.DateRange, contentDescription = "진료 예약") },
                text = { Text("진료 예약", fontWeight = FontWeight.Bold) },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // 0. Profile Reminder (Conditional)
                if (needsProfileUpdate) {
                    item {
                        ProfileReminderBanner(onClick = onNavigateToMyPage)
                    }
                }

                // 1. Welcome Banner
                item {
                    WelcomeBanner(patientName = patient?.name ?: "환자")
                }

                // 2. ASAP Standby Status Card (High Priority)
                activeStandby?.let { standby ->
                    item(key = "active_standby") {
                        val standbyPatient = allPatients.find { it.id == standby.patient_id }
                        StandbyStatusCard(
                            appointment = standby,
                            patientName = standbyPatient?.name,
                            queuePosition = queuePosition,
                            onJoinCall = {
                                if (!standby.meet_link.isNullOrBlank()) {
                                    MeetUtil.openGoogleMeet(context, standby.meet_link)
                                }
                            },
                            onPay = {
                                standby.id?.let { id ->
                                    val amount = standby.payment_amount ?: 0
                                    onNavigateToPayment(id, amount)
                                }
                            }
                        )
                    }
                }

                // 3. Stats Row
                item {
                    StatsRow(total = stats.total, active = stats.active, completed = stats.completed)
                }

                // 4. Section Title
                item {
                    Text(
                        text = "전체 진료 내역",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                if (otherAppointments.isEmpty() && !isLoading) {
                    item {
                        EmptyAppointmentsCard()
                    }
                } else {
                    items(
                        items = otherAppointments,
                        key = { it.id ?: it.hashCode() }
                    ) { appt ->
                        val apptPatient = allPatients.find { it.id == appt.patient_id }
                        AppointmentCard(
                            appointment = appt,
                            patientName = apptPatient?.name,
                            onEnterConsultation = {
                                if (!appt.meet_link.isNullOrBlank()) {
                                    MeetUtil.openGoogleMeet(context, appt.meet_link)
                                }
                            }
                        )
                    }
                }
            }

            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
        }
    }
}

@Composable
fun ProfileReminderBanner(onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.error)
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "프로필 정보가 부족합니다",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
                Text(
                    text = "원활한 진료를 위해 정보를 업데이트해주세요.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
            TextButton(onClick = onClick) {
                Text("이동")
            }
        }
    }
}

@Composable
fun HorizontalStepper(
    activeStepIndex: Int,
    modifier: Modifier = Modifier
) {
    val steps = listOf("접수", "보험조회", "수납결제", "영상대기", "진료입장")
    
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.Center
    ) {
        steps.forEachIndexed { index, stepTitle ->
            val stepNumber = index + 1
            val isCompleted = stepNumber < activeStepIndex
            val isActive = stepNumber == activeStepIndex

            // Node Column
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.width(56.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(
                            when {
                                isCompleted -> MaterialTheme.colorScheme.primary
                                isActive -> MaterialTheme.colorScheme.secondary
                                else -> MaterialTheme.colorScheme.surfaceVariant
                            }
                        )
                ) {
                    if (isCompleted) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(14.dp)
                        )
                    } else {
                        Text(
                            text = stepNumber.toString(),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = when {
                                isActive -> MaterialTheme.colorScheme.onSecondary
                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stepTitle,
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 9.sp,
                    fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                    color = when {
                        isActive -> MaterialTheme.colorScheme.secondary
                        isCompleted -> MaterialTheme.colorScheme.onSurface
                        else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    },
                    textAlign = TextAlign.Center
                )
            }

            // Connector Line
            if (index < steps.size - 1) {
                val lineColor = if (stepNumber < activeStepIndex) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.surfaceVariant
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(top = 11.dp) // center of 24.dp circle
                        .height(2.dp)
                        .background(lineColor)
                )
            }
        }
    }
}

@Composable
fun StandbyStatusCard(
    appointment: Appointment,
    patientName: String? = null,
    queuePosition: Int? = null,
    onJoinCall: () -> Unit,
    onPay: (() -> Unit)? = null
) {
    val activeStepIndex = when {
        appointment.status in listOf("calling", "in_progress") -> 5
        appointment.status == Appointment.STATUS_WAITING -> 4
        appointment.status == Appointment.STATUS_PAYMENT_PENDING && appointment.payment_amount != null -> 3
        appointment.status == Appointment.STATUS_PAYMENT_PENDING && appointment.payment_amount == null -> 2
        else -> 1
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val titlePrefix = if (patientName != null) "[$patientName] " else ""
                Text(
                    text = "${titlePrefix}진료 진행 단계",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Surface(
                    color = appointment.getStatusColor().copy(alpha = 0.1f),
                    contentColor = appointment.getStatusColor(),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = when (appointment.status) {
                            "waiting" -> "대기 중"
                            "calling" -> "진료 입장 대기"
                            "in_progress" -> "진료 중"
                            "payment_pending" -> {
                                if (appointment.payment_amount == null) "진료비 산정 중" else "수납 대기"
                            }
                            else -> appointment.statusText
                        },
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            // Horizontal Stepper Progress Indicator
            HorizontalStepper(activeStepIndex = activeStepIndex)

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            // Active Step Content Column
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val activeStepTitle = when (activeStepIndex) {
                    1 -> "진료 접수 완료"
                    2 -> "건강보험 진료비 조회"
                    3 -> "진료비 결제"
                    4 -> "대기열 등록 및 영상 대기"
                    5 -> "진료실 입장"
                    else -> ""
                }
                
                val activeStepIcon = when (activeStepIndex) {
                    1 -> Icons.Default.CheckCircle
                    2 -> Icons.Default.HourglassTop
                    3 -> Icons.Default.Payment
                    4 -> Icons.Default.HourglassEmpty
                    5 -> Icons.Default.VideoCall
                    else -> Icons.Default.Info
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = activeStepIcon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = activeStepTitle,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(modifier = Modifier.padding(12.dp)) {
                        when (activeStepIndex) {
                            1 -> {
                                Text(
                                    text = "비대면 진료 접수가 정상적으로 완료되었습니다.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            2 -> {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        strokeWidth = 2.dp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = "EMR에서 건강보험 적용 본인부담금을 확인하고 있습니다. 잠시만 대기해 주세요...",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            3 -> {
                                Column(
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Info,
                                            contentDescription = "Info",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "건강보험 적용 본인부담금 수납이 필요합니다.",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    
                                    appointment.payment_amount?.let { amount ->
                                        Button(
                                            onClick = { onPay?.invoke() },
                                            modifier = Modifier.fillMaxWidth().height(48.dp),
                                            shape = RoundedCornerShape(10.dp),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = MaterialTheme.colorScheme.primary,
                                                contentColor = MaterialTheme.colorScheme.onPrimary
                                            )
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Payment, 
                                                contentDescription = null,
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            val amountStr = java.text.NumberFormat.getNumberInstance(java.util.Locale.KOREA).format(amount)
                                            Text(
                                                text = "$amountStr 원 간편 결제하기",
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                            4 -> {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "대기번호: #${appointment.queue_number ?: "-"}",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "내 앞 대기자: ${queuePosition ?: 0}명",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.primary,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Text(
                                        text = "예상 대기 시간: 약 ${(queuePosition ?: 0) * 5 + 5}분",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    
                                    val hasMeetLink = !appointment.meet_link.isNullOrBlank()
                                    Spacer(modifier = Modifier.height(4.dp))
                                    
                                    Button(
                                        onClick = onJoinCall,
                                        enabled = hasMeetLink,
                                        modifier = Modifier.fillMaxWidth().height(44.dp),
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.primary,
                                            disabledContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                                        )
                                    ) {
                                        Icon(
                                            imageVector = if (hasMeetLink) Icons.Default.VideoCall else Icons.Default.HourglassEmpty,
                                            contentDescription = null,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = if (hasMeetLink) "영상 진료방 개설 및 입장" else "순서가 되면 진료방이 준비됩니다",
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    
                                    if (hasMeetLink) {
                                        Text(
                                            text = "* 환자 본인이 먼저 입장하여 진료실을 개설합니다. 이후 의사가 입장하여 진료가 개시됩니다.",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                            5 -> {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text(
                                        text = "의사가 대기 중입니다! 아래 버튼을 눌러 즉시 입장하세요.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Button(
                                        onClick = onJoinCall,
                                        modifier = Modifier.fillMaxWidth().height(44.dp),
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.secondary
                                        )
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.VideoCall, 
                                            contentDescription = null,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(text = "영상 진료 입장하기", fontWeight = FontWeight.Bold)
                                    }
                                    Text(
                                        text = "* 진료방 입장 후 의사가 아직 입장하지 않았다면 의사 호출을 시도해 주세요.",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
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

@Composable
fun EmptyAppointmentsCard() {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Box(
            modifier = Modifier.fillMaxWidth().padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "현재 접수된 진료가 없습니다.\n우측 하단의 [진료 예약] 버튼을 클릭해 실시간 접수를 진행하세요.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun WelcomeBanner(patientName: String) {
    val gradient = Brush.horizontalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.primary,
            MaterialTheme.colorScheme.secondary
        )
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(gradient)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "안녕하세요, ${patientName}님!",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "마트클리닉에서 가장 빠른 진료를 받아보세요.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
                Icon(
                    imageVector = Icons.Default.MedicalServices,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.9f),
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}

@Composable
fun StatsRow(total: Int, active: Int, completed: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        StatCard(
            title = "전체", 
            count = total, 
            icon = Icons.Default.Assignment, 
            modifier = Modifier.weight(1f)
        )
        StatCard(
            title = "대기/진행", 
            count = active, 
            icon = Icons.Default.HourglassTop, 
            isHighlight = true, 
            modifier = Modifier.weight(1f)
        )
        StatCard(
            title = "완료", 
            count = completed, 
            icon = Icons.Default.CheckCircle, 
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun StatCard(
    title: String, 
    count: Int, 
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier, 
    isHighlight: Boolean = false
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isHighlight) MaterialTheme.colorScheme.primaryContainer 
                             else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp, horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isHighlight) MaterialTheme.colorScheme.primary 
                       else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                modifier = Modifier.size(20.dp)
            )
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 10.sp,
                    color = if (isHighlight) MaterialTheme.colorScheme.onPrimaryContainer 
                            else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = count.toString(),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (isHighlight) MaterialTheme.colorScheme.primary 
                            else MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun WelcomeBannerPreview() {
    MaterialTheme {
        WelcomeBanner(patientName = "홍길동")
    }
}
