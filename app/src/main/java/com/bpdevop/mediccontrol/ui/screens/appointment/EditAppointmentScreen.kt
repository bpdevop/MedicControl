package com.bpdevop.mediccontrol.ui.screens.appointment

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
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
import com.bpdevop.mediccontrol.ui.components.DatePickerModal
import com.bpdevop.mediccontrol.ui.components.TimePickerModal
import com.bpdevop.mediccontrol.ui.viewmodels.AppointmentViewModel
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date

@Composable
fun EditAppointmentScreen(
    patientId: String,
    appointment: PatientAppointment,
    viewModel: AppointmentViewModel = hiltViewModel(),
    onAppointmentUpdated: () -> Unit,
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var visitType by remember { mutableStateOf(appointment.visitType) }
    var appointmentDate by remember { mutableStateOf(appointment.date) }
    var appointmentTime by remember { mutableStateOf(appointment.time) }
    var notes by remember { mutableStateOf(appointment.notes ?: "") }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var dateError by remember { mutableStateOf(false) }
    var timeError by remember { mutableStateOf(false) }

    val updateAppointmentState by viewModel.updateAppointmentState.collectAsState()

    when (updateAppointmentState) {
        is UiState.Success -> {
            LaunchedEffect(Unit) {
                onAppointmentUpdated()
                viewModel.resetUpdateAppointmentState()
            }
        }

        is UiState.Error -> {
            LaunchedEffect(Unit) {
                Toast.makeText(context, (updateAppointmentState as UiState.Error).message, Toast.LENGTH_LONG).show()
                viewModel.resetUpdateAppointmentState()
            }
        }

        else -> Unit
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Visit type selection
        Text(text = stringResource(R.string.new_appointment_visit_type))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            VisitType.entries.forEach { type ->
                FilterChip(
                    selected = visitType == type,
                    onClick = { visitType = type },
                    label = {
                        Text(
                            text = stringResource(
                                id = when (type) {
                                    VisitType.NEW -> R.string.visit_type_new
                                    VisitType.FOLLOW_UP -> R.string.visit_type_follow_up
                                    VisitType.EMERGENCY -> R.string.visit_type_emergency
                                }
                            )
                        )
                    },
                    leadingIcon = if (visitType == type) {
                        { Icon(Icons.Default.Done, contentDescription = null, modifier = Modifier.size(FilterChipDefaults.IconSize)) }
                    } else null
                )
            }
        }

        // Appointment date and time
        OutlinedTextField(
            value = appointmentDate?.formatToString() ?: "",
            onValueChange = {},
            label = { Text(stringResource(R.string.new_appointment_date)) },
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showDatePicker = true },
            enabled = false,
            isError = dateError
        )

        OutlinedTextField(
            value = appointmentTime?.formatToString("HH:mm") ?: "",
            onValueChange = {},
            label = { Text(stringResource(R.string.new_appointment_time)) },
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showTimePicker = true },
            enabled = false,
            isError = timeError
        )

        if (showDatePicker) {
            DatePickerModal(
                onDateSelected = { selectedDateMillis ->
                    selectedDateMillis?.let { appointmentDate = Date(it) }
                    showDatePicker = false
                    dateError = appointmentDate == null
                },
                onDismiss = { showDatePicker = false }
            )
        }

        if (showTimePicker) {
            TimePickerModal(
                onTimeSelected = { hour, minute ->
                    val calendar = Calendar.getInstance().apply {
                        set(Calendar.HOUR_OF_DAY, hour)
                        set(Calendar.MINUTE, minute)
                    }
                    appointmentTime = calendar.time
                    showTimePicker = false
                    timeError = appointmentTime == null
                },
                onDismiss = { showTimePicker = false }
            )
        }

        // Notes
        OutlinedTextField(
            value = notes,
            onValueChange = { notes = it },
            label = { Text(stringResource(R.string.new_appointment_notes)) },
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
            maxLines = 4
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Save Button
        Button(
            onClick = {
                coroutineScope.launch {
                    dateError = appointmentDate == null
                    timeError = appointmentTime == null

                    if (!dateError && !timeError) {
                        val updatedAppointment = appointment.copy(
                            date = appointmentDate,
                            time = appointmentTime,
                            visitType = visitType,
                            notes = notes.ifEmpty { null }
                        )
                        viewModel.updateAppointment(patientId, updatedAppointment)
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = stringResource(R.string.new_appointment_save))
        }

        if (updateAppointmentState is UiState.Loading) {
            CircularProgressIndicator(modifier = Modifier.padding(top = 16.dp))
        }
    }
}