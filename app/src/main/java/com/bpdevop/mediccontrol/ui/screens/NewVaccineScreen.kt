package com.bpdevop.mediccontrol.ui.screens

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.bpdevop.mediccontrol.R
import com.bpdevop.mediccontrol.core.extensions.formatToString
import com.bpdevop.mediccontrol.core.utils.UiState
import com.bpdevop.mediccontrol.data.model.Vaccine
import com.bpdevop.mediccontrol.ui.components.DatePickerModal
import com.bpdevop.mediccontrol.ui.viewmodels.VaccinationViewModel
import kotlinx.coroutines.launch
import java.util.Date

@Composable
fun NewVaccineScreen(
    patientId: String,
    viewModel: VaccinationViewModel = hiltViewModel(),
    onVaccineAdded: () -> Unit,
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var vaccineName by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var vaccineDate by remember { mutableStateOf<Date?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }

    var vaccineNameError by remember { mutableStateOf(false) }
    var vaccineDateError by remember { mutableStateOf(false) }

    val addVaccineState by viewModel.addVaccineState.collectAsState()


    HandleUiStates(addVaccineState, context, viewModel, onVaccineAdded)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OutlinedTextField(
            value = vaccineName,
            onValueChange = {
                vaccineName = it
                vaccineNameError = vaccineName.isEmpty()
            },
            label = { Text(stringResource(R.string.new_vaccine_name)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            isError = vaccineNameError,
            supportingText = {
                if (vaccineNameError) {
                    Text(stringResource(R.string.new_vaccine_error_name_required))
                }
            }
        )

        OutlinedTextField(
            value = vaccineDate?.formatToString() ?: "",
            onValueChange = {},
            label = { Text(stringResource(R.string.new_vaccine_date)) },
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showDatePicker = true },
            enabled = false,
            isError = vaccineDateError,
            supportingText = {
                if (vaccineDateError) {
                    Text(stringResource(R.string.new_vaccine_error_date_required))
                }
            }
        )

        if (showDatePicker) {
            DatePickerModal(
                onDateSelected = { selectedDateMillis ->
                    selectedDateMillis?.let { vaccineDate = Date(it) }
                    showDatePicker = false
                    vaccineDateError = vaccineDate == null
                },
                onDismiss = { showDatePicker = false }
            )
        }

        OutlinedTextField(
            value = notes,
            onValueChange = { notes = it },
            label = { Text(stringResource(R.string.new_vaccine_notes)) },
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
            maxLines = 4,
            keyboardOptions = KeyboardOptions.Default.copy(
                keyboardType = KeyboardType.Text
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                coroutineScope.launch {
                    vaccineNameError = vaccineName.isEmpty()
                    vaccineDateError = vaccineDate == null

                    if (!vaccineNameError && !vaccineDateError) {
                        coroutineScope.launch {
                            val vaccine = Vaccine(
                                name = vaccineName,
                                date = vaccineDate!!,
                                notes = notes.ifEmpty { null }
                            )
                            viewModel.addVaccine(patientId, vaccine)
                        }
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = stringResource(R.string.new_vaccine_save))
        }

        if (addVaccineState is UiState.Loading) {
            CircularProgressIndicator(modifier = Modifier.padding(top = 16.dp))
        }
    }
}


@Composable
fun HandleUiStates(
    state: UiState<String>,
    context: Context,
    viewModel: VaccinationViewModel,
    onVaccineAdded: () -> Unit,
) {
    when (state) {
        is UiState.Success -> {
            LaunchedEffect(Unit) {
                onVaccineAdded()
                viewModel.resetAddVaccineState()
            }
        }

        is UiState.Error -> {
            LaunchedEffect(Unit) {
                Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
                viewModel.resetAddVaccineState()
            }
        }

        else -> Unit
    }
}