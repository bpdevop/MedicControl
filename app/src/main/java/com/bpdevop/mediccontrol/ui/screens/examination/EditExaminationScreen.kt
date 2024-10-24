package com.bpdevop.mediccontrol.ui.screens.examination

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.bpdevop.mediccontrol.R
import com.bpdevop.mediccontrol.core.extensions.formatToString
import com.bpdevop.mediccontrol.core.utils.UiState
import com.bpdevop.mediccontrol.core.utils.deleteImageFile
import com.bpdevop.mediccontrol.data.model.Examination
import com.bpdevop.mediccontrol.ui.components.DatePickerModal
import com.bpdevop.mediccontrol.ui.components.DocumentButtons
import com.bpdevop.mediccontrol.ui.components.DocumentsSection
import com.bpdevop.mediccontrol.ui.viewmodels.ExaminationViewModel
import kotlinx.coroutines.launch
import java.io.File
import java.util.Date

@Composable
fun EditExaminationScreen(
    patientId: String,
    examinationId: String,
    viewModel: ExaminationViewModel = hiltViewModel(),
    onExaminationUpdated: () -> Unit,
) {
    val examinationState by viewModel.examinationState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.getExaminationById(patientId, examinationId)
    }

    when (val state = examinationState) {
        is UiState.Success -> {
            val examination = state.data
            EditExaminationForm(
                patientId = patientId,
                examination = examination,
                viewModel = viewModel,
                onExaminationUpdated = onExaminationUpdated
            )
        }

        is UiState.Error -> {
            Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
        }

        is UiState.Loading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }

        else -> Unit
    }
}

@Composable
fun EditExaminationForm(
    patientId: String,
    examination: Examination,
    viewModel: ExaminationViewModel,
    onExaminationUpdated: () -> Unit,
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Estados inicializados con los datos del examen a editar
    var temperature by remember { mutableStateOf(examination.temperature?.toString() ?: "") }
    var weight by remember { mutableStateOf(examination.weight?.toString() ?: "") }
    var height by remember { mutableStateOf(examination.height?.toString() ?: "") }
    var temperatureUnit by remember { mutableStateOf(examination.temperatureUnit ?: "°C") }
    var weightUnit by remember { mutableStateOf(examination.weightUnit ?: "kg") }
    var heightUnit by remember { mutableStateOf(examination.heightUnit ?: "cm") }
    val symptoms = remember { mutableStateListOf(*examination.symptoms.toTypedArray()) }
    val diagnosis = remember { mutableStateListOf(*examination.diagnosis.toTypedArray()) }
    var notes by remember { mutableStateOf(examination.notes ?: "") }
    var examinationDate by remember { mutableStateOf(examination.date) }
    var showDatePicker by remember { mutableStateOf(false) }

    var loading by remember { mutableStateOf(false) }

    // Documentos
    val initialDocuments = remember { examination.files }
    val documentUris = remember { mutableStateListOf<Uri>() }
    val deletedDocuments = remember { mutableStateListOf<String>() }
    val tempFiles = remember { mutableStateListOf<File>() }

    val editExaminationState by viewModel.updateExaminationState.collectAsState()

    HandleUiStatesExamination(
        editExaminationState,
        context,
        viewModel,
        onExaminationUpdated,
        setLoading = { isLoading -> loading = isLoading },
        tempFiles = tempFiles,
    )

    // Limpiar archivos temporales al retroceder
    BackHandler {
        tempFiles.forEach { deleteImageFile(it) }
    }

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
            initialDocuments = initialDocuments,
            newDocumentUris = documentUris,
            onRemoveDocument = { uri ->
                documentUris.remove(uri)
            },
            onRemoveExistingDocument = { filePath ->
                deletedDocuments.add(filePath)
            }
        )

        // Botones para seleccionar documentos
        DocumentButtons(
            context = context,
            onDocumentUris = { newUris ->
                documentUris.addAll(newUris)
            },
            onTempFiles = { file ->
                tempFiles.addAll(file)
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

                    val updatedExamination = examination.copy(
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
                        files = initialDocuments
                    )

                    viewModel.updateExamination(patientId, updatedExamination, documentUris, initialDocuments - deletedDocuments)
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
    }
}

@Composable
private fun HandleUiStatesExamination(
    state: UiState<String>,
    context: Context,
    viewModel: ExaminationViewModel,
    onExaminationUpdated: () -> Unit,
    setLoading: (Boolean) -> Unit,
    tempFiles: List<File>,
) {
    when (state) {
        is UiState.Success -> {
            LaunchedEffect(Unit) {
                tempFiles.forEach { deleteImageFile(it) }
                setLoading(false)
                onExaminationUpdated()
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