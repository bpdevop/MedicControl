package com.bpdevop.mediccontrol.ui.screens.prescription

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import com.bpdevop.mediccontrol.BuildConfig
import com.bpdevop.mediccontrol.R
import com.bpdevop.mediccontrol.core.extensions.createImageFile
import com.bpdevop.mediccontrol.core.extensions.createVideoFile
import com.bpdevop.mediccontrol.core.extensions.formatToString
import com.bpdevop.mediccontrol.core.utils.UiState
import com.bpdevop.mediccontrol.data.model.Prescription
import com.bpdevop.mediccontrol.data.model.PrescriptionItem
import com.bpdevop.mediccontrol.ui.components.DatePickerModal
import com.bpdevop.mediccontrol.ui.screens.examination.DocumentButtons
import com.bpdevop.mediccontrol.ui.viewmodels.PrescriptionViewModel
import kotlinx.coroutines.launch
import java.io.File
import java.util.Date

@Composable
fun NewPrescriptionScreen(
    patientId: String,
    viewModel: PrescriptionViewModel = hiltViewModel(),
    onPrescriptionAdded: () -> Unit,
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val cameraUri = remember { mutableStateOf<Uri>(Uri.EMPTY) }
    val videoUri = remember { mutableStateOf<Uri>(Uri.EMPTY) }
    val photoFiles = remember { mutableStateListOf<File>() }
    val videoFiles = remember { mutableStateListOf<File>() }

    // Estados para los campos
    var medicationName by remember { mutableStateOf("") }
    var dosage by remember { mutableStateOf("") }
    var frequency by remember { mutableStateOf("Cada 8 horas") }
    var duration by remember { mutableStateOf("5 días") }
    val medications = remember { mutableStateListOf<PrescriptionItem>() } // Lista de medicamentos
    var notes by remember { mutableStateOf("") }
    var prescriptionDate by remember { mutableStateOf<Date?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }

    var loading by remember { mutableStateOf(false) }

    val documentUris = remember { mutableStateListOf<Uri>() }
    val addPrescriptionState by viewModel.addPrescriptionState.collectAsState()

    HandleUiStatesPrescription(addPrescriptionState, context, viewModel, onPrescriptionAdded, setLoading = { isLoading -> loading = isLoading })

    // Launchers para tomar fotos, grabar videos y seleccionar documentos
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
        // Inputs para nombre del medicamento, dosis, frecuencia, y duración
        OutlinedTextField(
            value = medicationName,
            onValueChange = { medicationName = it },
            label = { Text(stringResource(R.string.new_prescription_medication)) },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = dosage,
            onValueChange = { dosage = it },
            label = { Text(stringResource(R.string.new_prescription_dosage)) },
            modifier = Modifier.fillMaxWidth()
        )

        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = frequency,
                onValueChange = { frequency = it },
                label = { Text(stringResource(R.string.new_prescription_frequency)) },
                modifier = Modifier.weight(1f)
            )

            Spacer(modifier = Modifier.width(8.dp))

            OutlinedTextField(
                value = duration,
                onValueChange = { duration = it },
                label = { Text(stringResource(R.string.new_prescription_duration)) },
                modifier = Modifier.weight(1f)
            )
        }

        // Botón para agregar medicamento
        Button(onClick = {
            medications.add(PrescriptionItem(medicationName, dosage, frequency, duration))
            medicationName = ""
            dosage = ""
            frequency = "Cada 8 horas"
            duration = "5 días"
        }) {
            Text(text = stringResource(R.string.new_prescription_medications_list))
        }

        // Listado de medicamentos agregados
        medications.forEach { medication ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("${medication.name} - ${medication.dosage} - ${medication.frequency} - ${medication.duration}")
                IconButton(onClick = { medications.remove(medication) }) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = null)
                }
            }
        }

        OutlinedTextField(
            value = notes,
            onValueChange = { notes = it },
            label = { Text(stringResource(R.string.new_prescription_notes)) },
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
            maxLines = 4
        )

        // Date Picker
        OutlinedTextField(
            value = prescriptionDate?.formatToString() ?: "",
            onValueChange = {},
            label = { Text(stringResource(R.string.new_prescription_date)) },
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showDatePicker = true },
            enabled = false
        )

        if (showDatePicker) {
            DatePickerModal(
                onDateSelected = { selectedDateMillis ->
                    selectedDateMillis?.let { prescriptionDate = Date(it) }
                    showDatePicker = false
                },
                onDismiss = { showDatePicker = false }
            )
        }

        // Botones para cámara, video y documentos
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

        // Guardar receta
        Button(onClick = {
            loading = true
            coroutineScope.launch {
                val prescription = Prescription(
                    medications = medications,
                    notes = notes.ifEmpty { null },
                    date = prescriptionDate,
                    files = listOf() // Aquí se guardarán los archivos
                )
                viewModel.addPrescription(patientId, prescription, documentUris)
            }
        }) {
            if (loading) {
                CircularProgressIndicator(color = Color.White)
            } else {
                Text(text = stringResource(R.string.new_prescription_save))
            }
        }

        if (addPrescriptionState is UiState.Loading) {
            CircularProgressIndicator(modifier = Modifier.padding(top = 16.dp))
        }
    }
}

@Composable
private fun HandleUiStatesPrescription(
    state: UiState<String>,
    context: Context,
    viewModel: PrescriptionViewModel,
    onPrescriptionAdded: () -> Unit,
    setLoading: (Boolean) -> Unit,
) {
    when (state) {
        is UiState.Success -> {
            LaunchedEffect(Unit) {
                setLoading(false)
                onPrescriptionAdded()
                viewModel.resetAddPrescriptionState()
            }
        }

        is UiState.Error -> {
            LaunchedEffect(Unit) {
                setLoading(false)
                Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
                viewModel.resetAddPrescriptionState()
            }
        }

        else -> Unit
    }
}