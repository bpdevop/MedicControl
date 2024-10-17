package com.bpdevop.mediccontrol.ui.screens.patient

import android.net.Uri
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.text.HtmlCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.bpdevop.mediccontrol.R
import com.bpdevop.mediccontrol.core.extensions.formatToString
import com.bpdevop.mediccontrol.core.utils.UiState
import com.bpdevop.mediccontrol.core.utils.deleteImageFile
import com.bpdevop.mediccontrol.data.model.Disease
import com.bpdevop.mediccontrol.data.model.Patient
import com.bpdevop.mediccontrol.ui.components.DatePickerModal
import com.bpdevop.mediccontrol.ui.components.DiseaseSelectionDialog
import com.bpdevop.mediccontrol.ui.components.ImagePickerComponent
import com.bpdevop.mediccontrol.ui.viewmodels.PatientsViewModel
import java.io.File
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatientDetailScreen(
    patientId: String,
    viewModel: PatientsViewModel = hiltViewModel(),
    onPatientUpdated: () -> Unit,
    onPatientDeleted: () -> Unit,
) {
    val context = LocalContext.current
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var birthDate by remember { mutableStateOf<Date?>(null) }
    var bloodType by remember { mutableStateOf("") }
    var rhFactor by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("") }
    var doctorId by remember { mutableStateOf("") }
    var photoUrl by remember { mutableStateOf<String?>(null) }
    var displayPhotoUri by remember { mutableStateOf<Uri?>(null) }
    var photoUri by remember { mutableStateOf<Uri?>(null) }


    var photoFile: File? = null
    var isEditing by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }

    var showBloodTypeMenu by remember { mutableStateOf(false) }
    var showRHMenu by remember { mutableStateOf(false) }

    val patientDetailState by viewModel.patientDetailState.collectAsState()
    val updatePatientState by viewModel.updatePatientState.collectAsState()
    val deletePatientState by viewModel.deletePatientState.collectAsState()
    val diseaseSearchState by viewModel.diseaseSearchState.collectAsState()

    var selectedDisease by remember { mutableStateOf<Disease?>(null) }
    var showDiseaseDialog by remember { mutableStateOf(false) }


    LaunchedEffect(patientId) {
        viewModel.loadPatientDetail(patientId)
    }

    LaunchedEffect(updatePatientState) {
        if (updatePatientState is UiState.Success) {
            onPatientUpdated()
            photoFile?.let { deleteImageFile(it) }
            viewModel.resetUpdatePatientState()
        }
    }

    LaunchedEffect(deletePatientState) {
        if (deletePatientState is UiState.Success) {
            onPatientDeleted()
            viewModel.resetDeletePatientState()
        }
    }

    when (patientDetailState) {
        is UiState.Success -> {
            val patient = (patientDetailState as UiState.Success<Patient>).data

            if (!isEditing) {
                name = patient.name
                phone = patient.phone ?: ""
                email = patient.email ?: ""
                address = patient.address ?: ""
                notes = patient.notes ?: ""
                birthDate = patient.birthDate
                bloodType = patient.bloodType ?: ""
                rhFactor = if (patient.rhFactor == true) "+" else "-"
                gender = patient.gender ?: ""
                doctorId = patient.doctorId
                photoUrl = patient.photoUrl
                if (photoUri == null) {
                    displayPhotoUri = patient.photoUrl?.let { Uri.parse(it) }
                }

                if (patient.diseaseId != null && patient.diseaseTitle != null) {
                    selectedDisease = Disease(
                        id = patient.diseaseId,
                        title = patient.diseaseTitle,
                        code = patient.diseaseCode,
                        chapter = null,
                        isLeaf = true,
                        stemId = null,
                        descendants = null,
                        synonyms = emptyList()
                    )
                }
            }
        }

        is UiState.Error -> {
            Toast.makeText(context, (patientDetailState as UiState.Error).message, Toast.LENGTH_SHORT).show()
        }

        else -> Unit
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        ImagePickerComponent(
            imageUri = displayPhotoUri,
            onImagePicked = { newUri, file ->
                photoUri = newUri
                displayPhotoUri = newUri
                photoFile = file
            },
            onImageRemoved = { file ->
                file?.let { deleteImageFile(it) }
                displayPhotoUri = null
                photoUri = null
                photoFile = null
            },
            isEditing = isEditing
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text(stringResource(R.string.detail_patient_name)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            enabled = isEditing
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = birthDate?.formatToString() ?: "",
            onValueChange = {},
            enabled = false,
            label = { Text(stringResource(R.string.detail_patient_birthdate)) },
            modifier = Modifier
                .fillMaxWidth()
                .clickable { if (isEditing) showDatePicker = true }
        )

        if (showDatePicker) {
            DatePickerModal(
                onDateSelected = { selectedDateMillis ->
                    birthDate = selectedDateMillis?.let { Date(it) }
                    showDatePicker = false
                },
                onDismiss = { showDatePicker = false }
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(text = stringResource(R.string.add_patient_gender), modifier = Modifier.padding(vertical = 8.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(
                    selected = gender == "M",
                    onClick = { if (isEditing) gender = "M" },
                    enabled = isEditing
                )
                Text(text = stringResource(R.string.detail_patient_male))
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(
                    selected = gender == "F",
                    onClick = { if (isEditing) gender = "F" },
                    enabled = isEditing
                )
                Text(text = stringResource(R.string.detail_patient_female))
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.weight(0.7f)) {
                ExposedDropdownMenuBox(
                    expanded = showBloodTypeMenu,
                    onExpandedChange = { showBloodTypeMenu = it }
                ) {
                    OutlinedTextField(
                        value = bloodType,
                        onValueChange = { },
                        readOnly = true,
                        enabled = isEditing,
                        label = { Text(stringResource(R.string.add_patient_blood_type)) },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = showBloodTypeMenu)
                        },
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                        modifier = Modifier
                            .menuAnchor(type = MenuAnchorType.PrimaryEditable, enabled = true)
                    )

                    if (isEditing) {
                        ExposedDropdownMenu(
                            expanded = showBloodTypeMenu,
                            onDismissRequest = { showBloodTypeMenu = false }
                        ) {
                            val bloodTypes = listOf("A", "B", "O", "AB")
                            bloodTypes.forEach { type ->
                                DropdownMenuItem(
                                    text = { Text(type) },
                                    onClick = {
                                        bloodType = type
                                        showBloodTypeMenu = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            Column(modifier = Modifier.weight(0.3f)) {
                ExposedDropdownMenuBox(
                    expanded = showRHMenu,
                    onExpandedChange = { showRHMenu = it }
                ) {
                    OutlinedTextField(
                        value = rhFactor,
                        onValueChange = { },
                        readOnly = true,
                        enabled = isEditing,
                        label = { Text(stringResource(R.string.add_patient_rh_factor)) },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = showRHMenu)
                        },
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                        modifier = Modifier
                            .menuAnchor(type = MenuAnchorType.PrimaryEditable, enabled = true)
                    )

                    if (isEditing) {
                        ExposedDropdownMenu(
                            expanded = showRHMenu,
                            onDismissRequest = { showRHMenu = false }
                        ) {
                            listOf("+", "-").forEach { type ->
                                DropdownMenuItem(
                                    text = { Text(type) },
                                    onClick = {
                                        rhFactor = type
                                        showRHMenu = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // OutlinedTextField para teléfono
        OutlinedTextField(
            value = phone,
            onValueChange = { phone = it },
            label = { Text(stringResource(R.string.detail_patient_phone)) },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions.Default.copy(
                keyboardType = KeyboardType.Phone
            ),
            singleLine = true,
            enabled = isEditing
        )

        Spacer(modifier = Modifier.height(8.dp))

        // OutlinedTextField para correo electrónico
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text(stringResource(R.string.detail_patient_email)) },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions.Default.copy(
                keyboardType = KeyboardType.Email
            ),
            singleLine = true,
            enabled = isEditing
        )

        Spacer(modifier = Modifier.height(8.dp))

        // OutlinedTextField para dirección
        OutlinedTextField(
            value = address,
            onValueChange = { address = it },
            label = { Text(stringResource(R.string.detail_patient_address)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            enabled = isEditing
        )

        Spacer(modifier = Modifier.height(8.dp))

        // OutlinedTextField para notas
        OutlinedTextField(
            value = notes,
            onValueChange = { notes = it },
            label = { Text(stringResource(R.string.detail_patient_notes)) },
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp),
            enabled = isEditing,
            maxLines = 4
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = HtmlCompat.fromHtml(selectedDisease?.title ?: "", HtmlCompat.FROM_HTML_MODE_LEGACY).toString(),
            onValueChange = { },
            readOnly = true,
            label = { Text("Enfermedad seleccionada") },
            trailingIcon = {
                IconButton(onClick = { showDiseaseDialog = true }) {
                    Icon(Icons.Default.Search, contentDescription = null)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = isEditing
        )

        if (showDiseaseDialog) {
            DiseaseSelectionDialog(
                title = "Seleccione una enfermedad",
                diseases = diseaseSearchState,
                onDismiss = { showDiseaseDialog = false },
                onDiseaseSelected = { disease ->
                    selectedDisease = disease
                    showDiseaseDialog = false
                },
                onSearchQueryChanged = { query ->
                    viewModel.searchDiseases(query)
                }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Botón de editar/guardar
        Button(
            onClick = {
                if (isEditing) {
                    viewModel.updatePatient(
                        Patient(
                            id = patientId,
                            name = name,
                            phone = phone.ifEmpty { null },
                            email = email.ifEmpty { null },
                            address = address.ifEmpty { null },
                            notes = notes.ifEmpty { null },
                            birthDate = birthDate,
                            bloodType = bloodType.ifEmpty { null },
                            rhFactor = rhFactor.takeIf { it.isNotEmpty() }?.let { it == "+" },
                            gender = gender,
                            photoUrl = photoUrl,
                            doctorId = doctorId,
                            diseaseId = selectedDisease?.id,
                            diseaseCode = selectedDisease?.code,
                            diseaseTitle = selectedDisease?.title
                        ),
                        photoUri
                    )
                }
                isEditing = !isEditing
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = updatePatientState !is UiState.Loading
        ) {
            if (updatePatientState is UiState.Loading) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary)
            } else {
                Text(text = if (isEditing) stringResource(R.string.detail_patient_action_save) else stringResource(R.string.detail_patient_action_edit))
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = {
                viewModel.deletePatient(
                    Patient(
                        id = patientId,
                        name = name,
                        doctorId = doctorId
                    )
                )
            },
            colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.error),
            modifier = Modifier.fillMaxWidth(),
            enabled = deletePatientState !is UiState.Loading
        ) {
            if (deletePatientState is UiState.Loading) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.onError)
            } else {
                Text(text = stringResource(R.string.detail_patient_action_delete))
            }
        }
    }
}
