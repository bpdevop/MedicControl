package com.bpdevop.mediccontrol.ui.screens

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
fun EditVaccineScreen(
    patientId: String,
    vaccine: Vaccine,
    viewModel: VaccinationViewModel = hiltViewModel(),
    onVaccineUpdated: () -> Unit,
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var vaccineName by remember { mutableStateOf(vaccine.name) }
    var notes by remember { mutableStateOf(vaccine.notes ?: "") }
    var vaccineDate by remember { mutableStateOf(vaccine.date) }
    var showDatePicker by remember { mutableStateOf(false) }

    val editVaccineState by viewModel.updateVaccineState.collectAsState()

    when (editVaccineState) {
        is UiState.Success -> {
            LaunchedEffect(Unit) {
                onVaccineUpdated()
                viewModel.resetUpdateVaccineState()
            }
        }

        is UiState.Error -> {
            LaunchedEffect(Unit) {
                Toast.makeText(context, (editVaccineState as UiState.Error).message, Toast.LENGTH_LONG).show()
                viewModel.resetUpdateVaccineState()
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
        OutlinedTextField(
            value = vaccineName,
            onValueChange = { vaccineName = it },
            label = { Text(stringResource(R.string.new_vaccine_name)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        OutlinedTextField(
            value = vaccineDate?.formatToString() ?: "",
            onValueChange = {},
            label = { Text(stringResource(R.string.new_vaccine_date)) },
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showDatePicker = true },
            enabled = false
        )

        if (showDatePicker) {
            DatePickerModal(
                onDateSelected = { selectedDateMillis ->
                    selectedDateMillis?.let { vaccineDate = Date(it) }
                    showDatePicker = false
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

        Button(
            onClick = {
                coroutineScope.launch {
                    val updatedVaccine = vaccine.copy(
                        name = vaccineName,
                        date = vaccineDate!!,
                        notes = notes.ifEmpty { null }
                    )
                    viewModel.editVaccine(patientId, updatedVaccine)
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = stringResource(R.string.new_vaccine_save))
        }

        if (editVaccineState is UiState.Loading) {
            CircularProgressIndicator(modifier = Modifier.padding(top = 16.dp))
        }
    }
}
