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
    onNavigateToMyPage: () -> Unit = {}
) {
    val patient by viewModel.patient.collectAsStateWithLifecycle()
    val appointments by viewModel.appointments.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val isAdmin by viewModel.isAdmin.collectAsStateWithLifecycle()
    val activeStandby by viewModel.activeStandby.collectAsStateWithLifecycle()
    val otherAppointments by viewModel.otherAppointments.collectAsStateWithLifecycle()
    val needsProfileUpdate by viewModel.needsProfileUpdate.collectAsStateWithLifecycle()

    val context = LocalContext.current

    // Refresh data when entering the screen
    LaunchedEffect(Unit) {
        viewModel.loadActivePatientAndAppointments()
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
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
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
                        StandbyStatusCard(
                            appointment = standby,
                            onJoinCall = {
                                if (!standby.meet_link.isNullOrBlank()) {
                                    MeetUtil.openGoogleMeet(context, standby.meet_link)
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
                    ) { appointment ->
                        AppointmentCard(
                            appointment = appointment,
                            onEnterConsultation = {
                                if (!appointment.meet_link.isNullOrBlank()) {
                                    MeetUtil.openGoogleMeet(context, appointment.meet_link)
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
    onJoinCall: () -> Unit
) {
    val isCalling = appointment.status == Appointment.STATUS_CALLING
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isCalling) MaterialTheme.colorScheme.secondaryContainer 
                             else MaterialTheme.colorScheme.tertiaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = if (isCalling) "🔔 의사가 대기 중입니다!" else "⏳ 진료 대기 중",
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

            Spacer(modifier = Modifier.height(20.dp))

            // Big Rank / Wait Time Circle
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (isCalling) "NOW" else "대기 중",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = if (isCalling) "진료중" else "순서",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = if (isCalling) "아래 버튼을 눌러 영상 진료실에 입장하세요." 
                       else "예상 대기 시간: 약 ${appointment.estimated_wait_minutes ?: 15}분",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onJoinCall,
                enabled = isCalling || appointment.status == Appointment.STATUS_IN_PROGRESS,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
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
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(gradient)
                .padding(horizontal = 24.dp, vertical = 12.dp)
        ) {
            Column {
                Text(
                    text = "안녕하세요,",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White.copy(alpha = 0.9f)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${patientName}님!",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "마트클리닉에서 가장 빠른 진료를 받아보세요.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
fun StatsRow(total: Int, active: Int, completed: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
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
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isHighlight) MaterialTheme.colorScheme.primaryContainer 
                             else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = if (isHighlight) MaterialTheme.colorScheme.onPrimaryContainer 
                        else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.titleLarge,
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
