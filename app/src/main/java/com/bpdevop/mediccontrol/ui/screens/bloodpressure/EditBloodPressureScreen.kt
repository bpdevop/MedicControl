package com.bpdevop.mediccontrol.ui.screens.bloodpressure

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.bpdevop.mediccontrol.data.model.BloodPressure
import com.bpdevop.mediccontrol.ui.components.BloodPressureWheelPicker
import com.bpdevop.mediccontrol.ui.components.DatePickerModal
import com.bpdevop.mediccontrol.ui.components.TimePickerModal
import com.bpdevop.mediccontrol.ui.viewmodels.BloodPressureViewModel
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date

@Composable
fun EditBloodPressureScreen(
    patientId: String,
    bloodPressure: BloodPressure,
    viewModel: BloodPressureViewModel = hiltViewModel(),
    onBloodPressureUpdated: () -> Unit,
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var systolic by remember { mutableIntStateOf(bloodPressure.systolic) }
    var diastolic by remember { mutableIntStateOf(bloodPressure.diastolic) }
    var pulse by remember { mutableIntStateOf(bloodPressure.pulse) }
    var bloodPressureDate by remember { mutableStateOf(bloodPressure.date) }
    var bloodPressureTime by remember { mutableStateOf(bloodPressure.time) }
    var notes by remember { mutableStateOf(bloodPressure.notes ?: "") }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    val editBloodPressureState by viewModel.updateBloodPressureState.collectAsState()

    // Manejar estados de éxito o error
    when (editBloodPressureState) {
        is UiState.Success -> {
            LaunchedEffect(Unit) {
                onBloodPressureUpdated()
                viewModel.resetUpdateBloodPressureState()
            }
        }

        is UiState.Error -> {
            LaunchedEffect(Unit) {
                Toast.makeText(context, (editBloodPressureState as UiState.Error).message, Toast.LENGTH_LONG).show()
                viewModel.resetUpdateBloodPressureState()
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
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Text(text = stringResource(R.string.new_blood_pressure_systolic))
            Text(text = stringResource(R.string.new_blood_pressure_diastolic))
            Text(text = stringResource(R.string.new_blood_pressure_pulse))
        }
        BloodPressureWheelPicker(
            systolicValue = systolic,
            diastolicValue = diastolic,
            pulseValue = pulse,
            onSnappedBloodPressure = { newSystolic, newDiastolic, newPulse ->
                systolic = newSystolic
                diastolic = newDiastolic
                pulse = newPulse
            }
        )

        // Fecha
        OutlinedTextField(
            value = bloodPressureDate?.formatToString() ?: "",
            onValueChange = {},
            label = { Text(stringResource(R.string.new_blood_pressure_date)) },
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showDatePicker = true },
            enabled = false
        )

        // Hora
        OutlinedTextField(
            value = bloodPressureTime?.formatToString("HH:mm") ?: "",
            onValueChange = {},
            label = { Text(stringResource(R.string.new_blood_pressure_time)) },
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showTimePicker = true },
            enabled = false
        )

        // Modal para seleccionar fecha
        if (showDatePicker) {
            DatePickerModal(
                onDateSelected = { selectedDateMillis ->
                    selectedDateMillis?.let { bloodPressureDate = Date(it) }
                    showDatePicker = false
                },
                onDismiss = { showDatePicker = false }
            )
        }

        // Modal para seleccionar hora
        if (showTimePicker) {
            TimePickerModal(
                onTimeSelected = { hour, minute ->
                    val calendar = Calendar.getInstance().apply {
                        set(Calendar.HOUR_OF_DAY, hour)
                        set(Calendar.MINUTE, minute)
                    }
                    bloodPressureTime = calendar.time
                    showTimePicker = false
                },
                onDismiss = { showTimePicker = false }
            )
        }

        // Campo de notas
        OutlinedTextField(
            value = notes,
            onValueChange = { notes = it },
            label = { Text(stringResource(R.string.new_blood_pressure_notes)) },
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
            maxLines = 4
        )

        // Botón para guardar
        Button(
            onClick = {
                coroutineScope.launch {
                    val updatedBloodPressure = bloodPressure.copy(
                        systolic = systolic,
                        diastolic = diastolic,
                        pulse = pulse,
                        date = bloodPressureDate,
                        time = bloodPressureTime,
                        notes = notes.ifEmpty { null }
                    )
                    viewModel.updateBloodPressure(patientId, updatedBloodPressure)
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = stringResource(R.string.new_blood_pressure_save))
        }

        // Indicador de carga
        if (editBloodPressureState is UiState.Loading) {
            CircularProgressIndicator(modifier = Modifier.padding(top = 16.dp))
        }
    }
}
