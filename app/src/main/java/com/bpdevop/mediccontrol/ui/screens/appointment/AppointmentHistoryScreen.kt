package com.bpdevop.mediccontrol.ui.screens.appointment

import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.bpdevop.mediccontrol.R
import com.bpdevop.mediccontrol.core.extensions.formatToString
import com.bpdevop.mediccontrol.core.utils.UiState
import com.bpdevop.mediccontrol.data.model.PatientAppointment
import com.bpdevop.mediccontrol.data.model.VisitType
import com.bpdevop.mediccontrol.ui.components.DateHeader
import com.bpdevop.mediccontrol.ui.components.MessageDialog
import com.bpdevop.mediccontrol.ui.components.MoreOptionsMenu
import com.bpdevop.mediccontrol.ui.viewmodels.AppointmentViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppointmentHistoryScreen(
    patientId: String,
    viewModel: AppointmentViewModel = hiltViewModel(),
    onEditAppointment: (PatientAppointment) -> Unit,
) {
    val appointmentHistoryState by viewModel.patientAppointmentHistoryState.collectAsState()
    val deleteAppointmentState by viewModel.deleteAppointmentState.collectAsState()
    val isRefreshing = appointmentHistoryState is UiState.Loading
    val coroutineScope = rememberCoroutineScope()

    val context = LocalContext.current

    var appointmentToDelete by remember { mutableStateOf<PatientAppointment?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.getPatientAppointmentHistory(patientId)
    }

    when (deleteAppointmentState) {
        is UiState.Success -> {
            LaunchedEffect(Unit) {
                viewModel.resetDeleteAppointmentState()
                viewModel.getPatientAppointmentHistory(patientId)
            }
        }

        is UiState.Error -> {
            LaunchedEffect(Unit) {
                Toast.makeText(context, (deleteAppointmentState as UiState.Error).message, Toast.LENGTH_SHORT).show()
            }
        }

        else -> Unit
    }

    if (showDeleteDialog && appointmentToDelete != null) {
        MessageDialog(
            title = stringResource(R.string.dialog_delete_appointment_title),
            message = stringResource(R.string.dialog_delete_appointment_message),
            dismissButtonText = stringResource(id = android.R.string.cancel),
            onConfirm = {
                viewModel.deleteAppointment(patientId, appointmentToDelete!!.id)
                showDeleteDialog = false
            },
            onDismiss = { showDeleteDialog = false }
        )
    }

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = {
            coroutineScope.launch { viewModel.getPatientAppointmentHistory(patientId) }
        },
        modifier = Modifier.fillMaxSize()
    ) {
        when (val state = appointmentHistoryState) {
            is UiState.Success -> {
                val appointments = state.data
                if (appointments.isEmpty()) {
                    EmptyAppointmentHistoryScreen()
                } else {
                    AppointmentHistoryList(
                        appointments = appointments,
                        onEditAppointment = onEditAppointment,
                        onDeleteAppointment = { appointment ->
                            appointmentToDelete = appointment
                            showDeleteDialog = true
                        }
                    )
                }
            }

            is UiState.Error -> {
                ErrorAppointmentHistoryScreen(state.message)
            }

            else -> Unit
        }
    }
}

@Composable
fun EmptyAppointmentHistoryScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stringResource(R.string.appointment_history_empty),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

@Composable
fun ErrorAppointmentHistoryScreen(message: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AppointmentHistoryList(
    appointments: List<PatientAppointment>,
    onEditAppointment: (PatientAppointment) -> Unit,
    onDeleteAppointment: (PatientAppointment) -> Unit,
) {
    val groupedAppointments = appointments
        .sortedByDescending { it.date }
        .groupBy { it.date?.formatToString("dd/MM/yyyy") ?: "" }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        groupedAppointments.forEach { (date, appointmentsForDate) ->
            stickyHeader {
                DateHeader(date)
            }
            items(appointmentsForDate) { appointment ->
                AppointmentHistoryItem(
                    appointment = appointment,
                    onEditAppointment = onEditAppointment,
                    onDeleteAppointment = onDeleteAppointment
                )
            }
        }
    }
}

@Composable
fun AppointmentHistoryItem(
    appointment: PatientAppointment,
    onEditAppointment: (PatientAppointment) -> Unit,
    onDeleteAppointment: (PatientAppointment) -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp, 8.dp)
    ) {
        Column {
            // Tipo de visita y hora en la misma fila, con etiquetas
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = stringResource(R.string.appointment_history_visit_type),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = when (appointment.visitType) {
                            VisitType.NEW -> stringResource(R.string.appointment_type_new)
                            VisitType.FOLLOW_UP -> stringResource(R.string.appointment_type_follow_up)
                            VisitType.EMERGENCY -> stringResource(R.string.appointment_type_emergency)
                        },
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                // Columna para la etiqueta de "Hora" y el tiempo de la cita
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = stringResource(R.string.appointment_history_time),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = appointment.time?.formatToString("HH:mm") ?: stringResource(R.string.no_time_available),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                    )
                }

                MoreOptionsMenu(
                    onEditClick = { onEditAppointment(appointment) },
                    onDeleteClick = { onDeleteAppointment(appointment) },
                    modifier = Modifier.align(Alignment.Top)
                )
            }

            // Notas en una segunda fila
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.appointment_history_notes),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = appointment.notes ?: stringResource(R.string.no_notes_available),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
            )
        }
    }
}