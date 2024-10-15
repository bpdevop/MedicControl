package com.bpdevop.mediccontrol.ui.screens.laboratory

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import com.bpdevop.mediccontrol.BuildConfig
import com.bpdevop.mediccontrol.R
import com.bpdevop.mediccontrol.core.extensions.createImageFile
import com.bpdevop.mediccontrol.core.extensions.createVideoFile
import com.bpdevop.mediccontrol.core.extensions.formatToString
import com.bpdevop.mediccontrol.core.utils.UiState
import com.bpdevop.mediccontrol.data.model.Laboratory
import com.bpdevop.mediccontrol.ui.components.DatePickerModal
import com.bpdevop.mediccontrol.ui.screens.examination.DocumentButtons
import com.bpdevop.mediccontrol.ui.screens.examination.DocumentsSection
import com.bpdevop.mediccontrol.ui.viewmodels.LaboratoryViewModel
import kotlinx.coroutines.launch
import java.io.File
import java.util.Date

@Composable
fun EditLaboratoryScreen(
    patientId: String,
    laboratoryId: String,
    viewModel: LaboratoryViewModel = hiltViewModel(),
    onLaboratoryUpdated: () -> Unit,
) {
    val laboratoryState by viewModel.laboratoryState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.getLaboratoryById(patientId, laboratoryId)
    }

    when (val state = laboratoryState) {
        is UiState.Success -> {
            val laboratory = state.data
            EditLaboratoryForm(
                patientId = patientId,
                laboratory = laboratory,
                viewModel = viewModel,
                onLaboratoryUpdated = onLaboratoryUpdated
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
fun EditLaboratoryForm(
    patientId: String,
    laboratory: Laboratory,
    viewModel: LaboratoryViewModel,
    onLaboratoryUpdated: () -> Unit,
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Estados inicializados con los datos del laboratorio a editar
    var notes by remember { mutableStateOf(laboratory.notes ?: "") }
    var laboratoryDate by remember { mutableStateOf(laboratory.date) }
    val labTests = remember { mutableStateListOf(*laboratory.tests.toTypedArray()) }
    var showDatePicker by remember { mutableStateOf(false) }
    var loading by remember { mutableStateOf(false) }

    // Documentos
    val initialDocuments = remember { laboratory.files }
    val documentUris = remember { mutableStateListOf<Uri>() }
    val deletedDocuments = remember { mutableStateListOf<String>() }

    val editLaboratoryState by viewModel.updateLaboratoryState.collectAsState()

    HandleUiStatesLaboratory(editLaboratoryState, context, viewModel, onLaboratoryUpdated) { isLoading ->
        loading = isLoading
    }

    // Launchers para captura y selección de documentos
    val cameraUri = remember { mutableStateOf<Uri>(Uri.EMPTY) }
    val videoUri = remember { mutableStateOf<Uri>(Uri.EMPTY) }
    val photoFiles = remember { mutableStateListOf<File>() }
    val videoFiles = remember { mutableStateListOf<File>() }

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            cameraUri.value.let { documentUris.add(it) }
        } else {
            photoFiles.removeLastOrNull()?.delete()
        }
    }

    val videoLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CaptureVideo()) { success ->
        if (success) {
            videoUri.value.let { documentUris.add(it) }
        } else {
            videoFiles.removeLastOrNull()?.delete()
        }
    }

    val documentLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
        documentUris.addAll(uris)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AddLabTest(
            labTests = labTests,
            onAddTest = { test -> labTests.add(test) },
            onRemoveTest = { test -> labTests.remove(test) }
        )

        OutlinedTextField(
            value = notes,
            onValueChange = { notes = it },
            label = { Text(stringResource(R.string.new_lab_notes)) },
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
            maxLines = 4
        )

        // Date Picker
        OutlinedTextField(
            value = laboratoryDate?.formatToString() ?: "",
            onValueChange = {},
            label = { Text(stringResource(R.string.new_lab_date)) },
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showDatePicker = true },
            enabled = false
        )

        if (showDatePicker) {
            DatePickerModal(
                onDateSelected = { selectedDateMillis ->
                    selectedDateMillis?.let { laboratoryDate = Date(it) }
                    showDatePicker = false
                },
                onDismiss = { showDatePicker = false }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        DocumentsSection(
            initialDocuments = initialDocuments,
            newDocumentUris = documentUris,
            onRemoveDocument = { uri -> documentUris.remove(uri) },
            onRemoveExistingDocument = { filePath -> deletedDocuments.add(filePath) }
        )

        DocumentButtons(
            onCameraClick = {
                val photoFileCreated = context.createImageFile()
                photoFiles.add(photoFileCreated)
                cameraUri.value = FileProvider.getUriForFile(context, "${BuildConfig.APPLICATION_ID}.provider", photoFileCreated)
                cameraLauncher.launch(cameraUri.value)
            },
            onVideoClick = {
                val videoFileCreated = context.createVideoFile()
                videoFiles.add(videoFileCreated)
                videoUri.value = FileProvider.getUriForFile(context, "${BuildConfig.APPLICATION_ID}.provider", videoFileCreated)
                videoLauncher.launch(videoUri.value)
            },
            onDocumentClick = { documentLauncher.launch("*/*") }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Botón de guardar
        Button(
            onClick = {
                loading = true
                coroutineScope.launch {
                    val updatedLaboratory = laboratory.copy(
                        tests = labTests,
                        notes = notes.ifEmpty { null },
                        date = laboratoryDate,
                        files = if (documentUris.isNotEmpty()) {
                            initialDocuments + documentUris.map { it.toString() }
                        } else {
                            initialDocuments
                        }
                    )
                    viewModel.updateLaboratory(patientId, updatedLaboratory, documentUris, initialDocuments - deletedDocuments)
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            if (loading) {
                CircularProgressIndicator(color = Color.White)
            } else {
                Text(text = stringResource(R.string.new_lab_save))
            }
        }

        if (editLaboratoryState is UiState.Loading) {
            CircularProgressIndicator(modifier = Modifier.padding(top = 16.dp))
        }
    }
}

@Composable
private fun HandleUiStatesLaboratory(
    state: UiState<String>,
    context: Context,
    viewModel: LaboratoryViewModel,
    onExaminationUpdated: () -> Unit,
    setLoading: (Boolean) -> Unit,
) {
    when (state) {
        is UiState.Success -> {
            LaunchedEffect(Unit) {
                setLoading(false)
                onExaminationUpdated()
                viewModel.resetAddLaboratoryState()
            }
        }

        is UiState.Error -> {
            LaunchedEffect(Unit) {
                setLoading(false)
                Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
                viewModel.resetAddLaboratoryState()
            }
        }

        else -> Unit
    }
}