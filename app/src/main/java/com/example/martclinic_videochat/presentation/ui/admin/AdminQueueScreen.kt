package com.example.martclinic_videochat.presentation.ui.admin

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.martclinic_videochat.presentation.viewmodel.AdminViewModel

@Composable
fun AdminQueueScreen(
    viewModel: AdminViewModel = hiltViewModel()
) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = "Admin Queue Screen")
    }
}
