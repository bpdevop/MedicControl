package com.bpdevop.mediccontrol.ui.screens.oxygensaturation

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
import com.bpdevop.mediccontrol.data.model.OxygenSaturation
import com.bpdevop.mediccontrol.ui.components.DatePickerModal
import com.bpdevop.mediccontrol.ui.components.OxygenSaturationWheelPicker
import com.bpdevop.mediccontrol.ui.components.TimePickerModal
import com.bpdevop.mediccontrol.ui.viewmodels.OxygenSaturationViewModel
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date

@Composable
fun NewOxygenSaturationScreen(
    patientId: String,
    viewModel: OxygenSaturationViewModel = hiltViewModel(),
    onOxygenSaturationAdded: () -> Unit,
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var saturation by remember { mutableIntStateOf(98) }
    var pulse by remember { mutableIntStateOf(70) }
    var oxygenDate by remember { mutableStateOf<Date?>(null) }
    var oxygenTime by remember { mutableStateOf<Date?>(null) }
    var notes by remember { mutableStateOf("") }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    var oxygenDateError by remember { mutableStateOf(false) }
    var oxygenTimeError by remember { mutableStateOf(false) }

    val addOxygenSaturationState by viewModel.addOxygenSaturationState.collectAsState()

    HandleUiStatesOxygenSaturation(addOxygenSaturationState, context, viewModel, onOxygenSaturationAdded)

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
            Text(text = stringResource(R.string.new_oxygen_saturation_value))
            Text(text = stringResource(R.string.new_oxygen_saturation_pulse))
        }
        OxygenSaturationWheelPicker(
            saturationValue = saturation,
            pulseValue = pulse,
            onSnappedOxygenSaturation = { newSaturation, newPulse ->
                saturation = newSaturation
                pulse = newPulse
            }
        )

        OutlinedTextField(
            value = oxygenDate?.formatToString() ?: "",
            onValueChange = {},
            label = { Text(stringResource(R.string.new_oxygen_saturation_date)) },
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showDatePicker = true },
            enabled = false,
            isError = oxygenDateError,
            supportingText = {
                if (oxygenDateError) {
                    Text(stringResource(R.string.new_oxygen_saturation_error_date_required))
                }
            }
        )

        OutlinedTextField(
            value = oxygenTime?.formatToString("HH:mm") ?: "",
            onValueChange = {},
            label = { Text(stringResource(R.string.new_oxygen_saturation_time)) },
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showTimePicker = true },
            enabled = false,
            isError = oxygenTimeError,
            supportingText = {
                if (oxygenTimeError) {
                    Text(stringResource(R.string.new_oxygen_saturation_error_time_required))
                }
            }
        )

        if (showDatePicker) {
            DatePickerModal(
                onDateSelected = { selectedDateMillis ->
                    selectedDateMillis?.let { oxygenDate = Date(it) }
                    showDatePicker = false
                    oxygenDateError = oxygenDate == null
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
                    oxygenTime = calendar.time
                    showTimePicker = false
                    oxygenTimeError = oxygenTime == null
                },
                onDismiss = { showTimePicker = false }
            )
        }

        OutlinedTextField(
            value = notes,
            onValueChange = { notes = it },
            label = { Text(stringResource(R.string.new_oxygen_saturation_notes)) },
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
            maxLines = 4,
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                coroutineScope.launch {
                    oxygenDateError = oxygenDate == null
                    oxygenTimeError = oxygenTime == null

                    if (!oxygenDateError && !oxygenTimeError) {
                        val oxygenSaturation = OxygenSaturation(
                            saturation = saturation,
                            pulse = pulse,
                            date = oxygenDate,
                            time = oxygenTime,
                            notes = notes.ifEmpty { null }
                        )
                        viewModel.addOxygenSaturation(patientId, oxygenSaturation)
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = stringResource(R.string.new_oxygen_saturation_save))
        }

        if (addOxygenSaturationState is UiState.Loading) {
            CircularProgressIndicator(modifier = Modifier.padding(top = 16.dp))
        }
    }
}

@Composable
fun HandleUiStatesOxygenSaturation(
    state: UiState<String>,
    context: Context,
    viewModel: OxygenSaturationViewModel,
    onOxygenSaturationAdded: () -> Unit,
) {
    when (state) {
        is UiState.Success -> {
            LaunchedEffect(Unit) {
                onOxygenSaturationAdded()
                viewModel.resetAddOxygenSaturationState()
            }
        }

        is UiState.Error -> {
            LaunchedEffect(Unit) {
                Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
                viewModel.resetAddOxygenSaturationState()
            }
        }

        else -> Unit
    }
}
