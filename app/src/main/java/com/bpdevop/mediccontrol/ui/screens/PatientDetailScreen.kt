package com.bpdevop.mediccontrol.ui.screens

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import com.bpdevop.mediccontrol.R
import com.bpdevop.mediccontrol.core.utils.UiState
import com.bpdevop.mediccontrol.data.model.Patient
import com.bpdevop.mediccontrol.ui.viewmodels.PatientsViewModel

@Composable
fun PatientDetailScreen(
    viewModel: PatientsViewModel = hiltViewModel(),
    patientId: String,
) {
    val patientDetailState by viewModel.patientDetailState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadPatientDetail(patientId)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.Start // Alineación a la izquierda
    ) {
        when (val state = patientDetailState) {
            is UiState.Loading -> {
                CircularProgressIndicator(color = Color.Blue)
                Text(text = stringResource(R.string.patient_detail_loading))
            }

            is UiState.Success -> {
                val patient = state.data
                PatientDetailContent(patient)
            }

            is UiState.Error -> {
                Text(text = state.message)
            }

            else -> {
                Text(text = stringResource(R.string.patient_detail_default_message))
            }
        }
    }
}

@Composable
fun PatientDetailContent(patient: Patient) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.Start // Alineación a la izquierda
    ) {
        // Foto del paciente
        Box(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(16.dp)
        ) {
            Image(
                painter = rememberAsyncImagePainter(patient.photoUrl ?: R.drawable.ic_person_placeholder),
                contentDescription = stringResource(R.string.patient_detail_photo_desc),
                modifier = Modifier
                    .size(128.dp)
                    .clip(CircleShape)
            )
        }

        // Nombre
        OutlinedTextField(
            value = patient.name,
            onValueChange = {},
            label = { Text(stringResource(R.string.patient_detail_name)) },
            enabled = false,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        // Fecha de nacimiento
        OutlinedTextField(
            value = patient.birthDate?.toString() ?: "",
            onValueChange = {},
            label = { Text(stringResource(R.string.patient_detail_birthdate)) },
            enabled = false,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        // Género
        Text(text = stringResource(R.string.patient_detail_gender), modifier = Modifier.padding(vertical = 8.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(
                    selected = patient.gender == "M",
                    onClick = {},
                    enabled = false // Deshabilitar RadioButton pero mostrar el seleccionado
                )
                Text(text = stringResource(R.string.patient_detail_male))
            }
            Spacer(modifier = Modifier.width(16.dp)) // Separador entre botones
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(
                    selected = patient.gender == "F",
                    onClick = {},
                    enabled = false // Deshabilitar RadioButton pero mostrar el seleccionado
                )
                Text(text = stringResource(R.string.patient_detail_female))
            }
        }

        // Tipo de sangre
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.weight(0.7f)) {
                OutlinedTextField(
                    value = patient.bloodType ?: "",
                    onValueChange = {},
                    label = { Text(stringResource(R.string.patient_detail_blood_type)) },
                    enabled = false,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            Column(modifier = Modifier.weight(0.3f)) {
                OutlinedTextField(
                    value = if (patient.rhFactor == true) "+" else "-",
                    onValueChange = {},
                    label = { Text(stringResource(R.string.patient_detail_rh_factor)) },
                    enabled = false,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // Número de teléfono
        OutlinedTextField(
            value = patient.phone ?: "",
            onValueChange = {},
            label = { Text(stringResource(R.string.patient_detail_phone)) },
            enabled = false,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions.Default.copy(
                keyboardType = KeyboardType.Phone
            )
        )

        // Dirección
        OutlinedTextField(
            value = patient.address ?: "",
            onValueChange = {},
            label = { Text(stringResource(R.string.patient_detail_address)) },
            enabled = false,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        // Correo electrónico
        OutlinedTextField(
            value = patient.email ?: "",
            onValueChange = {},
            label = { Text(stringResource(R.string.patient_detail_email)) },
            enabled = false,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions.Default.copy(
                keyboardType = KeyboardType.Email
            )
        )

        // Notas
        OutlinedTextField(
            value = patient.notes ?: "",
            onValueChange = {},
            label = { Text(stringResource(R.string.patient_detail_notes)) },
            enabled = false,
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp),
            maxLines = 4
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { /* Acción de edición */ },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = stringResource(R.string.patient_detail_edit_action))
        }
    }
}