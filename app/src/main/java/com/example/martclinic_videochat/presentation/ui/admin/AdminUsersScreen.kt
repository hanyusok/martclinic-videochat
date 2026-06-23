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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.martclinic_videochat.domain.model.Appointment
import com.example.martclinic_videochat.domain.model.Patient
import com.example.martclinic_videochat.domain.model.Pharmacy
import com.example.martclinic_videochat.presentation.viewmodel.AdminViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminUsersScreen(
    viewModel: AdminViewModel = hiltViewModel()
) {
    val appointments by viewModel.allAppointments.collectAsStateWithLifecycle()
    val patients by viewModel.allPatients.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val selectedPatientFavorites by viewModel.selectedPatientFavorites.collectAsStateWithLifecycle()
    val masterPharmacies by viewModel.masterPharmacies.collectAsStateWithLifecycle()

    var selectedPatient by remember { mutableStateOf<Patient?>(null) }

    BoxWithConstraints(
        modifier = Modifier.fillMaxSize()
    ) {
        val isWideScreen = maxWidth >= 600.dp

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("사용자 관리", fontWeight = FontWeight.Bold) },
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
                            Text(
                                "사용자 목록",
                                modifier = Modifier.padding(16.dp),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Box(modifier = Modifier.weight(1f)) {
                                UserDirectoryList(
                                    patients = patients,
                                    onItemClick = { selectedPatient = it })
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
                            val pat = selectedPatient
                            if (pat != null) {
                                PatientDetailPane(
                                    patient = pat,
                                    appointments = appointments,
                                    favoritePharmacies = selectedPatientFavorites,
                                    masterPharmacies = masterPharmacies,
                                    onLoadFavorites = { viewModel.loadPatientFavorites(it) },
                                    onAddFavorite = { id, p ->
                                        viewModel.addPatientFavoritePharmacy(
                                            id,
                                            p
                                        )
                                    },
                                    onRemoveFavorite = { id, pid ->
                                        viewModel.removePatientFavoritePharmacy(
                                            id,
                                            pid
                                        )
                                    },
                                    onToggleDefault = { id, pid, isDef ->
                                        viewModel.togglePatientDefaultPharmacy(
                                            id,
                                            pid,
                                            isDef
                                        )
                                    },
                                    onUpdateProfile = { viewModel.updatePatientProfile(it) },
                                    onDeleteProfile = { viewModel.deletePatientProfile(it) },
                                    onDismiss = { selectedPatient = null }
                                )
                            } else {
                                EmptyDetailPlaceholder("사용자 목록에서 환자를 선택하면 이곳에 프로필 상세 정보와 진료 이력이 표시됩니다.")
                            }
                        }
                    }
                } else {
                    var showDetailBottomSheet by remember { mutableStateOf(false) }

                    Column(modifier = Modifier.fillMaxSize()) {
                        Text(
                            "사용자 목록",
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Box(modifier = Modifier.weight(1f)) {
                            UserDirectoryList(patients = patients, onItemClick = {
                                selectedPatient = it
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
                                selectedPatient = null
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
                                selectedPatient?.let { pat ->
                                    PatientDetailPane(
                                        patient = pat,
                                        appointments = appointments,
                                        favoritePharmacies = selectedPatientFavorites,
                                        masterPharmacies = masterPharmacies,
                                        onLoadFavorites = { viewModel.loadPatientFavorites(it) },
                                        onAddFavorite = { id, p ->
                                            viewModel.addPatientFavoritePharmacy(
                                                id,
                                                p
                                            )
                                        },
                                        onRemoveFavorite = { id, pid ->
                                            viewModel.removePatientFavoritePharmacy(
                                                id,
                                                pid
                                            )
                                        },
                                        onToggleDefault = { id, pid, isDef ->
                                            viewModel.togglePatientDefaultPharmacy(
                                                id,
                                                pid,
                                                isDef
                                            )
                                        },
                                        onUpdateProfile = { viewModel.updatePatientProfile(it) },
                                        onDeleteProfile = { viewModel.deletePatientProfile(it) },
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
                        Text(patient.name ?: "불명", fontWeight = FontWeight.Bold)
                        Text(patient.phone ?: "", style = MaterialTheme.typography.bodySmall)
                    }
                    Text(
                        text = patient.relationship ?: "본인",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatientDetailPane(
    patient: Patient,
    appointments: List<Appointment>,
    favoritePharmacies: List<Pharmacy>,
    masterPharmacies: List<Pharmacy>,
    onLoadFavorites: (String) -> Unit,
    onAddFavorite: (String, Pharmacy) -> Unit,
    onRemoveFavorite: (String, String) -> Unit,
    onToggleDefault: (String, String, Boolean) -> Unit,
    onUpdateProfile: (Patient) -> Unit,
    onDeleteProfile: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val patientAppointments = remember(patient, appointments) {
        appointments.filter { it.patient_id == patient.id }
    }

    val context = LocalContext.current
    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showAddPharmacyDialog by remember { mutableStateOf(false) }

    var editName by remember { mutableStateOf(patient.name ?: "") }
    var editPhone by remember { mutableStateOf(patient.phone ?: "") }
    var editResident by remember { mutableStateOf(patient.resident_number ?: "") }
    var editRelation by remember { mutableStateOf(patient.relationship ?: "") }
    var editChartNumber by remember { mutableStateOf(patient.clinic_patient_number ?: "") }

    LaunchedEffect(patient.id) {
        patient.id?.let { onLoadFavorites(it) }
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
                    text = "환자 프로필 관리",
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
                Column(modifier = Modifier.weight(1f)) {
                    Text(patient.name ?: "불명", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Text(
                        text = "관계: ${patient.relationship ?: "본인"}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
                
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(onClick = {
                        editName = patient.name ?: ""
                        editPhone = patient.phone ?: ""
                        editResident = patient.resident_number ?: ""
                        editRelation = patient.relationship ?: ""
                        editChartNumber = patient.clinic_patient_number ?: ""
                        showEditDialog = true
                    }) {
                        Icon(Icons.Default.Edit, contentDescription = "프로필 수정", tint = MaterialTheme.colorScheme.primary)
                    }
                    IconButton(onClick = { showDeleteConfirm = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "환자 삭제", tint = MaterialTheme.colorScheme.error)
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                DetailRow("전화번호", patient.phone ?: "")
                val residentNumber = patient.resident_number
                val maskedResident = if (residentNumber?.contains("-") == true) {
                    val parts = residentNumber?.split("-") ?: emptyList()
                    if (parts.size == 2) "${parts[0]}-*******" else "******-*******"
                } else {
                    "******-*******"
                }
                DetailRow("주민등록번호", maskedResident)
                DetailRow("차트 등록 번호", patient.clinic_patient_number ?: "미확인 (EMR 미동기화)")
            }

            HorizontalDivider()

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "단골 약국 관리",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Button(
                    onClick = { showAddPharmacyDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer, contentColor = MaterialTheme.colorScheme.onPrimaryContainer),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("단골약국 추가", style = MaterialTheme.typography.labelMedium)
                }
            }

            if (favoritePharmacies.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("등록된 단골 약국이 없습니다.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.outline)
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 140.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(favoritePharmacies) { fav ->
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (fav.is_default) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                                                 else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(fav.pharmacy_name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                        if (fav.is_default) {
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Surface(
                                                color = MaterialTheme.colorScheme.primary,
                                                shape = RoundedCornerShape(4.dp)
                                            ) {
                                                Text(
                                                    text = "대표",
                                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = Color.White
                                                )
                                            }
                                        }
                                    }
                                    Text(fav.address, style = MaterialTheme.typography.bodySmall, maxLines = 1)
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    TextButton(
                                        onClick = { onToggleDefault(patient.id!!, fav.id!!, !fav.is_default) },
                                        contentPadding = PaddingValues(horizontal = 8.dp)
                                    ) {
                                        Text(if (fav.is_default) "대표해제" else "대표지정", style = MaterialTheme.typography.labelSmall)
                                    }
                                    IconButton(
                                        onClick = { onRemoveFavorite(patient.id!!, fav.id!!) },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "삭제", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }
                }
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
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("등록된 과거 진료 내역이 없습니다.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.outline)
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 140.dp),
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

    if (showEditDialog) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("환자 프로필 수정") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(value = editName, onValueChange = { editName = it }, label = { Text("이름") })
                    OutlinedTextField(value = editPhone, onValueChange = { editPhone = it }, label = { Text("전화번호") })
                    OutlinedTextField(value = editResident, onValueChange = { editResident = it }, label = { Text("주민등록번호") })
                    OutlinedTextField(value = editRelation, onValueChange = { editRelation = it }, label = { Text("관계 (예: 본인, 자녀)") })
                    OutlinedTextField(value = editChartNumber, onValueChange = { editChartNumber = it }, label = { Text("차트번호 (EMR 동기화용)") })
                }
            },
            confirmButton = {
                Button(onClick = {
                    onUpdateProfile(
                        patient.copy(
                            name = editName,
                            phone = editPhone,
                            resident_number = editResident,
                            relationship = editRelation,
                            clinic_patient_number = editChartNumber.ifBlank { null }
                        )
                    )
                    showEditDialog = false
                    Toast.makeText(context, "환자 프로필이 수정되었습니다.", Toast.LENGTH_SHORT).show()
                }) {
                    Text("수정")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) {
                    Text("취소")
                }
            }
        )
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("환자 프로필 삭제") },
            text = { Text("${patient.name}님의 정보를 삭제하시겠습니까?\n\n이 작업은 되돌릴 수 없습니다.") },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteProfile(patient.id!!)
                        showDeleteConfirm = false
                        onDismiss()
                        Toast.makeText(context, "환자 정보가 삭제되었습니다.", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("삭제 확정")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("취소")
                }
            }
        )
    }

    if (showAddPharmacyDialog) {
        AlertDialog(
            onDismissRequest = { showAddPharmacyDialog = false },
            title = { Text("단골 약국 추가") },
            text = {
                Column {
                    Text("아래 목록에서 단골 약국으로 지정할 약국을 선택하세요.", style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.height(8.dp))
                    if (masterPharmacies.isEmpty()) {
                        Text("선택 가능한 약국 목록이 비어 있습니다.", color = MaterialTheme.colorScheme.outline)
                    } else {
                        LazyColumn(modifier = Modifier.heightIn(max = 240.dp)) {
                            items(masterPharmacies) { pharmacy ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                        .clickable {
                                            onAddFavorite(patient.id!!, pharmacy)
                                            showAddPharmacyDialog = false
                                            Toast.makeText(context, "단골 약국이 성공적으로 추가되었습니다.", Toast.LENGTH_SHORT).show()
                                        },
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text(pharmacy.pharmacy_name, fontWeight = FontWeight.Bold)
                                        Text(pharmacy.address, style = MaterialTheme.typography.bodySmall)
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showAddPharmacyDialog = false }) {
                    Text("닫기")
                }
            }
        )
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
