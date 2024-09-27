package com.bpdevop.mediccontrol.ui.screens.bloodpressure

import android.content.Context
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
fun NewBloodPressureScreen(
    patientId: String,
    viewModel: BloodPressureViewModel = hiltViewModel(),
    onBloodPressureAdded: () -> Unit,
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var systolic by remember { mutableIntStateOf(120) }
    var diastolic by remember { mutableIntStateOf(80) }
    var pulse by remember { mutableIntStateOf(70) }
    var bloodPressureDate by remember { mutableStateOf<Date?>(null) }
    var bloodPressureTime by remember { mutableStateOf<Date?>(null) }
    var notes by remember { mutableStateOf("") }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    var bloodPressureDateError by remember { mutableStateOf(false) }
    var bloodPressureTimeError by remember { mutableStateOf(false) }

    val addBloodPressureState by viewModel.addBloodPressureState.collectAsState()

    HandleUiStatesBloodPressure(addBloodPressureState, context, viewModel, onBloodPressureAdded)

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

        OutlinedTextField(
            value = bloodPressureDate?.formatToString() ?: "",
            onValueChange = {},
            label = { Text(stringResource(R.string.new_blood_pressure_date)) },
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showDatePicker = true },
            enabled = false,
            isError = bloodPressureDateError,
            supportingText = {
                if (bloodPressureDateError) {
                    Text(stringResource(R.string.new_blood_pressure_error_date_required))
                }
            }
        )

        OutlinedTextField(
            value = bloodPressureTime?.formatToString("HH:mm") ?: "",
            onValueChange = {},
            label = { Text(stringResource(R.string.new_blood_pressure_time)) },
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showTimePicker = true },
            enabled = false,
            isError = bloodPressureTimeError,
            supportingText = {
                if (bloodPressureTimeError) {
                    Text(stringResource(R.string.new_blood_pressure_error_time_required))
                }
            }
        )

        if (showDatePicker) {
            DatePickerModal(
                onDateSelected = { selectedDateMillis ->
                    selectedDateMillis?.let { bloodPressureDate = Date(it) }
                    showDatePicker = false
                    bloodPressureDateError = bloodPressureDate == null
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
                    bloodPressureTime = calendar.time
                    showTimePicker = false
                    bloodPressureTimeError = bloodPressureTime == null
                },
                onDismiss = { showTimePicker = false }
            )
        }

        OutlinedTextField(
            value = notes,
            onValueChange = { notes = it },
            label = { Text(stringResource(R.string.new_blood_pressure_notes)) },
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
            maxLines = 4,
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                coroutineScope.launch {
                    bloodPressureDateError = bloodPressureDate == null
                    bloodPressureTimeError = bloodPressureTime == null

                    if (!bloodPressureDateError && !bloodPressureTimeError) {
                        val bloodPressure = BloodPressure(
                            systolic = systolic,
                            diastolic = diastolic,
                            pulse = pulse,
                            date = bloodPressureDate,
                            time = bloodPressureTime,
                            notes = notes.ifEmpty { null }
                        )
                        viewModel.addBloodPressure(patientId, bloodPressure)
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = stringResource(R.string.new_blood_pressure_save))
        }

        if (addBloodPressureState is UiState.Loading) {
            CircularProgressIndicator(modifier = Modifier.padding(top = 16.dp))
        }
    }
}

@Composable
fun HandleUiStatesBloodPressure(
    state: UiState<String>,
    context: Context,
    viewModel: BloodPressureViewModel,
    onBloodPressureAdded: () -> Unit,
) {
    when (state) {
        is UiState.Success -> {
            LaunchedEffect(Unit) {
                onBloodPressureAdded()
                viewModel.resetAddBloodPressureState()
            }
        }

        is UiState.Error -> {
            LaunchedEffect(Unit) {
                Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
                viewModel.resetAddBloodPressureState()
            }
        }

        else -> Unit
    }
}
