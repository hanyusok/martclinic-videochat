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
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.martclinic_videochat.domain.model.Patient
import com.example.martclinic_videochat.presentation.viewmodel.MyPageViewModel
import com.example.martclinic_videochat.util.DateTimeUtil
import io.github.jan.supabase.auth.status.SessionStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyPageScreen(
    viewModel: MyPageViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val patient by viewModel.patient.collectAsState()
    val appointments by viewModel.appointments.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val sessionStatus by viewModel.sessionStatus.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("마이페이지", fontWeight = FontWeight.Bold) },
                actions = {
                    if (sessionStatus is SessionStatus.Authenticated) {
                        IconButton(onClick = { viewModel.signOut() }) {
                            Icon(
                                imageVector = Icons.Default.ExitToApp,
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
                            patient = patient!!,
                            appointments = appointments,
                            isLoading = isLoading,
                            viewModel = viewModel, // Pass existing viewModel
                            onUpdateProfile = { name, phone, resident ->
                                viewModel.updatePatientProfile(
                                    nameInput = name,
                                    phoneInput = phone,
                                    residentInput = resident,
                                    onSuccess = {
                                        Toast.makeText(context, "회원 정보가 수정되었습니다.", Toast.LENGTH_SHORT).show()
                                    },
                                    onError = { error ->
                                        Toast.makeText(context, error, Toast.LENGTH_LONG).show()
                                    }
                                )
                            }
                        )
                    } else if (!isLoading) {
                        ProfileRegistrationForm(
                            viewModel = viewModel, // Pass existing viewModel
                            onSubmit = { name, phone, resident ->
                                viewModel.createPatientProfile(
                                    nameInput = name,
                                    phoneInput = phone,
                                    residentInput = resident,
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
    patient: Patient,
    appointments: List<com.example.martclinic_videochat.domain.model.Appointment>,
    isLoading: Boolean,
    onUpdateProfile: (String, String, String) -> Unit,
    viewModel: MyPageViewModel // ViewModel passed from parent
) {
    var showEditDialog by remember { mutableStateOf(false) }
    var showEmrSearchDialog by remember { mutableStateOf(false) }
    
    val context = LocalContext.current
    val emrSearchResults by viewModel.emrSearchResults.collectAsState()
    val isEmrLoading by viewModel.isEmrLoading.collectAsState()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Patient Profile Card Section
        item {
            PatientProfileCard(
                patient = patient,
                onEditClick = { showEditDialog = true }
            )
        }

        // EMR Search Row outside the card/dialog
        item {
            Button(
                onClick = {
                    viewModel.searchEmrPatients(patient.name)
                    showEmrSearchDialog = true
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
            ) {
                Icon(Icons.Default.Search, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("EMR 환자 데이터 검색 (본인인증)")
            }
        }

        // Section Title: History
        item {
            Text(
                text = "과거 진료 내역",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        if (appointments.isEmpty() && !isLoading) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Text(
                        text = "진료 내역이 없습니다.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        } else {
            items(appointments) { appointment ->
                val formattedTime = DateTimeUtil.formatTimestampToKst(appointment.created_at)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "상태: ${appointment.status.uppercase()}",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (appointment.status == "completed") MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.secondary
                            )
                            
                            appointment.payment_amount?.let { amount ->
                                Text(
                                    text = "${String.format("%,d", amount)}원",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = "증상: ${appointment.symptoms}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        
                        if (formattedTime.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "진료 일시: $formattedTime",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }

    if (showEditDialog) {
        PatientProfileEditDialog(
            patient = patient,
            onDismiss = { showEditDialog = false },
            onConfirm = { name, phone, resident ->
                onUpdateProfile(name, phone, resident)
                showEditDialog = false
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
    onEditClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${patient.name} 님",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                IconButton(onClick = onEditClick) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "정보 수정",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
            
            patient.clinic_patient_number?.let {
                Text(
                    text = "환자 번호: $it",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f))
            Spacer(modifier = Modifier.height(16.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Phone,
                    contentDescription = "전화번호",
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = patient.phone,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.AccountBox,
                    contentDescription = "주민등록번호",
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(modifier = Modifier.width(8.dp))
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
                    text = "주민번호: $maskedResident",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

@Composable
fun ProfileRegistrationForm(
    onSubmit: (String, String, String) -> Unit,
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

        Button(
            onClick = {
                if (isNameValid && isPhoneValid && isResidentValid) {
                    onSubmit(name, phone, residentNumber)
                }
            },
            enabled = isNameValid && isPhoneValid && isResidentValid,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text("정보 등록 및 완료", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
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
fun PatientProfileEditDialog(
    patient: Patient,
    onDismiss: () -> Unit,
    onConfirm: (String, String, String) -> Unit
) {
    var name by remember { mutableStateOf(patient.name) }
    var phoneValue by remember { mutableStateOf(TextFieldValue(patient.phone, TextRange(patient.phone.length))) }
    var residentValue by remember { mutableStateOf(TextFieldValue(patient.resident_number, TextRange(patient.resident_number.length))) }

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
                Text(
                    text = "내 정보 수정",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
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
                    isError = nameError != null,
                    supportingText = { nameError?.let { Text(it) } },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

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
                                onConfirm(name, phone, residentNumber)
                            }
                        },
                        enabled = isNameValid && isPhoneValid && isResidentValid,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Edit (Save)")
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
            TabRow(
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
