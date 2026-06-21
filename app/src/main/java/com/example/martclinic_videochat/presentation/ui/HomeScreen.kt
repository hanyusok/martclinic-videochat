package com.example.martclinic_videochat.presentation.ui

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
                    IconButton(onClick = { viewModel.loadActivePatientAndAppointments() }) {
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
fun StandbyStatusCard(
    appointment: Appointment,
    patientName: String? = null,
    onJoinCall: () -> Unit,
    onPay: (() -> Unit)? = null
) {
    val isCalling = appointment.status == Appointment.STATUS_CALLING
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isCalling) MaterialTheme.colorScheme.secondaryContainer 
                             else MaterialTheme.colorScheme.tertiaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val titlePrefix = if (patientName != null) "[$patientName] " else ""
                val titleText = when (appointment.status) {
                    Appointment.STATUS_CALLING -> "🔔 ${titlePrefix}의사가 대기 중입니다!"
                    Appointment.STATUS_PAYMENT_PENDING -> "💳 ${titlePrefix}결제 대기 중"
                    else -> "⏳ ${titlePrefix}진료 대기 중"
                }
                Text(
                    text = titleText,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isCalling) MaterialTheme.colorScheme.onSecondaryContainer 
                            else MaterialTheme.colorScheme.onTertiaryContainer
                )
                Surface(
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                    shape = CircleShape
                ) {
                    Text(
                        text = "대기번호 #${appointment.queue_number ?: " - "}",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Big Rank / Wait Time Circle
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    val statusLabel = when (appointment.status) {
                        Appointment.STATUS_CALLING -> "NOW"
                        Appointment.STATUS_PAYMENT_PENDING -> "필수"
                        else -> "대기 중"
                    }
                    val statusText = when (appointment.status) {
                        Appointment.STATUS_CALLING -> "진료중"
                        Appointment.STATUS_PAYMENT_PENDING -> "결제"
                        else -> "순서"
                    }
                    Text(
                        text = statusLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = statusText,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            val waitTimeText = when (appointment.status) {
                Appointment.STATUS_CALLING -> "아래 버튼을 눌러 영상 진료실에 입장하세요."
                Appointment.STATUS_PAYMENT_PENDING -> {
                    if (appointment.payment_amount == null) "진료비용을 산정 중입니다. 잠시만 기다려주세요..."
                    else "원활한 진료를 위해 사전 결제(${appointment.payment_amount}원)를 진행해주세요."
                }
                else -> "예상 대기 시간: 약 ${appointment.estimated_wait_minutes ?: 15}분"
            }
            Text(
                text = waitTimeText,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = if (appointment.status == Appointment.STATUS_PAYMENT_PENDING) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (appointment.status == Appointment.STATUS_PAYMENT_PENDING) {
                if (appointment.payment_amount == null) {
                    androidx.compose.material3.CircularProgressIndicator(
                        modifier = Modifier.size(48.dp),
                        color = MaterialTheme.colorScheme.error
                    )
                } else {
                    Button(
                        onClick = { onPay?.invoke() },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Icon(imageVector = Icons.Default.Payment, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("결제 후 대기열 등록", fontWeight = FontWeight.Bold)
                    }
                }
            } else {
                Button(
                    onClick = onJoinCall,
                    enabled = isCalling || appointment.status == Appointment.STATUS_IN_PROGRESS,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        disabledContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                    )
                ) {
                    Icon(
                        imageVector = if (isCalling) Icons.Default.VideoCall else Icons.Default.HourglassEmpty,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isCalling) "영상 진료 입장하기" else "순서가 되면 버튼이 활성화됩니다",
                        fontWeight = FontWeight.Bold
                    )
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
                text = "현재 접수된 진료가 없습니다.\n아래 [예약] 탭에서 ASAP 접수를 진행하세요.",
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
            Row(verticalAlignment = Alignment.CenterVertically) {
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
            }
        }
    }
}

@Composable
fun StatsRow(total: Int, active: Int, completed: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        StatCard(title = "전체", count = total, modifier = Modifier.weight(1f))
        StatCard(title = "대기/진행", count = active, modifier = Modifier.weight(1f), isHighlight = true)
        StatCard(title = "완료", count = completed, modifier = Modifier.weight(1f))
    }
}

@Composable
fun StatCard(title: String, count: Int, modifier: Modifier = Modifier, isHighlight: Boolean = false) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isHighlight) MaterialTheme.colorScheme.primaryContainer 
                             else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        )
    ) {
        Column(
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = if (isHighlight) MaterialTheme.colorScheme.onPrimaryContainer 
                        else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (isHighlight) MaterialTheme.colorScheme.primary 
                        else MaterialTheme.colorScheme.onSurface
            )
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
