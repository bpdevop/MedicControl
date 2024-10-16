package com.bpdevop.mediccontrol.ui.screens.examination

import android.content.Context
import android.net.Uri
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.bpdevop.mediccontrol.R
import com.bpdevop.mediccontrol.core.extensions.formatToString
import com.bpdevop.mediccontrol.core.utils.UiState
import com.bpdevop.mediccontrol.data.model.Examination
import com.bpdevop.mediccontrol.ui.components.DatePickerModal
import com.bpdevop.mediccontrol.ui.components.DocumentButtons
import com.bpdevop.mediccontrol.ui.components.DocumentsSection
import com.bpdevop.mediccontrol.ui.viewmodels.ExaminationViewModel
import kotlinx.coroutines.launch
import java.util.Date

@Composable
fun NewExaminationScreen(
    patientId: String,
    viewModel: ExaminationViewModel = hiltViewModel(),
    onExaminationAdded: () -> Unit,
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Estados para los campos
    var temperature by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }
    var height by remember { mutableStateOf("") }
    var temperatureUnit by remember { mutableStateOf("°C") }
    var weightUnit by remember { mutableStateOf("kg") }
    var heightUnit by remember { mutableStateOf("cm") }
    val symptoms = remember { mutableStateListOf<String>() } // Lista de síntomas
    val diagnosis = remember { mutableStateListOf<String>() } // Lista de diagnósticos
    var notes by remember { mutableStateOf("") }
    var examinationDate by remember { mutableStateOf<Date?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }

    var loading by remember { mutableStateOf(false) }

    val documentUris = remember { mutableStateListOf<Uri>() }
    val addExaminationState by viewModel.addExaminationState.collectAsState()

    HandleUiStatesExamination(addExaminationState, context, viewModel, onExaminationAdded, setLoading = { isLoading -> loading = isLoading })

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Inputs para temperatura, peso y altura con Dropdowns
        InputsExamination(
            temperature = temperature,
            onTemperatureChange = { temperature = it },
            weight = weight,
            onWeightChange = { weight = it },
            height = height,
            onHeightChange = { height = it },
            temperatureUnit = temperatureUnit,
            onTemperatureUnitChange = { temperatureUnit = it },
            weightUnit = weightUnit,
            onWeightUnitChange = { weightUnit = it },
            heightUnit = heightUnit,
            onHeightUnitChange = { heightUnit = it }
        )

        // Agregar o remover síntomas y diagnósticos
        AddSymptomsAndDiagnosis(
            symptoms = symptoms,
            onAddSymptom = { newSymptom -> symptoms.add(newSymptom) },
            onRemoveSymptom = { symptom -> symptoms.remove(symptom) },
            diagnosis = diagnosis,
            onAddDiagnosis = { newDiagnosis -> diagnosis.add(newDiagnosis) },
            onRemoveDiagnosis = { diag -> diagnosis.remove(diag) }
        )

        OutlinedTextField(
            value = notes,
            onValueChange = { notes = it },
            label = { Text(stringResource(R.string.new_examination_notes)) },
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
            maxLines = 4
        )

        // Date Picker
        OutlinedTextField(
            value = examinationDate?.formatToString() ?: "",
            onValueChange = {},
            label = { Text(stringResource(R.string.new_examination_date)) },
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showDatePicker = true },
            enabled = false
        )

        if (showDatePicker) {
            DatePickerModal(
                onDateSelected = { selectedDateMillis ->
                    selectedDateMillis?.let { examinationDate = Date(it) }
                    showDatePicker = false
                },
                onDismiss = { showDatePicker = false }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        DocumentsSection(
            initialDocuments = emptyList(),
            newDocumentUris = documentUris,
            onRemoveDocument = { uri -> documentUris.remove(uri) },
            onRemoveExistingDocument = {}
        )

        DocumentButtons(
            context = context,
            onDocumentUris = { newUris ->
                documentUris.addAll(newUris)
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Save Button
        Button(
            onClick = {
                loading = true
                coroutineScope.launch {
                    val temperatureFloat = temperature.takeIf { it.isNotEmpty() }?.toFloatOrNull()
                    val weightFloat = weight.takeIf { it.isNotEmpty() }?.toFloatOrNull()
                    val heightFloat = height.takeIf { it.isNotEmpty() }?.toFloatOrNull()

                    val examination = Examination(
                        temperature = temperatureFloat,
                        temperatureUnit = if (temperatureFloat != null) temperatureUnit else null,
                        weight = weightFloat,
                        weightUnit = if (weightFloat != null) weightUnit else null,
                        height = heightFloat,
                        heightUnit = if (heightFloat != null) heightUnit else null,
                        symptoms = symptoms,
                        diagnosis = diagnosis,
                        notes = notes.ifEmpty { null },
                        date = examinationDate,
                        files = listOf()
                    )
                    viewModel.addExamination(patientId, examination, documentUris)
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            if (loading) {
                CircularProgressIndicator(color = Color.White)
            } else {
                Text(text = stringResource(R.string.new_examination_save))
            }
        }

        if (addExaminationState is UiState.Loading) {
            CircularProgressIndicator(modifier = Modifier.padding(top = 16.dp))
        }
    }
}

@Composable
private fun HandleUiStatesExamination(
    state: UiState<String>,
    context: Context,
    viewModel: ExaminationViewModel,
    onExaminationAdded: () -> Unit,
    setLoading: (Boolean) -> Unit,
) {
    when (state) {
        is UiState.Success -> {
            LaunchedEffect(Unit) {
                setLoading(false)
                onExaminationAdded()
                viewModel.resetAddExaminationState()
            }
        }

        is UiState.Error -> {
            LaunchedEffect(Unit) {
                setLoading(false)
                Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
                viewModel.resetAddExaminationState()
            }
        }

        else -> Unit
    }
}

@Composable
fun InputsExamination(
    temperature: String,
    onTemperatureChange: (String) -> Unit,
    weight: String,
    onWeightChange: (String) -> Unit,
    height: String,
    onHeightChange: (String) -> Unit,
    temperatureUnit: String,
    onTemperatureUnitChange: (String) -> Unit,
    weightUnit: String,
    onWeightUnitChange: (String) -> Unit,
    heightUnit: String,
    onHeightUnitChange: (String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // Temperature Input
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = temperature,
                onValueChange = onTemperatureChange,
                label = { Text(stringResource(R.string.new_examination_temperature)) },
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
            )

            Spacer(modifier = Modifier.width(8.dp))

            // Dropdown for temperature unit
            var expandedTemperature by remember { mutableStateOf(false) }
            Box {
                Button(onClick = { expandedTemperature = !expandedTemperature }) {
                    Text(text = temperatureUnit)
                }
                DropdownMenu(
                    expanded = expandedTemperature,
                    onDismissRequest = { expandedTemperature = false }
                ) {
                    DropdownMenuItem(text = { Text("°C") }, onClick = {
                        onTemperatureUnitChange("°C")
                        expandedTemperature = false
                    })
                    DropdownMenuItem(text = { Text("°F") }, onClick = {
                        onTemperatureUnitChange("°F")
                        expandedTemperature = false
                    })
                }
            }
        }

        // Weight Input
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = weight,
                onValueChange = onWeightChange,
                label = { Text(stringResource(R.string.new_examination_weight)) },
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
            )

            Spacer(modifier = Modifier.width(8.dp))

            // Dropdown for weight unit
            var expandedWeight by remember { mutableStateOf(false) }
            Box {
                Button(onClick = { expandedWeight = !expandedWeight }) {
                    Text(text = weightUnit)
                }
                DropdownMenu(
                    expanded = expandedWeight,
                    onDismissRequest = { expandedWeight = false }
                ) {
                    DropdownMenuItem(text = { Text("kg") }, onClick = {
                        onWeightUnitChange("kg")
                        expandedWeight = false
                    })
                    DropdownMenuItem(text = { Text("lb") }, onClick = {
                        onWeightUnitChange("lb")
                        expandedWeight = false
                    })
                }
            }
        }

        // Height Input
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = height,
                onValueChange = onHeightChange,
                label = { Text(stringResource(R.string.new_examination_height)) },
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
            )

            Spacer(modifier = Modifier.width(8.dp))

            // Dropdown for height unit
            var expandedHeight by remember { mutableStateOf(false) }
            Box {
                Button(onClick = { expandedHeight = !expandedHeight }) {
                    Text(text = heightUnit)
                }
                DropdownMenu(
                    expanded = expandedHeight,
                    onDismissRequest = { expandedHeight = false }
                ) {
                    DropdownMenuItem(text = { Text("cm") }, onClick = {
                        onHeightUnitChange("cm")
                        expandedHeight = false
                    })
                    DropdownMenuItem(text = { Text("inch") }, onClick = {
                        onHeightUnitChange("inch")
                        expandedHeight = false
                    })
                    DropdownMenuItem(text = { Text("feet") }, onClick = {
                        onHeightUnitChange("feet")
                        expandedHeight = false
                    })
                }
            }
        }
    }
}

@Composable
fun AddSymptomsAndDiagnosis(
    symptoms: List<String>,
    onAddSymptom: (String) -> Unit,
    onRemoveSymptom: (String) -> Unit,
    diagnosis: List<String>,
    onAddDiagnosis: (String) -> Unit,
    onRemoveDiagnosis: (String) -> Unit,
) {
    var newSymptom by remember { mutableStateOf("") }
    var newDiagnosis by remember { mutableStateOf("") }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {

        // Sección de síntomas con botón de agregar
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = newSymptom,
                onValueChange = { newSymptom = it },
                label = { Text(stringResource(R.string.new_examination_symptoms)) },
                modifier = Modifier.weight(1f),
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(onClick = {
                if (newSymptom.isNotEmpty()) {
                    onAddSymptom(newSymptom)
                    newSymptom = ""
                }
            }) {
                Icon(imageVector = Icons.Default.Add, contentDescription = null)
            }
        }

        if (symptoms.isNotEmpty()) {
            Text(text = stringResource(R.string.new_examination_symptoms_list))
            symptoms.forEach { symptom ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = symptom)
                    IconButton(onClick = { onRemoveSymptom(symptom) }) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = null)
                    }
                }
            }
        }

        // Sección de diagnósticos con botón de agregar
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = newDiagnosis,
                onValueChange = { newDiagnosis = it },
                label = { Text(stringResource(R.string.new_examination_diagnosis)) },
                modifier = Modifier.weight(1f),
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(onClick = {
                if (newDiagnosis.isNotEmpty()) {
                    onAddDiagnosis(newDiagnosis)
                    newDiagnosis = ""
                }
            }) {
                Icon(imageVector = Icons.Default.Add, contentDescription = null)
            }
        }

        if (diagnosis.isNotEmpty()) {
            Text(text = stringResource(R.string.new_examination_diagnosis_list))
            diagnosis.forEach { diag ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = diag)
                    IconButton(onClick = { onRemoveDiagnosis(diag) }) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = null)
                    }
                }
            }
        }
    }
}

@Composable
fun DocumentButtons(
    onCameraClick: () -> Unit,
    onVideoClick: () -> Unit,
    onDocumentClick: () -> Unit,
) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
        IconButton(onClick = onCameraClick) {
            Icon(imageVector = Icons.Default.CameraAlt, contentDescription = null)
        }

        IconButton(onClick = onVideoClick) {
            Icon(imageVector = Icons.Default.Videocam, contentDescription = null)
        }

        IconButton(onClick = onDocumentClick) {
            Icon(imageVector = Icons.Default.Folder, contentDescription = null)
        }
    }
}