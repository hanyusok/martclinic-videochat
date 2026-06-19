package com.example.martclinic_videochat.presentation.ui

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
    
    val context = LocalContext.current
    val emrSearchResults by viewModel.emrSearchResults.collectAsState()
    val isEmrLoading by viewModel.isEmrLoading.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // User Account Section
        item {
            Text(
                text = "계정 정보",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Card(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Email,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "연결된 이메일",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = userProfile?.email ?: "정보 없음",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(8.dp)) }

        // Patient Profiles Section
        item {
            Text(
                text = "환자 프로필 관리",
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
                    viewModel.syncPatientWithEmrDirectly(
                        patient = p,
                        onSuccess = {
                            Toast.makeText(context, "방문 기록과 동기화되었습니다.", Toast.LENGTH_SHORT).show()
                        },
                        onError = { error ->
                            Toast.makeText(context, error, Toast.LENGTH_LONG).show()
                        }
                    )
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
                        text = patient.relationship ?: "",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (patient.relationship == "본인") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                    )
                    Text(
                        text = "${patient.name ?: ""} 님",
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
                            contentDescription = "방문 기록 동기화",
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
                    text = patient.phone ?: "",
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
                val maskedResident = if (patient.resident_number?.contains("-") == true) {
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
    var birthdate by remember { mutableStateOf("") }
    var phoneValue by remember { mutableStateOf(TextFieldValue("")) }
    var residentValue by remember { mutableStateOf(TextFieldValue("")) }

    var matchedResidentNumber by remember { mutableStateOf<String?>(null) }
    var searchAttempted by remember { mutableStateOf(false) }
    var manualRegistration by remember { mutableStateOf(false) }

    val emrSearchResults by viewModel.emrSearchResults.collectAsState()
    val isEmrLoading by viewModel.isEmrLoading.collectAsState()

    var nameError by remember { mutableStateOf<String?>(null) }
    var birthdateError by remember { mutableStateOf<String?>(null) }
    var phoneError by remember { mutableStateOf<String?>(null) }
    var residentError by remember { mutableStateOf<String?>(null) }

    val phone = phoneValue.text
    val residentNumber = residentValue.text

    val isNameValid = name.isNotBlank() && name.length >= 2
    val isBirthdateValid = birthdate.matches(Regex("^\\d{8}$"))
    val isPhoneValid = phone.matches(Regex("^01[016789]-\\d{3,4}-\\d{4}$"))
    val isResidentValid = residentNumber.matches(Regex("^\\d{6}-[\\d*]{7}$"))

    fun formatPhone(input: String): String {
        val digits = input.filter { it.isDigit() }
        return when {
            digits.length <= 3 -> digits
            digits.length <= 7 -> "${digits.take(3)}-${digits.drop(3)}"
            else -> "${digits.take(3)}-${digits.substring(3, 7)}-${digits.drop(7).take(4)}"
        }
    }

    fun formatResident(input: String): String {
        val digits = input.filter { it.isDigit() || it == '*' }
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
            (textBeforeHyphen.dropLast(1) + textAfterHyphen).filter { it.isDigit() || it == '*' }
        } else {
            newText.filter { it.isDigit() || it == '*' }
        }

        val formatted = formatResident(digitsOnly)
        if (formatted.length <= 14) {
            var digitCountBeforeCursor = newText.take(newValue.selection.start).count { it.isDigit() || it == '*' }
            if (wasHyphenDeleted) digitCountBeforeCursor = (digitCountBeforeCursor - 1).coerceAtLeast(0)

            var newCursorPos = 0
            var currentDigits = 0
            for (i in formatted.indices) {
                if (currentDigits == digitCountBeforeCursor) break
                if (formatted[i].isDigit() || formatted[i] == '*') currentDigits++
                newCursorPos++
            }
            if (newCursorPos < formatted.length && formatted[newCursorPos] == '-' && !isDeletion) {
                newCursorPos++
            }

            residentValue = TextFieldValue(formatted, TextRange(newCursorPos))
            residentError = if (formatted.isNotEmpty() && !formatted.matches(Regex("^\\d{6}-[\\d*]{7}$"))) 
                "형식에 맞춰 입력해 주세요 (예: 000000-0000000)" else null
        }
    }

    LaunchedEffect(emrSearchResults, isEmrLoading) {
        if (searchAttempted && !isEmrLoading) {
            val shortBirthdate = if (birthdate.length >= 8) birthdate.substring(2, 8) else birthdate
            val match = emrSearchResults.find { it.resident_number?.startsWith(shortBirthdate) == true }
            if (match != null) {
                matchedResidentNumber = match.resident_number
                match.phone?.let {
                    val formattedPhone = formatPhone(it.filter { c -> c.isDigit() })
                    phoneValue = TextFieldValue(formattedPhone, TextRange(formattedPhone.length))
                }
            } else {
                matchedResidentNumber = null
            }
        }
    }

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
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
                    searchAttempted = false
                }
            },
            label = { Text("이름") },
            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
            isError = nameError != null,
            supportingText = { nameError?.let { Text(it) } },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            enabled = !searchAttempted || matchedResidentNumber == null // Lock if matched
        )

        OutlinedTextField(
            value = birthdate,
            onValueChange = { 
                val digits = it.filter { c -> c.isDigit() }
                if (digits.length <= 8) {
                    birthdate = digits
                    birthdateError = if (digits.isNotEmpty() && digits.length < 8) "생년월일 8자리를 입력해 주세요 (예: 19800101)" else null
                    searchAttempted = false
                    matchedResidentNumber = null
                    manualRegistration = false
                }
            },
            label = { Text("생년월일 (8자리)") },
            leadingIcon = { Icon(Icons.Default.DateRange, contentDescription = null) },
            isError = birthdateError != null,
            supportingText = { birthdateError?.let { Text(it) } },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            enabled = !searchAttempted || matchedResidentNumber == null // Lock if matched
        )

        if (!searchAttempted || isEmrLoading || (searchAttempted && matchedResidentNumber == null && !manualRegistration)) {
            Button(
                onClick = { 
                    if (isNameValid && isBirthdateValid) {
                        searchAttempted = true
                        manualRegistration = false
                        viewModel.searchEmrPatients(name)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = isNameValid && isBirthdateValid && !isEmrLoading,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
            ) {
                if (isEmrLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onSecondary)
                } else {
                    Icon(Icons.Default.Search, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("방문 기록 조회")
                }
            }
        }

        if (searchAttempted && !isEmrLoading) {
            if (matchedResidentNumber != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "방문 기록이 확인되었습니다.",
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontWeight = FontWeight.Bold
                        )
                    }
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
            } else {
                if (!manualRegistration) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "일치하는 환자 정보가 없습니다.",
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "정보를 직접 입력하시겠습니까?",
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                    Button(
                        onClick = { manualRegistration = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.onSurfaceVariant)
                    ) {
                        Text("수기 입력")
                    }
                } else {
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
                }
            }
        }

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
            
            val canSave = (matchedResidentNumber != null && isPhoneValid) || (manualRegistration && isPhoneValid && isResidentValid)
            Button(
                onClick = {
                    if (matchedResidentNumber != null && isPhoneValid) {
                        onSubmit(name, phone, matchedResidentNumber!!, false)
                    } else if (manualRegistration && isPhoneValid && isResidentValid) {
                        onSubmit(name, phone, residentNumber, true)
                    }
                },
                enabled = canSave,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1.5f)
            ) {
                Text("Save", fontWeight = FontWeight.Bold)
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
    var name by remember { mutableStateOf(patient.name ?: "") }
    var birthdate by remember { mutableStateOf("") }
    var phoneValue by remember { 
        val text = patient.phone ?: ""
        mutableStateOf(TextFieldValue(text, TextRange(text.length))) 
    }
    var residentValue by remember { 
        val text = patient.resident_number ?: ""
        mutableStateOf(TextFieldValue(text, TextRange(text.length))) 
    }
    var relationship by remember { mutableStateOf(patient.relationship ?: "본인") }

    var matchedResidentNumber by remember { mutableStateOf<String?>(if (!isNew && (patient.resident_number?.isNotEmpty() == true)) patient.resident_number else null) }
    var searchAttempted by remember { mutableStateOf(!isNew) }
    var manualRegistration by remember { mutableStateOf(!isNew) }

    val emrSearchResults by viewModel.emrSearchResults.collectAsState()
    val isEmrLoading by viewModel.isEmrLoading.collectAsState()

    var nameError by remember { mutableStateOf<String?>(null) }
    var birthdateError by remember { mutableStateOf<String?>(null) }
    var phoneError by remember { mutableStateOf<String?>(null) }
    var residentError by remember { mutableStateOf<String?>(null) }

    val phone = phoneValue.text
    val residentNumber = residentValue.text

    val isNameValid = name.isNotBlank() && name.length >= 2
    val isBirthdateValid = birthdate.matches(Regex("^\\d{8}$"))
    val isPhoneValid = phone.matches(Regex("^01[016789]-\\d{3,4}-\\d{4}$"))
    val isResidentValid = residentNumber.matches(Regex("^\\d{6}-[\\d*]{7}$"))
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
        val digits = input.filter { it.isDigit() || it == '*' }
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
            (textBeforeHyphen.dropLast(1) + textAfterHyphen).filter { it.isDigit() || it == '*' }
        } else {
            newText.filter { it.isDigit() || it == '*' }
        }

        val formatted = formatPhone(digitsOnly)
        if (formatted.length <= 13) {
            var digitCountBeforeCursor = newText.take(newValue.selection.start).count { it.isDigit() }
            if (wasHyphenDeleted) digitCountBeforeCursor = (digitCountBeforeCursor - 1).coerceAtLeast(0)
            
            var newCursorPos = 0
            var currentDigits = 0
            for (i in formatted.indices) {
                if (currentDigits == digitCountBeforeCursor) break
                if (formatted[i].isDigit() || formatted[i] == '*') currentDigits++
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
            (textBeforeHyphen.dropLast(1) + textAfterHyphen).filter { it.isDigit() || it == '*' }
        } else {
            newText.filter { it.isDigit() || it == '*' }
        }

        val formatted = formatResident(digitsOnly)
        if (formatted.length <= 14) {
            var digitCountBeforeCursor = newText.take(newValue.selection.start).count { it.isDigit() || it == '*' }
            if (wasHyphenDeleted) digitCountBeforeCursor = (digitCountBeforeCursor - 1).coerceAtLeast(0)

            var newCursorPos = 0
            var currentDigits = 0
            for (i in formatted.indices) {
                if (currentDigits == digitCountBeforeCursor) break
                if (formatted[i].isDigit() || formatted[i] == '*') currentDigits++
                newCursorPos++
            }
            if (newCursorPos < formatted.length && formatted[newCursorPos] == '-' && !isDeletion) {
                newCursorPos++
            }

            residentValue = TextFieldValue(formatted, TextRange(newCursorPos))
            residentError = if (formatted.isNotEmpty() && !formatted.matches(Regex("^\\d{6}-[\\d*]{7}$"))) 
                "형식에 맞춰 입력해 주세요 (예: 000000-0000000)" else null
        }
    }

    LaunchedEffect(emrSearchResults, isEmrLoading) {
        if (isNew && searchAttempted && !isEmrLoading) {
            val shortBirthdate = if (birthdate.length >= 8) birthdate.substring(2, 8) else birthdate
            val match = emrSearchResults.find { it.resident_number?.startsWith(shortBirthdate) == true }
            if (match != null) {
                matchedResidentNumber = match.resident_number
                match.phone?.let {
                    val formattedPhone = formatPhone(it.filter { c -> c.isDigit() })
                    phoneValue = TextFieldValue(formattedPhone, TextRange(formattedPhone.length))
                }
            } else {
                matchedResidentNumber = null
            }
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            val scrollState = rememberScrollState()
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth()
                    .verticalScroll(scrollState),
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
                            if (isNew) searchAttempted = false
                        }
                    },
                    label = { Text("이름") },
                    isError = nameError != null,
                    supportingText = { nameError?.let { Text(it) } },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isNew || (!searchAttempted || matchedResidentNumber == null)
                )

                if (isNew) {
                    OutlinedTextField(
                        value = birthdate,
                        onValueChange = { 
                            val digits = it.filter { c -> c.isDigit() }
                            if (digits.length <= 8) {
                                birthdate = digits
                                birthdateError = if (digits.isNotEmpty() && digits.length < 8) "생년월일 8자리를 입력해 주세요 (예: 19800101)" else null
                                searchAttempted = false
                                matchedResidentNumber = null
                                manualRegistration = false
                            }
                        },
                        label = { Text("생년월일 (8자리)") },
                        leadingIcon = { Icon(Icons.Default.DateRange, contentDescription = null) },
                        isError = birthdateError != null,
                        supportingText = { birthdateError?.let { Text(it) } },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !searchAttempted || matchedResidentNumber == null
                    )

                    if (!searchAttempted || isEmrLoading || (searchAttempted && matchedResidentNumber == null && !manualRegistration)) {
                        Button(
                            onClick = { 
                                if (isNameValid && isBirthdateValid) {
                                    searchAttempted = true
                                    manualRegistration = false
                                    viewModel.searchEmrPatients(name)
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = isNameValid && isBirthdateValid && !isEmrLoading,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                        ) {
                            if (isEmrLoading) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onSecondary)
                            } else {
                                Icon(Icons.Default.Search, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("방문 기록 검색")
                            }
                        }
                    }
                }

                if (!isNew || (searchAttempted && !isEmrLoading)) {
                    if (isNew && matchedResidentNumber != null) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "방문 기록이 확인되었습니다.",
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        
                        OutlinedTextField(
                            value = phoneValue,
                            onValueChange = { onPhoneChange(it) },
                            label = { Text("전화번호") },
                            isError = phoneError != null,
                            supportingText = { phoneError?.let { Text(it) } },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else if (isNew && matchedResidentNumber == null && !manualRegistration) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "일치하는 환자 정보가 없습니다.",
                                        color = MaterialTheme.colorScheme.onErrorContainer,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "정보를 직접 입력하시겠습니까?",
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                        Button(
                            onClick = { manualRegistration = true },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.onSurfaceVariant)
                        ) {
                            Text("수기 입력")
                        }
                    } else {
                        // Manual registration or Edit mode
                        OutlinedTextField(
                            value = phoneValue,
                            onValueChange = { onPhoneChange(it) },
                            label = { Text("전화번호") },
                            isError = phoneError != null,
                            supportingText = { phoneError?.let { Text(it) } },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = residentValue,
                            onValueChange = { onResidentChange(it) },
                            label = { Text("주민등록번호") },
                            isError = residentError != null,
                            supportingText = { residentError?.let { Text(it) } },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(),
                            enabled = isNew // Can only edit resident number if adding new family member
                        )
                    }
                }

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
                    
                    val canSave = if (isNew) {
                        (matchedResidentNumber != null && isPhoneValid) || (manualRegistration && isPhoneValid && isResidentValid)
                    } else {
                        isNameValid && isPhoneValid && isResidentValid
                    }
                    
                    Button(
                        onClick = {
                            if (canSave) {
                                val finalResident = if (isNew && matchedResidentNumber != null) matchedResidentNumber!! else residentNumber
                                onConfirm(name, phone, finalResident, relationship)
                            }
                        },
                        enabled = canSave,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(if (isNew) "Register" else "Save")
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
