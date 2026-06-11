package com.example.martclinic_videochat.presentation.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.martclinic_videochat.domain.model.Appointment
import com.example.martclinic_videochat.presentation.viewmodel.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel()
) {
    val appointments by viewModel.appointments.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Mart Clinic Home") })
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.padding(16.dp))
            }

            Button(
                onClick = { viewModel.loadAppointments("dummy-patient-id") },
                modifier = Modifier.padding(16.dp)
            ) {
                Text("Refresh Appointments")
            }

            LazyColumn {
                items(appointments) { appointment ->
                    AppointmentItem(appointment)
                }
            }
        }
    }
}

@Composable
fun AppointmentItem(appointment: Appointment) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "Status: ${appointment.status}", style = MaterialTheme.typography.titleMedium)
            Text(text = "Symptoms: ${appointment.symptoms}", style = MaterialTheme.typography.bodyMedium)
        }
    }
}
