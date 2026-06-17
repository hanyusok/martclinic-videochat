package com.example.martclinic_videochat.presentation.ui

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.window.Dialog
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import com.example.martclinic_videochat.domain.model.Patient
import com.example.martclinic_videochat.presentation.viewmodel.MyPageViewModel
import io.github.jan.supabase.auth.status.SessionStatus

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun MyPageScreen(
    viewModel: MyPageViewModel = hiltViewModel(),
    onNavigateToAdmin: () -> Unit = {}
) {
    val context = LocalContext.current
    val patient by viewModel.patient.collectAsState()
    val patients by viewModel.patients.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val sessionStatus by viewModel.sessionStatus.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()

    LaunchedEffect(userProfile) {
        if (userProfile?.role == com.example.martclinic_videochat.domain.model.UserRole.ADMIN) {
            onNavigateToAdmin()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("마이페이지", fontWeight = FontWeight.Bold) },
                actions = {
                    if (sessionStatus is SessionStatus.Authenticated) {
                        IconButton(onClick = { viewModel.signOut() }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                                contentDescription = "로그아웃",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (sessionStatus) {
                is SessionStatus.Initializing -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is SessionStatus.Authenticated -> {
                    if (patient != null) {
                        MyPageDashboard(
                            patients = patients,
                            viewModel = viewModel,
                            onUpdateProfile = { id, name, phone, resident, relationship ->
                                viewModel.updatePatientProfile(
                                    patientId = id,
                                    nameInput = name,
                                    phoneInput = phone,
                                    residentInput = resident,
                                    relationshipInput = relationship,
                                    onSuccess = {
                                        Toast.makeText(context, "정보가 수정되었습니다.", Toast.LENGTH_SHORT).show()
                                    },
                                    onError = { error ->
                                        Toast.makeText(context, error, Toast.LENGTH_LONG).show()
                                    }
                                )
                            },
                            onAddFamily = { name, phone, resident, relationship ->
                                viewModel.createPatientProfile(
                                    nameInput = name,
                                    phoneInput = phone,
                                    residentInput = resident,
                                    relationshipInput = relationship,
                                    onSuccess = {
                                        Toast.makeText(context, "가족 정보가 등록되었습니다.", Toast.LENGTH_SHORT).show()
                                    },
                                    onError = { error ->
                                        Toast.makeText(context, error, Toast.LENGTH_LONG).show()
                                    }
                                )
                            },
                            onDeleteProfile = { id ->
                                viewModel.deletePatientProfile(
                                    patientId = id,
                                    onSuccess = {
                                        Toast.makeText(context, "정보가 삭제되었습니다.", Toast.LENGTH_SHORT).show()
                                    },
                                    onError = { error ->
                                        Toast.makeText(context, error, Toast.LENGTH_LONG).show()
                                    }
                                )
                            }
                        )
                    } else if (!isLoading) {
                        ProfileRegistrationForm(
                            viewModel = viewModel,
                            onCancel = { viewModel.signOut() },
                            onSubmit = { name, phone, resident, skipEmr ->
                                viewModel.createPatientProfile(
                                    nameInput = name,
                                    phoneInput = phone,
                                    residentInput = resident,
                                    relationshipInput = "본인",
                                    skipEmrCheck = skipEmr,
                                    onSuccess = {
                                        Toast.makeText(context, "환자 정보가 등록되었습니다.", Toast.LENGTH_SHORT).show()
                                    },
                                    onError = { error ->
                                        Toast.makeText(context, error, Toast.LENGTH_LONG).show()
                                    }
                                )
                            }
                        )
                    }
                }
                else -> {
                    AuthScreen(
                        isLoading = isLoading,
                        onLogin = { email, password ->
                            viewModel.signInWithEmail(email, password, onError = { error ->
                                Toast.makeText(context, error, Toast.LENGTH_LONG).show()
                            })
                        },
                        onSignUp = { email, password ->
                            viewModel.signUpWithEmail(email, password, onError = { error ->
                                Toast.makeText(context, error, Toast.LENGTH_LONG).show()
                            })
                        },
                        onGoogleLogin = {
                            viewModel.signInWithGoogle(onError = { error ->
                                Toast.makeText(context, error, Toast.LENGTH_LONG).show()
                            })
                        },
                        onKakaoLogin = {
                            viewModel.signInWithKakao(onError = { error ->
                                Toast.makeText(context, error, Toast.LENGTH_LONG).show()
                            })
                        }
                    )
                }
            }

            if (isLoading && sessionStatus !is SessionStatus.Initializing) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
        }
    }
}

@Composable
fun MyPageDashboard(
    patients: List<Patient>,
    onUpdateProfile: (String, String, String, String, String) -> Unit,
    onAddFamily: (String, String, String, String) -> Unit,
    onDeleteProfile: (String) -> Unit,
    viewModel: MyPageViewModel
) {
    var editingPatient by remember { mutableStateOf<Patient?>(null) }
    var isAddingFamily by remember { mutableStateOf(false) }
    var showEmrSearchDialog by remember { mutableStateOf(false) }
    var searchTargetId by remember { mutableStateOf<String?>(null) }
    
    val context = LocalContext.current
    val emrSearchResults by viewModel.emrSearchResults.collectAsState()
    val isEmrLoading by viewModel.isEmrLoading.collectAsState()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Patient Profiles Section
        item {
            Text(
                text = "가족 프로필 관리",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        items(patients) { p ->
            PatientProfileCard(
                patient = p,
                onEditClick = { editingPatient = p },
                onSyncClick = {
                    searchTargetId = p.id
                    viewModel.searchEmrPatients(p.name)
                    showEmrSearchDialog = true
                }
            )
        }

        item {
            Button(
                onClick = { isAddingFamily = true },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer, contentColor = MaterialTheme.colorScheme.onSecondaryContainer)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("가족 추가하기", fontWeight = FontWeight.Bold)
            }
        }
    }

    editingPatient?.let { p ->
        PatientProfileEditDialog(
            patient = p,
            viewModel = viewModel,
            onDismiss = { editingPatient = null },
            onConfirm = { name, phone, resident, relationship ->
                onUpdateProfile(p.id!!, name, phone, resident, relationship)
                editingPatient = null
            },
            onDelete = {
                onDeleteProfile(p.id!!)
                editingPatient = null
            }
        )
    }

    if (isAddingFamily) {
        // We can reuse the edit dialog for adding by passing a dummy patient
        val userId = viewModel.patient.value?.user_id ?: ""
        PatientProfileEditDialog(
            patient = Patient(user_id = userId, name = "", phone = "", resident_number = "", relationship = "배우자"),
            isNew = true,
            viewModel = viewModel,
            onDismiss = { isAddingFamily = false },
            onConfirm = { name, phone, resident, relationship ->
                onAddFamily(name, phone, resident, relationship)
                isAddingFamily = false
            }
        )
    }

    if (showEmrSearchDialog) {
        Dialog(onDismissRequest = { showEmrSearchDialog = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "EMR 환자 검색 결과",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    if (isEmrLoading) {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                    } else {
                        val exists = emrSearchResults.isNotEmpty()
                        
                        Text(
                            text = if (exists) "EMR 등록 환자 확인됨 (${emrSearchResults.size}명)" else "EMR 등록 정보 없음",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (exists) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                        )

                        if (exists) {
                            LazyColumn(modifier = Modifier.heightIn(max = 300.dp)) {
                                items(emrSearchResults) { p ->
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp),
                                        onClick = {
                                            // Use the dedicated sync method for EMR results
                                            viewModel.syncWithEmrRecord(
                                                emrPatient = p,
                                                targetPatientId = searchTargetId,
                                                onSuccess = {
                                                    Toast.makeText(context, "EMR 정보와 동기화되었습니다.", Toast.LENGTH_SHORT).show()
                                                },
                                                onError = { error ->
                                                    Toast.makeText(context, error, Toast.LENGTH_LONG).show()
                                                }
                                            )
                                            showEmrSearchDialog = false
                                        }
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Row(
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Text(text = "${p.name}", fontWeight = FontWeight.Bold)
                                                Text(text = if (p.sex == "1") "남성" else "여성", style = MaterialTheme.typography.bodySmall)
                                            }
                                            Text(text = "생년월일: ${p.birth_date}", style = MaterialTheme.typography.bodySmall)
                                            Text(text = "주민번호: ${p.resident_number}", style = MaterialTheme.typography.bodySmall)
                                        }
                                    }
                                }
                            }
                        } else {
                            Text(
                                text = "입력하신 이름으로 등록된 환자가 병원 데이터베이스에 존재하지 않습니다.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    TextButton(
                        onClick = { showEmrSearchDialog = false },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("닫기")
                    }
                }
            }
        }
    }
}

@Composable
fun PatientProfileCard(
    patient: Patient,
    onEditClick: () -> Unit,
    onSyncClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = if (patient.relationship == "본인") MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(
                        text = patient.relationship,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (patient.relationship == "본인") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                    )
                    Text(
                        text = "${patient.name} 님",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (patient.relationship == "본인") MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    patient.clinic_patient_number?.let {
                        Text(
                            text = "번호: $it",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (patient.relationship == "본인") MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }
                Row {
                    IconButton(onClick = onSyncClick, modifier = Modifier.size(40.dp)) {
                        Icon(
                            imageVector = Icons.Default.Sync,
                            contentDescription = "EMR 동기화",
                            modifier = Modifier.size(20.dp),
                            tint = if (patient.relationship == "본인") MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = onEditClick, modifier = Modifier.size(40.dp)) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "정보 수정",
                            modifier = Modifier.size(20.dp),
                            tint = if (patient.relationship == "본인") MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Phone,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = if (patient.relationship == "본인") MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = patient.phone,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (patient.relationship == "본인") MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.AccountBox,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = if (patient.relationship == "본인") MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                )
                Spacer(modifier = Modifier.width(6.dp))
                val maskedResident = if (patient.resident_number.contains("-")) {
                    val parts = patient.resident_number.split("-")
                    if (parts.size == 2) {
                        "${parts[0]}-*******"
                    } else {
                        "******-*******"
                    }
                } else {
                    "******-*******"
                }
                Text(
                    text = maskedResident,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (patient.relationship == "본인") MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun ProfileRegistrationForm(
    onCancel: () -> Unit,
    onSubmit: (String, String, String, Boolean) -> Unit,
    viewModel: MyPageViewModel // ViewModel passed from parent
) {
    var name by remember { mutableStateOf("") }
    var phoneValue by remember { mutableStateOf(TextFieldValue("")) }
    var residentValue by remember { mutableStateOf(TextFieldValue("")) }

    val emrSearchResults by viewModel.emrSearchResults.collectAsState()
    val isEmrLoading by viewModel.isEmrLoading.collectAsState()

    var showEmrSearchDialog by remember { mutableStateOf(false) }

    var nameError by remember { mutableStateOf<String?>(null) }
    var phoneError by remember { mutableStateOf<String?>(null) }
    var residentError by remember { mutableStateOf<String?>(null) }

    val phone = phoneValue.text
    val residentNumber = residentValue.text

    val isNameValid = name.isNotBlank() && name.length >= 2
    val isPhoneValid = phone.matches(Regex("^01[016789]-\\d{3,4}-\\d{4}$"))
    val isResidentValid = residentNumber.matches(Regex("^\\d{6}-\\d{7}$"))

    fun formatPhone(input: String): String {
        val digits = input.filter { it.isDigit() }
        return when {
            digits.length <= 3 -> digits
            digits.length <= 7 -> "${digits.take(3)}-${digits.drop(3)}"
            else -> "${digits.take(3)}-${digits.substring(3, 7)}-${digits.drop(7).take(4)}"
        }
    }

    fun formatResident(input: String): String {
        val digits = input.filter { it.isDigit() }
        return when {
            digits.length <= 6 -> digits
            else -> "${digits.take(6)}-${digits.drop(6).take(7)}"
        }
    }

    fun onPhoneChange(newValue: TextFieldValue) {
        val oldText = phoneValue.text
        val newText = newValue.text
        
        // Only format if text actually changed and it's not a deletion of a hyphen
        val isDeletion = newText.length < oldText.length
        val wasHyphenDeleted = isDeletion && oldText.getOrNull(newValue.selection.start) == '-'
        
        val digitsOnly = if (wasHyphenDeleted) {
            // If user manually deleted a hyphen, delete the digit before it too to avoid jumpy behavior
            val textBeforeHyphen = oldText.take(newValue.selection.start)
            val textAfterHyphen = oldText.drop(newValue.selection.start + 1)
            (textBeforeHyphen.dropLast(1) + textAfterHyphen).filter { it.isDigit() }
        } else {
            newText.filter { it.isDigit() }
        }

        val formatted = formatPhone(digitsOnly)
        if (formatted.length <= 13) {
            // Calculate new cursor position
            // This is a simplified approach: move cursor to end for formatting simplicity 
            // but we try to preserve it if possible. 
            // For true 'order', we map digit index back to formatted index.
            var digitCountBeforeCursor = newText.take(newValue.selection.start).count { it.isDigit() }
            if (wasHyphenDeleted) digitCountBeforeCursor = (digitCountBeforeCursor - 1).coerceAtLeast(0)
            
            var newCursorPos = 0
            var currentDigits = 0
            for (i in formatted.indices) {
                if (currentDigits == digitCountBeforeCursor) break
                if (formatted[i].isDigit()) currentDigits++
                newCursorPos++
            }
            
            phoneValue = TextFieldValue(formatted, TextRange(newCursorPos))
            phoneError = if (formatted.isNotEmpty() && !formatted.matches(Regex("^01[016789]-\\d{3,4}-\\d{4}$"))) 
                "형식에 맞춰 입력해 주세요 (예: 010-1234-5678)" else null
        }
    }

    fun onResidentChange(newValue: TextFieldValue) {
        val oldText = residentValue.text
        val newText = newValue.text
        val isDeletion = newText.length < oldText.length
        val wasHyphenDeleted = isDeletion && oldText.getOrNull(newValue.selection.start) == '-'

        val digitsOnly = if (wasHyphenDeleted) {
            val textBeforeHyphen = oldText.take(newValue.selection.start)
            val textAfterHyphen = oldText.drop(newValue.selection.start + 1)
            (textBeforeHyphen.dropLast(1) + textAfterHyphen).filter { it.isDigit() }
        } else {
            newText.filter { it.isDigit() }
        }

        val formatted = formatResident(digitsOnly)
        if (formatted.length <= 14) {
            var digitCountBeforeCursor = newText.take(newValue.selection.start).count { it.isDigit() }
            if (wasHyphenDeleted) digitCountBeforeCursor = (digitCountBeforeCursor - 1).coerceAtLeast(0)

            var newCursorPos = 0
            var currentDigits = 0
            for (i in formatted.indices) {
                if (currentDigits == digitCountBeforeCursor) break
                if (formatted[i].isDigit()) currentDigits++
                newCursorPos++
            }

            residentValue = TextFieldValue(formatted, TextRange(newCursorPos))
            residentError = if (formatted.isNotEmpty() && !formatted.matches(Regex("^\\d{6}-\\d{7}$"))) 
                "형식에 맞춰 입력해 주세요 (예: 000000-0000000)" else null
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "환자 정보 등록",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Text(
            text = "마트클리닉 서비스를 사용하기 위해 최초 1회 본인 확인 정보 등록이 필요합니다.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = name,
            onValueChange = { 
                if (it.length <= 20) {
                    name = it
                    nameError = if (it.isNotEmpty() && it.length < 2) "이름은 2자 이상이어야 합니다." else null
                }
            },
            label = { Text("이름") },
            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
            isError = nameError != null,
            supportingText = { nameError?.let { Text(it) } },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = { 
                if (name.isNotBlank()) {
                    viewModel.searchEmrPatients(name)
                    showEmrSearchDialog = true
                }
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
        ) {
            Icon(Icons.Default.Search, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("EMR 환자 검색")
        }

        OutlinedTextField(
            value = phoneValue,
            onValueChange = { onPhoneChange(it) },
            label = { Text("전화번호 (예: 010-1234-5678)") },
            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
            isError = phoneError != null,
            supportingText = { phoneError?.let { Text(it) } },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = residentValue,
            onValueChange = { onResidentChange(it) },
            label = { Text("주민등록번호 (예: 000000-0000000)") },
            leadingIcon = { Icon(Icons.Default.AccountBox, contentDescription = null) },
            isError = residentError != null,
            supportingText = { residentError?.let { Text(it) } },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Cancel", fontWeight = FontWeight.Bold)
            }
            Button(
                onClick = {
                    if (isNameValid && isPhoneValid && isResidentValid) {
                        onSubmit(name, phone, residentNumber, false)
                    }
                },
                enabled = isNameValid && isPhoneValid && isResidentValid,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1.5f)
            ) {
                Text("Save", fontWeight = FontWeight.Bold)
            }
        }
    }

    if (showEmrSearchDialog) {
        Dialog(onDismissRequest = { showEmrSearchDialog = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "EMR 환자 검색 결과",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    if (isEmrLoading) {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                    } else {
                        val exists = emrSearchResults.isNotEmpty()

                        Text(
                            text = if (exists) "EMR 등록 환자 확인됨 (${emrSearchResults.size}명)" else "EMR 등록 정보 없음",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (exists) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                        )

                        if (exists) {
                            LazyColumn(modifier = Modifier.heightIn(max = 300.dp)) {
                                items(emrSearchResults) { patient ->
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp),
                                        onClick = {
                                            name = patient.name ?: ""
                                            val resNum = patient.resident_number ?: ""
                                            residentValue = TextFieldValue(resNum, TextRange(resNum.length))
                                            patient.phone?.let {
                                                phoneValue = TextFieldValue(it, TextRange(it.length))
                                            }
                                            showEmrSearchDialog = false
                                        }
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Row(
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Text(text = "${patient.name}", fontWeight = FontWeight.Bold)
                                                Text(text = if (patient.sex == "1") "남성" else "여성", style = MaterialTheme.typography.bodySmall)
                                            }
                                            Text(text = "생년월일: ${patient.birth_date}", style = MaterialTheme.typography.bodySmall)
                                            Text(text = "주민번호: ${patient.resident_number}", style = MaterialTheme.typography.bodySmall)
                                        }
                                    }
                                }
                            }
                        } else {
                            Text(
                                text = "입력하신 이름으로 등록된 환자가 병원 데이터베이스에 존재하지 않습니다. 정보를 직접 입력하여 등록하시겠습니까?",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            
                            Button(
                                onClick = {
                                    if (isNameValid && isPhoneValid && isResidentValid) {
                                        onSubmit(name, phone, residentNumber, true)
                                        showEmrSearchDialog = false
                                    }
                                },
                                enabled = isNameValid && isPhoneValid && isResidentValid,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("기록 없이 바로 저장")
                            }
                        }
                    }

                    TextButton(
                        onClick = { showEmrSearchDialog = false },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("닫기")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PatientProfileEditDialog(
    patient: Patient,
    isNew: Boolean = false,
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, String) -> Unit,
    onDelete: (() -> Unit)? = null,
    viewModel: MyPageViewModel // Added viewModel parameter
) {
    var name by remember { mutableStateOf(patient.name) }
    var phoneValue by remember { mutableStateOf(TextFieldValue(patient.phone, TextRange(patient.phone.length))) }
    var residentValue by remember { mutableStateOf(TextFieldValue(patient.resident_number, TextRange(patient.resident_number.length))) }
    var relationship by remember { mutableStateOf(patient.relationship) }

    val emrSearchResults by viewModel.emrSearchResults.collectAsState()
    val isEmrLoading by viewModel.isEmrLoading.collectAsState()
    var showEmrSearchDialog by remember { mutableStateOf(false) }

    var nameError by remember { mutableStateOf<String?>(null) }
    var phoneError by remember { mutableStateOf<String?>(null) }
    var residentError by remember { mutableStateOf<String?>(null) }

    val phone = phoneValue.text
    val residentNumber = residentValue.text

    val isNameValid = name.isNotBlank() && name.length >= 2
    val isPhoneValid = phone.matches(Regex("^01[016789]-\\d{3,4}-\\d{4}$"))
    val isResidentValid = residentNumber.matches(Regex("^\\d{6}-\\d{7}$"))
    val relationships = listOf("본인", "배우자", "자녀", "부모", "기타")

    fun formatPhone(input: String): String {
        val digits = input.filter { it.isDigit() }
        return when {
            digits.length <= 3 -> digits
            digits.length <= 7 -> "${digits.take(3)}-${digits.drop(3)}"
            else -> "${digits.take(3)}-${digits.substring(3, 7)}-${digits.drop(7).take(4)}"
        }
    }

    fun formatResident(input: String): String {
        val digits = input.filter { it.isDigit() }
        return when {
            digits.length <= 6 -> digits
            else -> "${digits.take(6)}-${digits.drop(6).take(7)}"
        }
    }

    fun onPhoneChange(newValue: TextFieldValue) {
        val oldText = phoneValue.text
        val newText = newValue.text
        val isDeletion = newText.length < oldText.length
        val wasHyphenDeleted = isDeletion && oldText.getOrNull(newValue.selection.start) == '-'
        
        val digitsOnly = if (wasHyphenDeleted) {
            val textBeforeHyphen = oldText.take(newValue.selection.start)
            val textAfterHyphen = oldText.drop(newValue.selection.start + 1)
            (textBeforeHyphen.dropLast(1) + textAfterHyphen).filter { it.isDigit() }
        } else {
            newText.filter { it.isDigit() }
        }

        val formatted = formatPhone(digitsOnly)
        if (formatted.length <= 13) {
            var digitCountBeforeCursor = newText.take(newValue.selection.start).count { it.isDigit() }
            if (wasHyphenDeleted) digitCountBeforeCursor = (digitCountBeforeCursor - 1).coerceAtLeast(0)
            
            var newCursorPos = 0
            var currentDigits = 0
            for (i in formatted.indices) {
                if (currentDigits == digitCountBeforeCursor) break
                if (formatted[i].isDigit()) currentDigits++
                newCursorPos++
            }
            if (newCursorPos < formatted.length && formatted[newCursorPos] == '-' && !isDeletion) {
                newCursorPos++
            }
            
            phoneValue = TextFieldValue(formatted, TextRange(newCursorPos))
            phoneError = if (formatted.isNotEmpty() && !formatted.matches(Regex("^01[016789]-\\d{3,4}-\\d{4}$"))) 
                "형식에 맞춰 입력해 주세요 (예: 010-1234-5678)" else null
        }
    }

    fun onResidentChange(newValue: TextFieldValue) {
        val oldText = residentValue.text
        val newText = newValue.text
        val isDeletion = newText.length < oldText.length
        val wasHyphenDeleted = isDeletion && oldText.getOrNull(newValue.selection.start) == '-'

        val digitsOnly = if (wasHyphenDeleted) {
            val textBeforeHyphen = oldText.take(newValue.selection.start)
            val textAfterHyphen = oldText.drop(newValue.selection.start + 1)
            (textBeforeHyphen.dropLast(1) + textAfterHyphen).filter { it.isDigit() }
        } else {
            newText.filter { it.isDigit() }
        }

        val formatted = formatResident(digitsOnly)
        if (formatted.length <= 14) {
            var digitCountBeforeCursor = newText.take(newValue.selection.start).count { it.isDigit() }
            if (wasHyphenDeleted) digitCountBeforeCursor = (digitCountBeforeCursor - 1).coerceAtLeast(0)

            var newCursorPos = 0
            var currentDigits = 0
            for (i in formatted.indices) {
                if (currentDigits == digitCountBeforeCursor) break
                if (formatted[i].isDigit()) currentDigits++
                newCursorPos++
            }
            if (newCursorPos < formatted.length && formatted[newCursorPos] == '-' && !isDeletion) {
                newCursorPos++
            }

            residentValue = TextFieldValue(formatted, TextRange(newCursorPos))
            residentError = if (formatted.isNotEmpty() && !formatted.matches(Regex("^\\d{6}-\\d{7}$"))) 
                "형식에 맞춰 입력해 주세요 (예: 000000-0000000)" else null
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isNew) "가족 추가" else "프로필 수정",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    if (!isNew && patient.relationship != "본인") {
                        IconButton(onClick = { onDelete?.invoke() }) {
                            Icon(Icons.Default.Delete, contentDescription = "삭제", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Relationship Selection (only if not '본인' editing self)
                if (patient.relationship != "본인" || isNew) {
                    Text(
                        text = "관계 선택",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        relationships.filter { it != "본인" }.forEach { rel ->
                            FilterChip(
                                selected = relationship == rel,
                                onClick = { relationship = rel },
                                label = { Text(rel) },
                                leadingIcon = if (relationship == rel) {
                                    { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp)) }
                                } else null
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }

                OutlinedTextField(
                    value = name,
                    onValueChange = { 
                        if (it.length <= 20) {
                            name = it
                            nameError = if (it.isNotEmpty() && it.length < 2) "이름은 2자 이상이어야 합니다." else null
                        }
                    },
                    label = { Text("이름") },
                    isError = nameError != null,
                    supportingText = { nameError?.let { Text(it) } },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Button(
                    onClick = { 
                        if (name.isNotBlank()) {
                            viewModel.searchEmrPatients(name)
                            showEmrSearchDialog = true
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                ) {
                    Icon(Icons.Default.Search, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("EMR 환자 검색")
                }

                OutlinedTextField(
                    value = phoneValue,
                    onValueChange = { onPhoneChange(it) },
                    label = { Text("전화번호") },
                    isError = phoneError != null,
                    supportingText = { phoneError?.let { Text(it) } },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = residentValue,
                    onValueChange = { onResidentChange(it) },
                    label = { Text("주민등록번호") },
                    isError = residentError != null,
                    supportingText = { residentError?.let { Text(it) } },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = {
                            if (isNameValid && isPhoneValid && isResidentValid) {
                                onConfirm(name, phone, residentNumber, relationship)
                            }
                        },
                        enabled = isNameValid && isPhoneValid && isResidentValid,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(if (isNew) "Register" else "Save")
                    }
                }
            }
        }
    }

    if (showEmrSearchDialog) {
        Dialog(onDismissRequest = { showEmrSearchDialog = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "EMR 환자 검색 결과",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    if (isEmrLoading) {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                    } else {
                        val exists = emrSearchResults.isNotEmpty()

                        Text(
                            text = if (exists) "EMR 등록 환자 확인됨 (${emrSearchResults.size}명)" else "EMR 등록 정보 없음",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (exists) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                        )

                        if (exists) {
                            LazyColumn(modifier = Modifier.heightIn(max = 300.dp)) {
                                items(emrSearchResults) { p ->
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp),
                                        onClick = {
                                            name = p.name ?: ""
                                            val resNum = p.resident_number ?: ""
                                            residentValue = TextFieldValue(resNum, TextRange(resNum.length))
                                            p.phone?.let {
                                                phoneValue = TextFieldValue(it, TextRange(it.length))
                                            }
                                            showEmrSearchDialog = false
                                        }
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Row(
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Text(text = "${p.name}", fontWeight = FontWeight.Bold)
                                                Text(text = if (p.sex == "1") "남성" else "여성", style = MaterialTheme.typography.bodySmall)
                                            }
                                            Text(text = "생년월일: ${p.birth_date}", style = MaterialTheme.typography.bodySmall)
                                            Text(text = "주민번호: ${p.resident_number}", style = MaterialTheme.typography.bodySmall)
                                        }
                                    }
                                }
                            }
                        } else {
                            Text(
                                text = "입력하신 이름으로 등록된 환자가 병원 데이터베이스에 존재하지 않습니다. 정보를 직접 입력하여 등록하시겠습니까?",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            
                            Button(
                                onClick = {
                                    if (isNameValid && isPhoneValid && isResidentValid) {
                                        onConfirm(name, phone, residentNumber, relationship)
                                        showEmrSearchDialog = false
                                    }
                                },
                                enabled = isNameValid && isPhoneValid && isResidentValid,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("기록 없이 바로 저장")
                            }
                        }
                    }

                    TextButton(
                        onClick = { showEmrSearchDialog = false },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("닫기")
                    }
                }
            }
        }
    }
}

@Composable
fun AuthScreen(
    isLoading: Boolean,
    onLogin: (String, String) -> Unit,
    onSignUp: (String, String) -> Unit,
    onGoogleLogin: () -> Unit,
    onKakaoLogin: () -> Unit
) {
    var isLoginTab by remember { mutableStateOf(true) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "마트클리닉",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 24.dp)
            )
            Text(
                text = "비대면 진료와 처방을 간편하게",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 24.dp)
            )
        }

        item {
            SecondaryTabRow(
                selectedTabIndex = if (isLoginTab) 0 else 1,
                modifier = Modifier.fillMaxWidth()
            ) {
                Tab(
                    selected = isLoginTab,
                    onClick = { isLoginTab = true },
                    text = { Text("로그인", fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = !isLoginTab,
                    onClick = { isLoginTab = false },
                    text = { Text("회원가입", fontWeight = FontWeight.Bold) }
                )
            }
        }

        item {
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("이메일 주소") },
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("비밀번호") },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                trailingIcon = {
                    val icon = if (isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                    val description = if (isPasswordVisible) "비밀번호 숨기기" else "비밀번호 표시"
                    IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                        Icon(imageVector = icon, contentDescription = description)
                    }
                },
                singleLine = true,
                visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = {
                    if (isLoginTab) {
                        if (email.isNotBlank() && password.isNotBlank()) {
                            onLogin(email, password)
                        }
                    } else {
                        if (email.isNotBlank() && password.isNotBlank()) {
                            onSignUp(email, password)
                        }
                    }
                },
                enabled = !isLoading && email.isNotBlank() && password.isNotBlank(),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text(
                    text = if (isLoginTab) "로그인" else "회원가입",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HorizontalDivider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.outlineVariant)
                Text(
                    text = " 또는 간편 로그인 ",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                HorizontalDivider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.outlineVariant)
            }
        }

        // Kakao Social Login Button
        item {
            Button(
                onClick = onKakaoLogin,
                enabled = !isLoading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFEE500),
                    contentColor = Color(0xFF191919)
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text("💬", modifier = Modifier.padding(end = 8.dp))
                    Text("카카오로 로그인", fontWeight = FontWeight.Bold)
                }
            }
        }

        // Google Social Login Button
        item {
            OutlinedButton(
                onClick = onGoogleLogin,
                enabled = !isLoading,
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = Color.White,
                    contentColor = Color.DarkGray
                ),
                border = BorderStroke(1.dp, Color(0xFFCCCCCC)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text("🌐", modifier = Modifier.padding(end = 8.dp))
                    Text("Google로 로그인", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
