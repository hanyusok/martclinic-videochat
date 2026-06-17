package com.example.martclinic_videochat.presentation.ui

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
import com.example.martclinic_videochat.domain.model.Pharmacy
import com.example.martclinic_videochat.presentation.ui.components.PharmacyCard
import com.example.martclinic_videochat.presentation.viewmodel.PharmacyViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PharmacyScreen(
    viewModel: PharmacyViewModel = hiltViewModel()
) {
    val pharmacies by viewModel.pharmacies.collectAsState()
    val nearbyPharmacies by viewModel.nearbyPharmacies.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("약국 선택", fontWeight = FontWeight.Bold) }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("내 단골 약국") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("주변 약국 검색") }
                )
            }

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                val listToShow = if (selectedTab == 0) pharmacies else nearbyPharmacies
                
                if (listToShow.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = if (selectedTab == 0) "등록된 단골 약국이 없습니다." else "주변에 검색된 약국이 없습니다.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(listToShow) { item ->
                            val favoriteVersion = pharmacies.find { 
                                it.pharmacy_name == item.pharmacy_name && it.address == item.address 
                            }
                            val isFavorite = favoriteVersion != null
                            
                            // Use the favorite version if it exists to show the correct 'is_default' status
                            val pharmacyToDisplay = favoriteVersion ?: item

                            PharmacyCard(
                                pharmacy = pharmacyToDisplay,
                                isFavorite = isFavorite,
                                onFavoriteClick = {
                                    viewModel.toggleFavoritePharmacy(item)
                                },
                                onSetDefault = {
                                    viewModel.toggleDefaultPharmacy(pharmacyToDisplay, !pharmacyToDisplay.is_default)
                                },
                                onUpdateFax = { newFax ->
                                    viewModel.updateFaxNumber(pharmacyToDisplay, newFax)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
