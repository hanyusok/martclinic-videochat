package com.example.martclinic_videochat.presentation.ui

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.martclinic_videochat.presentation.viewmodel.PrescriptionViewModel
import com.example.martclinic_videochat.util.DateTimeUtil

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrescriptionDialog(
    appointmentId: String,
    onDismiss: () -> Unit,
    viewModel: PrescriptionViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val prescription by viewModel.prescription.collectAsState()
    val defaultPharmacy by viewModel.defaultPharmacy.collectAsState()
    val dispatchedPharmacy by viewModel.dispatchedPharmacy.collectAsState() // Add thi
    val isLoading by viewModel.isLoading.collectAsState()
    val dispatchSuccess by viewModel.dispatchSuccess.collectAsState()

    LaunchedEffect(appointmentId) {
        viewModel.loadPrescription(appointmentId)
    }

    LaunchedEffect(dispatchSuccess) {
        if (dispatchSuccess == true) {
            Toast.makeText(context, "약국으로 처방전이 안전하게 전송되었습니다.", Toast.LENGTH_SHORT).show()
            viewModel.resetDispatchStatus()
        } else if (dispatchSuccess == false) {
            Toast.makeText(context, "전송에 실패했습니다. 다시 시도해주세요.", Toast.LENGTH_SHORT).show()
            viewModel.resetDispatchStatus()
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .wrapContentHeight()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header Row
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "전자 처방전 조회",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "닫기")
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                if (isLoading && prescription == null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                } else if (prescription == null) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "없음",
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.outline
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "발행된 처방전 정보가 없습니다.\n진료 완료 후 처방전이 등록됩니다.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    val pres = prescription!!
                    val isSent = !pres.sent_pharmacy_id.isNullOrBlank()

                    // Prescription Info Details
                    Text(
                        text = "의사 소견 및 복약 지도",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f))
                    ) {
                        Text(
                            text = pres.doctor_notes,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(16.dp),
                            lineHeight = MaterialTheme.typography.bodyMedium.lineHeight * 1.3
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Step Progress Timeline
                    Text(
                        text = "조제 및 수령 단계",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    TimelineStep(title = "1. 비대면 진료 완료", subtitle = "의사 상담이 완료되었습니다.", isCompleted = true)
                    TimelineStep(title = "2. 처방전 전자 발행", subtitle = "온라인 처방전이 발행되었습니다.", isCompleted = true)
                    TimelineStep(title = "3. 약국 매칭 및 전송", subtitle = if (isSent) "선택하신 약국으로 전송 완료" else "약국을 선택하고 전송해주세요.", isCompleted = isSent)
                    TimelineStep(title = "4. 약국 조제 완료", subtitle = if (isSent) "조제 대기 또는 수령 대기 중" else "처방전 전송 시 조제가 시작됩니다.", isCompleted = false, isActive = isSent)

                    Spacer(modifier = Modifier.height(20.dp))

                    // Pharmacy Selection / Dispatch Area
                    if (isSent) {
                        Text(
                            text = "전송 완료된 약국",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = dispatchedPharmacy?.pharmacy_name ?: "지정된 약국",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = dispatchedPharmacy?.address ?: "",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                pres.sent_at?.let { sentTime ->
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "전송 일시: ${DateTimeUtil.formatTimestampToKst(sentTime)}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.outline
                                    )
                                }
                            }
                        }
                    } else {
                        // User has default pharmacy
                        val pharm = defaultPharmacy
                        if (pharm != null) {
                            Text(
                                text = "내 기본 수령 약국",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        text = pharm.pharmacy_name,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = pharm.address,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = { viewModel.dispatchPrescriptionToDefaultPharmacy() },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("기본 약국으로 처방전 전송하기", fontWeight = FontWeight.Bold)
                            }
                        } else {
                            // User has no default pharmacy set
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = "경고",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = "기본 수령 약국이 지정되어 있지 않습니다. 약국 탭에서 단골 약국을 먼저 지정해 주세요.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onErrorContainer
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
fun TimelineStep(
    title: String,
    subtitle: String,
    isCompleted: Boolean,
    isActive: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icon Indicator
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(
                    if (isCompleted) MaterialTheme.colorScheme.primary
                    else if (isActive) MaterialTheme.colorScheme.secondary
                    else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isCompleted) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "완료",
                    tint = Color.White,
                    modifier = Modifier.size(14.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Step Text
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isCompleted || isActive) FontWeight.Bold else FontWeight.Normal,
                color = if (isCompleted || isActive) MaterialTheme.colorScheme.onSurface
                        else MaterialTheme.colorScheme.outline
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = if (isCompleted || isActive) MaterialTheme.colorScheme.onSurfaceVariant
                        else MaterialTheme.colorScheme.outline
            )
        }
    }
}
