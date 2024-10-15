package com.bpdevop.mediccontrol.ui.screens

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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.Color
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
fun AddPatientScreen(
    viewModel: PatientsViewModel = hiltViewModel(),
    onPatientAdded: () -> Unit,
) {
    val context = LocalContext.current

    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var birthDate by remember { mutableStateOf<Date?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }
    var bloodType by remember { mutableStateOf("") }
    var rhFactor by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("") }
    var photoUri by remember { mutableStateOf<Uri?>(null) }
    var photoFile: File? = null

    var selectedDisease by remember { mutableStateOf<Disease?>(null) }
    var showDiseaseDialog by remember { mutableStateOf(false) }

    var showBloodTypeMenu by remember { mutableStateOf(false) }
    var showRHMenu by remember { mutableStateOf(false) }
    var loading by remember { mutableStateOf(false) }

    val addPatientState by viewModel.addPatientState.collectAsState()
    val diseaseSearchState by viewModel.diseaseSearchState.collectAsState()

    val showErrorToast: (String) -> Unit = { message ->
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    if (addPatientState is UiState.Success) {
        LaunchedEffect(Unit) {
            onPatientAdded()
            deleteImageFile(photoFile)
            viewModel.resetAddPatientState()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        ImagePickerComponent(
            imageUri = photoUri,
            onImagePicked = { uri, file ->
                photoUri = uri
                photoFile = file
            },
            onImageRemoved = { file ->
                file?.let { deleteImageFile(it) }
                photoUri = null
                photoFile = null
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text(stringResource(R.string.add_patient_name)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = birthDate?.formatToString() ?: "",
            onValueChange = {},
            enabled = false,
            label = { Text(stringResource(R.string.add_patient_birthdate)) },
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showDatePicker = true }
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
                    onClick = { gender = "M" }
                )
                Text(text = stringResource(R.string.add_patient_male))
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(
                    selected = gender == "F",
                    onClick = { gender = "F" }
                )
                Text(text = stringResource(R.string.add_patient_female))
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
                        label = { Text(stringResource(R.string.add_patient_blood_type)) },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = showBloodTypeMenu)
                        },
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                        modifier = Modifier
                            .menuAnchor(type = MenuAnchorType.PrimaryEditable, enabled = true)
                    )

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

            Column(modifier = Modifier.weight(0.3f)) {
                ExposedDropdownMenuBox(
                    expanded = showRHMenu,
                    onExpandedChange = { showRHMenu = it }
                ) {
                    OutlinedTextField(
                        value = rhFactor,
                        onValueChange = { },
                        readOnly = true,
                        label = { Text(stringResource(R.string.add_patient_rh_factor)) },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = showRHMenu)
                        },
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                        modifier = Modifier
                            .menuAnchor(type = MenuAnchorType.PrimaryEditable, enabled = true)
                    )

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

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = phone,
            onValueChange = { phone = it },
            label = { Text(stringResource(R.string.add_patient_phone)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions.Default.copy(
                keyboardType = KeyboardType.Phone
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = address,
            onValueChange = { address = it },
            label = { Text(stringResource(R.string.add_patient_address)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text(stringResource(R.string.add_patient_email)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions.Default.copy(
                keyboardType = KeyboardType.Email
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = notes,
            onValueChange = { notes = it },
            label = { Text(stringResource(R.string.add_patient_notes)) },
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp),
            maxLines = 4
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = selectedDisease?.title?.let { HtmlCompat.fromHtml(it, HtmlCompat.FROM_HTML_MODE_COMPACT).toString() } ?: "Seleccione una enfermedad",
            onValueChange = { },
            readOnly = true,
            label = { Text("Enfermedad seleccionada") },
            trailingIcon = {
                IconButton(onClick = { showDiseaseDialog = true }) {
                    Icon(Icons.Default.Search, contentDescription = null)
                }
            },
            modifier = Modifier.fillMaxWidth()
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

        Button(
            onClick = {
                loading = true

                val patient = Patient(
                    name = name,
                    phone = phone.ifEmpty { null },
                    email = email.ifEmpty { null },
                    address = address.ifEmpty { null },
                    notes = notes.ifEmpty { null },
                    birthDate = birthDate,
                    bloodType = bloodType.ifEmpty { null },
                    rhFactor = rhFactor.takeIf { it.isNotEmpty() }?.let { it == "+" },
                    gender = gender,
                    diseaseId = selectedDisease?.id,
                    diseaseCode = selectedDisease?.code,
                    diseaseTitle = selectedDisease?.title
                )

                viewModel.addPatient(patient, photoUri)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            if (loading) {
                CircularProgressIndicator(color = Color.White)
            } else {
                Text(text = stringResource(R.string.add_patient_action_add))
            }
        }

        when (addPatientState) {
            is UiState.Error -> LaunchedEffect(Unit) {
                showErrorToast((addPatientState as UiState.Error).message)
                loading = false
            }

            else -> Unit
        }
    }
}