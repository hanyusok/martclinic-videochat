package com.example.martclinic_videochat.presentation.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import com.example.martclinic_videochat.domain.model.Pharmacy

@Composable
fun PharmacyCard(
    pharmacy: Pharmacy,
    isFavorite: Boolean,
    onFavoriteClick: () -> Unit,
    onSetDefault: () -> Unit,
    onUpdateFax: (String) -> Unit
) {
    val context = LocalContext.current
    var showFaxDialog by remember { mutableStateOf(false) }
    var faxInput by remember { mutableStateOf(pharmacy.fax ?: "") }
    
    // Simple validation: 9-11 digits with optional hyphens
    val isFaxValid = remember(faxInput) {
        val digitsOnly = faxInput.filter { it.isDigit() }
        digitsOnly.length in 9..11
    }

    if (showFaxDialog) {
        AlertDialog(
            onDismissRequest = { showFaxDialog = false },
            title = { Text("팩스 번호 업데이트") },
            text = {
                Column {
                    OutlinedTextField(
                        value = faxInput,
                        onValueChange = { 
                            // Only allow digits and hyphens
                            if (it.all { char -> char.isDigit() || char == '-' }) {
                                faxInput = it 
                            }
                        },
                        label = { Text("팩스 번호") },
                        placeholder = { Text("02-123-4567") },
                        singleLine = true,
                        isError = faxInput.isNotEmpty() && !isFaxValid,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (faxInput.isNotEmpty() && !isFaxValid) {
                        Text(
                            text = "올바른 번호 형식이 아닙니다 (9-11자리)",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onUpdateFax(faxInput)
                        showFaxDialog = false
                    },
                    enabled = isFaxValid
                ) {
                    Text("저장")
                }
            },
            dismissButton = {
                TextButton(onClick = { showFaxDialog = false }) {
                    Text("취소")
                }
            }
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (pharmacy.is_default) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = pharmacy.pharmacy_name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        if (pharmacy.is_default) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                color = MaterialTheme.colorScheme.primary,
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = "대표",
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White
                                )
                            }
                        }
                    }
                    Text(
                        text = pharmacy.address,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                IconButton(onClick = onFavoriteClick) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "즐겨찾기",
                        tint = if (isFavorite) Color.Red else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable {
                            val intent = Intent(Intent.ACTION_DIAL).apply {
                                data = Uri.parse("tel:${pharmacy.phone}")
                            }
                            context.startActivity(intent)
                        }
                    ) {
                        Icon(
                            Icons.Default.Phone,
                            contentDescription = "전화 걸기",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = pharmacy.phone,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Print,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.secondary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (pharmacy.fax.isNullOrBlank()) "팩스 번호 없음" else "Fax: ${pharmacy.fax}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (pharmacy.fax.isNullOrBlank()) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f) else MaterialTheme.colorScheme.onSurface
                        )
                        IconButton(onClick = { showFaxDialog = true }, modifier = Modifier.size(24.dp)) {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = "팩스 수정",
                                modifier = Modifier.size(14.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                if (isFavorite) {
                    TextButton(onClick = onSetDefault) {
                        Text(if (pharmacy.is_default) "대표 약국 해제" else "대표 약국으로 설정")
                    }
                }
            }
        }
    }
}
