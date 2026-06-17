package com.example.martclinic_videochat.presentation.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.martclinic_videochat.domain.model.AdminStats
import com.example.martclinic_videochat.domain.model.Appointment
import com.example.martclinic_videochat.domain.model.Patient
import com.example.martclinic_videochat.domain.model.UserProfile
import com.example.martclinic_videochat.presentation.viewmodel.AdminViewModel
import com.example.martclinic_videochat.ui.theme.MartclinicvideochatTheme
import com.example.martclinic_videochat.util.MeetUtil
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    viewModel: AdminViewModel = hiltViewModel(),
    onExitAdmin: () -> Unit = {}
) {
    val stats by viewModel.stats.collectAsStateWithLifecycle()
    val appointments by viewModel.allAppointments.collectAsStateWithLifecycle()
    val patients by viewModel.allPatients.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val currentUserProfile by viewModel.currentUserProfile.collectAsStateWithLifecycle()

    AdminDashboardScreenContent(
        stats = stats,
        appointments = appointments,
        patients = patients,
        isLoading = isLoading,
        currentUserProfile = currentUserProfile,
        onRefresh = { viewModel.loadDashboardData() },
        onUpdateStatus = { id, status -> viewModel.updateStatus(id, status) },
        onExitAdmin = onExitAdmin,
        onSignOut = { viewModel.signOut() }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreenContent(
    stats: AdminStats,
    appointments: List<Appointment>,
    patients: List<Patient>,
    isLoading: Boolean,
    currentUserProfile: UserProfile?,
    onRefresh: () -> Unit,
    onUpdateStatus: (String, String) -> Unit,
    onExitAdmin: () -> Unit,
    onSignOut: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var selectedAppointment by remember { mutableStateOf<Appointment?>(null) }
    var selectedPatient by remember { mutableStateOf<Patient?>(null) }

    LaunchedEffect(selectedTab) {
        selectedAppointment = null
        selectedPatient = null
    }

    val currentSelectedAppointment = selectedAppointment?.let { appt ->
        appointments.find { it.id == appt.id } ?: appt
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("관리자 대시보드", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onExitAdmin) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Exit Admin")
                    }
                },
                actions = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        currentUserProfile?.email?.let { email ->
                            Text(
                                text = email,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(end = 4.dp)
                            )
                        }
                        IconButton(onClick = onRefresh) {
                            Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                        }
                        IconButton(onClick = onSignOut) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                                contentDescription = "Logout",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            )
        }
    ) { padding ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            val isWideScreen = maxWidth >= 600.dp

            if (isWideScreen) {
                Row(modifier = Modifier.fillMaxSize()) {
                    // Left Panel (Master List)
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

                        TabRow(selectedTabIndex = selectedTab) {
                            Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }) {
                                Text("실시간 대기열", modifier = Modifier.padding(12.dp), style = MaterialTheme.typography.titleSmall)
                            }
                            Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }) {
                                Text("사용자 관리", modifier = Modifier.padding(12.dp), style = MaterialTheme.typography.titleSmall)
                            }
                        }

                        Box(modifier = Modifier.weight(1f)) {
                            when (selectedTab) {
                                0 -> LiveQueueList(
                                    appointments = appointments,
                                    onItemClick = { selectedAppointment = it }
                                )
                                1 -> UserDirectoryList(
                                    patients = patients,
                                    onItemClick = { selectedPatient = it }
                                )
                            }
                            if (isLoading) {
                                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                            }
                        }
                    }

                    VerticalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                    // Right Panel (Detail Pane)
                    Column(
                        modifier = Modifier
                            .weight(1.8f)
                            .fillMaxHeight()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        when (selectedTab) {
                            0 -> {
                                val appt = currentSelectedAppointment
                                if (appt != null) {
                                    AppointmentDetailPane(
                                        appointment = appt,
                                        patients = patients,
                                        onUpdateStatus = { id, status ->
                                            onUpdateStatus(id, status)
                                        },
                                        onDismiss = { selectedAppointment = null }
                                    )
                                } else {
                                    EmptyDetailPlaceholder("대기열에서 진료 건을 선택하면 이곳에 상세 정보와 조작 패널이 표시됩니다.")
                                }
                            }
                            1 -> {
                                val pat = selectedPatient
                                if (pat != null) {
                                    PatientDetailPane(
                                        patient = pat,
                                        appointments = appointments,
                                        onDismiss = { selectedPatient = null }
                                    )
                                } else {
                                    EmptyDetailPlaceholder("사용자 목록에서 환자를 선택하면 이곳에 프로필 상세 정보와 진료 이력이 표시됩니다.")
                                }
                            }
                        }
                    }
                }
            } else {
                // Mobile Layout (Single Panel with BottomSheet Details)
                var showDetailBottomSheet by remember { mutableStateOf(false) }

                Column(modifier = Modifier.fillMaxSize()) {
                    AdminStatsHeader(
                        totalPatients = stats.totalPatients,
                        activeQueue = stats.activeAppointments,
                        completedToday = stats.completedAppointmentsToday,
                        revenueToday = stats.totalRevenueToday
                    )

                    TabRow(selectedTabIndex = selectedTab) {
                        Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }) {
                            Text("실시간 대기열", modifier = Modifier.padding(16.dp))
                        }
                        Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }) {
                            Text("사용자 관리", modifier = Modifier.padding(16.dp))
                        }
                    }

                    Box(modifier = Modifier.weight(1f)) {
                        when (selectedTab) {
                            0 -> LiveQueueList(
                                appointments = appointments,
                                onItemClick = {
                                    selectedAppointment = it
                                    showDetailBottomSheet = true
                                }
                            )
                            1 -> UserDirectoryList(
                                patients = patients,
                                onItemClick = {
                                    selectedPatient = it
                                    showDetailBottomSheet = true
                                }
                            )
                        }
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
                            selectedPatient = null
                        },
                        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
                    ) {
                        Box(modifier = Modifier.padding(bottom = 32.dp, start = 16.dp, end = 16.dp)) {
                            when (selectedTab) {
                                0 -> currentSelectedAppointment?.let { appt ->
                                    AppointmentDetailPane(
                                        appointment = appt,
                                        patients = patients,
                                        onUpdateStatus = { id, status ->
                                            onUpdateStatus(id, status)
                                        },
                                        onDismiss = {
                                            showDetailBottomSheet = false
                                            selectedAppointment = null
                                        }
                                    )
                                }
                                1 -> selectedPatient?.let { pat ->
                                    PatientDetailPane(
                                        patient = pat,
                                        appointments = appointments,
                                        onDismiss = {
                                            showDetailBottomSheet = false
                                            selectedPatient = null
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
        LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(activeList) { appointment ->
                QueueItem(appointment, onClick = { onItemClick(appointment) })
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
                Text(
                    text = appointment.statusText,
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
fun UserDirectoryList(
    patients: List<Patient>,
    onItemClick: (Patient) -> Unit
) {
    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(patients) { patient ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onItemClick(patient) },
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.padding(end = 12.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(patient.name, fontWeight = FontWeight.Bold)
                        Text(patient.phone, style = MaterialTheme.typography.bodySmall)
                    }
                    Text(
                        text = patient.relationship,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
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

@Composable
fun AppointmentDetailPane(
    appointment: Appointment,
    patients: List<Patient>,
    onUpdateStatus: (String, String) -> Unit,
    onDismiss: () -> Unit
) {
    val patient = patients.find { it.id == appointment.patient_id }
    val context = LocalContext.current

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
                    text = "진료 상세 정보",
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
                Surface(
                    color = appointment.getStatusColor().copy(alpha = 0.1f),
                    contentColor = appointment.getStatusColor(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = appointment.statusText,
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
                    val maskedResident = if (patient.resident_number.contains("-")) {
                        val parts = patient.resident_number.split("-")
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

            if (!appointment.meet_link.isNullOrBlank()) {
                Button(
                    onClick = { MeetUtil.openGoogleMeet(context, appointment.meet_link) },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.VideoCall, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("영상 진료 입장 (Google Meet)", fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            when (appointment.status) {
                Appointment.STATUS_WAITING, Appointment.STATUS_CALLING -> {
                    Button(
                        onClick = { onUpdateStatus(appointment.id!!, Appointment.STATUS_IN_PROGRESS) },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("진료 시작하기", fontWeight = FontWeight.Bold)
                    }
                }
                Appointment.STATUS_IN_PROGRESS -> {
                    Button(
                        onClick = { onUpdateStatus(appointment.id!!, Appointment.STATUS_COMPLETED) },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("진료 완료하기", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun PatientDetailPane(
    patient: Patient,
    appointments: List<Appointment>,
    onDismiss: () -> Unit
) {
    val patientAppointments = remember(patient, appointments) {
        appointments.filter { it.patient_id == patient.id }
    }

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
                    text = "환자 프로필 상세",
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
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = null,
                    modifier = Modifier.size(60.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(patient.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Text(
                        text = "관계: ${patient.relationship}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                DetailRow("전화번호", patient.phone)
                val residentNumber = patient.resident_number
                val maskedResident = if (residentNumber.contains("-")) {
                    val parts = residentNumber.split("-")
                    if (parts.size == 2) "${parts[0]}-*******" else "******-*******"
                } else {
                    "******-*******"
                }
                DetailRow("주민등록번호", maskedResident)
                DetailRow("차트 등록 번호", patient.clinic_patient_number ?: "미확인 (EMR 미동기화)")
            }

            HorizontalDivider()

            Text(
                text = "최근 진료 내역",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            if (patientAppointments.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("등록된 과거 진료 내역이 없습니다.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.outline)
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 200.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(patientAppointments) { appt ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = appt.created_at?.take(10) ?: "날짜 없음",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(appt.symptoms, style = MaterialTheme.typography.bodySmall, maxLines = 1)
                                }
                                Text(
                                    text = appt.statusText,
                                    color = appt.getStatusColor(),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
    }
}

@Preview(showBackground = true)
@Composable
fun AdminDashboardScreenPreview() {
    val sampleStats = AdminStats(
        totalPatients = 120,
        activeAppointments = 5,
        completedAppointmentsToday = 25,
        totalRevenueToday = 450000
    )
    val sampleAppointments = listOf(
        Appointment(id = "1", patient_id = "p1", status = Appointment.STATUS_CALLING, symptoms = "목이 아파요", queue_number = 101),
        Appointment(id = "2", patient_id = "p2", status = Appointment.STATUS_IN_PROGRESS, symptoms = "머리가 어지러워요", queue_number = 102),
        Appointment(id = "3", patient_id = "p3", status = Appointment.STATUS_WAITING, symptoms = "기침이 나요", queue_number = 103)
    )
    val samplePatients = listOf(
        Patient(id = "p1", user_id = "u1", name = "김철수", phone = "010-1234-5678", resident_number = "900101-1", relationship = "본인"),
        Patient(id = "p2", user_id = "u2", name = "이영희", phone = "010-9876-5432", resident_number = "950505-2", relationship = "자녀"),
        Patient(id = "p3", user_id = "u3", name = "박민수", phone = "010-1111-2222", resident_number = "800808-1", relationship = "배우자")
    )

    MartclinicvideochatTheme {
        AdminDashboardScreenContent(
            stats = sampleStats,
            appointments = sampleAppointments,
            patients = samplePatients,
            isLoading = false,
            currentUserProfile = UserProfile(id = "admin_uid", email = "admin@example.com"),
            onRefresh = {},
            onUpdateStatus = { _, _ -> },
            onExitAdmin = {},
            onSignOut = {}
        )
    }
}
