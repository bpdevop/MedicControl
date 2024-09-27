package com.bpdevop.mediccontrol.ui.screens.bloodglucose

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.bpdevop.mediccontrol.R
import com.bpdevop.mediccontrol.core.extensions.formatToString
import com.bpdevop.mediccontrol.core.utils.UiState
import com.bpdevop.mediccontrol.data.model.BloodGlucose
import com.bpdevop.mediccontrol.data.model.BloodGlucoseType
import com.bpdevop.mediccontrol.ui.components.DatePickerModal
import com.bpdevop.mediccontrol.ui.components.TimePickerModal
import com.bpdevop.mediccontrol.ui.viewmodels.BloodGlucoseViewModel
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date

@Composable
fun NewBloodGlucoseScreen(
    patientId: String,
    viewModel: BloodGlucoseViewModel = hiltViewModel(),
    onBloodGlucoseAdded: () -> Unit,
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var glucoseType by remember { mutableStateOf(BloodGlucoseType.RANDOM) }
    var glucoseResult by remember { mutableIntStateOf(100) }
    var glucoseDate by remember { mutableStateOf<Date?>(null) }
    var glucoseTime by remember { mutableStateOf<Date?>(null) }
    var notes by remember { mutableStateOf("") }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    var glucoseDateError by remember { mutableStateOf(false) }
    var glucoseTimeError by remember { mutableStateOf(false) }

    var unit by remember { mutableStateOf("mg/dL") }

    val addGlucoseState by viewModel.addBloodGlucoseState.collectAsState()

    HandleUiStatesGlucose(addGlucoseState, context, viewModel, onBloodGlucoseAdded)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Selector de tipo de glicemia con FilterChips
        Text(text = stringResource(R.string.new_blood_glucose_type))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            BloodGlucoseType.entries.forEach { type ->
                FilterChip(
                    selected = glucoseType == type,
                    onClick = { glucoseType = type },
                    label = {
                        Text(
                            text = stringResource(
                                id = when (type) {
                                    BloodGlucoseType.FASTING -> R.string.blood_glucose_type_fasting
                                    BloodGlucoseType.POSTPRANDIAL -> R.string.blood_glucose_type_postprandial
                                    BloodGlucoseType.RANDOM -> R.string.blood_glucose_type_random
                                }
                            )
                        )
                    },
                    leadingIcon = if (glucoseType == type) {
                        { Icon(Icons.Default.Done, contentDescription = null, modifier = Modifier.size(FilterChipDefaults.IconSize)) }
                    } else null
                )
            }
        }

        // Resultado de glicemia y unidad
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = glucoseResult.toString(),
                onValueChange = { glucoseResult = it.toIntOrNull() ?: 0 },
                label = { Text(stringResource(R.string.new_blood_glucose_result)) },
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions.Default.copy(
                    keyboardType = KeyboardType.Decimal
                )
            )

            Spacer(modifier = Modifier.width(8.dp))

            // Selector de unidad de medida
            var expanded by remember { mutableStateOf(false) }
            Box {
                Button(onClick = { expanded = !expanded }) {
                    Text(text = unit)
                }
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("mg/dL") },
                        onClick = {
                            unit = "mg/dL"
                            expanded = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("mmol/L") },
                        onClick = {
                            unit = "mmol/L"
                            expanded = false
                        }
                    )
                }
            }
        }

        // Fecha y hora
        OutlinedTextField(
            value = glucoseDate?.formatToString() ?: "",
            onValueChange = {},
            label = { Text(stringResource(R.string.new_blood_glucose_date)) },
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showDatePicker = true },
            enabled = false,
            isError = glucoseDateError
        )

        OutlinedTextField(
            value = glucoseTime?.formatToString("HH:mm") ?: "",
            onValueChange = {},
            label = { Text(stringResource(R.string.new_blood_glucose_time)) },
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showTimePicker = true },
            enabled = false,
            isError = glucoseTimeError
        )

        if (showDatePicker) {
            DatePickerModal(
                onDateSelected = { selectedDateMillis ->
                    selectedDateMillis?.let { glucoseDate = Date(it) }
                    showDatePicker = false
                    glucoseDateError = glucoseDate == null
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
                    glucoseTime = calendar.time
                    showTimePicker = false
                    glucoseTimeError = glucoseTime == null
                },
                onDismiss = { showTimePicker = false }
            )
        }

        // Notas
        OutlinedTextField(
            value = notes,
            onValueChange = { notes = it },
            label = { Text(stringResource(R.string.new_blood_glucose_notes)) },
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
            maxLines = 4
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                coroutineScope.launch {
                    glucoseDateError = glucoseDate == null
                    glucoseTimeError = glucoseTime == null

                    if (!glucoseDateError && !glucoseTimeError) {
                        val bloodGlucose = BloodGlucose(
                            type = glucoseType,
                            result = glucoseResult.toFloat(),
                            date = glucoseDate,
                            time = glucoseTime,
                            notes = notes.ifEmpty { null },
                            unit = unit
                        )
                        viewModel.addBloodGlucose(patientId, bloodGlucose)
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = stringResource(R.string.new_blood_glucose_save))
        }

        if (addGlucoseState is UiState.Loading) {
            CircularProgressIndicator(modifier = Modifier.padding(top = 16.dp))
        }
    }
}


@Composable
fun HandleUiStatesGlucose(
    state: UiState<String>,
    context: Context,
    viewModel: BloodGlucoseViewModel,
    onBloodGlucoseAdded: () -> Unit,
) {
    when (state) {
        is UiState.Success -> {
            LaunchedEffect(Unit) {
                onBloodGlucoseAdded()
                viewModel.resetAddBloodGlucoseState()
            }
        }

        is UiState.Error -> {
            LaunchedEffect(Unit) {
                Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
                viewModel.resetAddBloodGlucoseState()
            }
        }

        else -> Unit
    }
}
