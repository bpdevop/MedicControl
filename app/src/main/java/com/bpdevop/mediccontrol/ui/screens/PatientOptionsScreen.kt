package com.bpdevop.mediccontrol.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.EventNote
import androidx.compose.material.icons.automirrored.filled.Note
import androidx.compose.material.icons.filled.Biotech
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Vaccines
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.bpdevop.mediccontrol.R
import com.bpdevop.mediccontrol.ui.icons.Allergies
import com.bpdevop.mediccontrol.ui.icons.BloodPressure
import com.bpdevop.mediccontrol.ui.icons.Glucose
import com.bpdevop.mediccontrol.ui.icons.Labs
import com.bpdevop.mediccontrol.ui.icons.MedicalInformation
import com.bpdevop.mediccontrol.ui.icons.OxygenSaturation
import com.bpdevop.mediccontrol.ui.icons.Radiology
import com.bpdevop.mediccontrol.ui.icons.StethoscopeCheck

@Composable
fun PatientOptionsScreen(
    patientId: String?,
    onOptionSelected: (String) -> Unit,
) {
    val options = mapOf(
        stringResource(id = R.string.patient_option_medical_history) to Pair(Icons.Filled.History, "medical_history"),
        stringResource(id = R.string.patient_option_medical_visit) to Pair(StethoscopeCheck, "medical_visit"),
        stringResource(id = R.string.patient_option_vaccinations) to Pair(Icons.Filled.Vaccines, "vaccination_screen"),
        stringResource(id = R.string.patient_option_allergies) to Pair(Allergies, "allergy_screen"),
        stringResource(id = R.string.patient_option_blood_pressure) to Pair(BloodPressure, "blood_pressure_screen"),
        stringResource(id = R.string.patient_option_glucose) to Pair(Glucose, "blood_glucose_screen"),
        stringResource(id = R.string.patient_option_oxygen_saturation) to Pair(OxygenSaturation, "oxygen_saturation_screen"),
        stringResource(id = R.string.patient_option_tests) to Pair(MedicalInformation, "examination_screen"),
        stringResource(id = R.string.patient_option_prescriptions) to Pair(Icons.Filled.Receipt, "prescription_screen"),
        stringResource(id = R.string.patient_option_labs) to Pair(Labs, "laboratory_screen"),
        stringResource(id = R.string.patient_option_radiology) to Pair(Radiology, "radiology_screen"),
        stringResource(id = R.string.patient_option_pathology) to Pair(Icons.Filled.Biotech, "pathology"),
        stringResource(id = R.string.patient_option_surgery) to Pair(Icons.Filled.LocalHospital, "surgery"),
        stringResource(id = R.string.patient_option_notes) to Pair(Icons.AutoMirrored.Filled.Note, "notes"),
        stringResource(id = R.string.patient_option_appointments) to Pair(Icons.AutoMirrored.Filled.EventNote, "appointment_screen"),
    )

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        options.forEach { (option, pair) ->
            val icon = pair.first
            val route = pair.second
            item {
                OptionItem(option = option, icon = icon, onClick = {
                    patientId?.let { id -> onOptionSelected("$route/$id") }
                })
            }
        }
    }
}

@Composable
private fun OptionItem(option: String, icon: ImageVector, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 16.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(imageVector = icon, contentDescription = null)
        Text(text = option, style = MaterialTheme.typography.bodyLarge)
    }
}