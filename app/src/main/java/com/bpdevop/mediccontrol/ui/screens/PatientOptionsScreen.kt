package com.bpdevop.mediccontrol.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.bpdevop.mediccontrol.R

@Composable
fun PatientOptionsScreen(
    patientId: String?,
    onOptionSelected: (String) -> Unit,
) {
    val options = mapOf(
        stringResource(id = R.string.patient_option_medical_history) to "medical_history",
        stringResource(id = R.string.patient_option_medical_visit) to "medical_visit",
        stringResource(id = R.string.patient_option_vaccinations) to "vaccination_screen",
        stringResource(id = R.string.patient_option_allergies) to "allergy_screen",
        stringResource(id = R.string.patient_option_blood_pressure) to "blood_pressure_screen",
        stringResource(id = R.string.patient_option_glucose) to "blood_glucose_screen",
        stringResource(id = R.string.patient_option_oxygen_saturation) to "oxygen_saturation_screen",
        stringResource(id = R.string.patient_option_tests) to "examination_screen",
        stringResource(id = R.string.patient_option_prescriptions) to "prescription_screen",
        stringResource(id = R.string.patient_option_labs) to "labs",
        stringResource(id = R.string.patient_option_radiology) to "radiology",
        stringResource(id = R.string.patient_option_pathology) to "pathology",
        stringResource(id = R.string.patient_option_surgery) to "surgery",
        stringResource(id = R.string.patient_option_notes) to "notes",
        stringResource(id = R.string.patient_option_appointments) to "appointments",
        stringResource(id = R.string.patient_option_exports) to "exports"
    )

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        options.forEach { (option, route) ->
            item {
                OptionItem(option = option, onClick = {
                    patientId?.let { id -> onOptionSelected("$route/$id") }
                })
            }
        }
    }
}

@Composable
fun OptionItem(option: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clickable(onClick = onClick)
    ) {
        Text(text = option, style = MaterialTheme.typography.bodyLarge)
    }
}