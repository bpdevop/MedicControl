package com.bpdevop.mediccontrol.ui.screens.prescription

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
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
import androidx.hilt.navigation.compose.hiltViewModel
import com.bpdevop.mediccontrol.R
import com.bpdevop.mediccontrol.core.extensions.formatToString
import com.bpdevop.mediccontrol.core.utils.UiState
import com.bpdevop.mediccontrol.core.utils.deleteImageFile
import com.bpdevop.mediccontrol.data.model.Prescription
import com.bpdevop.mediccontrol.data.model.PrescriptionItem
import com.bpdevop.mediccontrol.ui.components.DatePickerModal
import com.bpdevop.mediccontrol.ui.components.DocumentButtons
import com.bpdevop.mediccontrol.ui.components.DocumentsSection
import com.bpdevop.mediccontrol.ui.viewmodels.PrescriptionViewModel
import kotlinx.coroutines.launch
import java.io.File
import java.util.Date

@Composable
fun EditPrescriptionScreen(
    patientId: String,
    prescriptionId: String,
    viewModel: PrescriptionViewModel = hiltViewModel(),
    onPrescriptionUpdated: () -> Unit,
) {
    val prescriptionState by viewModel.prescriptionState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.getPrescriptionById(patientId, prescriptionId)
    }

    when (val state = prescriptionState) {
        is UiState.Success -> {
            val prescription = state.data
            EditPrescriptionForm(
                patientId = patientId,
                prescription = prescription,
                viewModel = viewModel,
                onPrescriptionUpdated = onPrescriptionUpdated
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
fun EditPrescriptionForm(
    patientId: String,
    prescription: Prescription,
    viewModel: PrescriptionViewModel,
    onPrescriptionUpdated: () -> Unit,
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Estados inicializados con los datos de la receta a editar
    var medicationName by remember { mutableStateOf("") }
    var dosage by remember { mutableStateOf("") }
    var frequency by remember { mutableStateOf("Cada 8 horas") }
    var duration by remember { mutableStateOf("5 días") }
    var notes by remember { mutableStateOf(prescription.notes ?: "") }
    var prescriptionDate by remember { mutableStateOf(prescription.date) }
    var showDatePicker by remember { mutableStateOf(false) }

    val medications = remember { mutableStateListOf(*prescription.medications.toTypedArray()) }
    val documentUris = remember { mutableStateListOf<Uri>() }
    val deletedDocuments = remember { mutableStateListOf<String>() }
    val tempFiles = remember { mutableStateListOf<File>() }

    var loading by remember { mutableStateOf(false) }
    val editPrescriptionState by viewModel.updatePrescriptionState.collectAsState()

    HandleUiStatesPrescription(
        editPrescriptionState,
        context,
        viewModel,
        onPrescriptionUpdated,
        setLoading = { isLoading -> loading = isLoading },
        tempFiles = tempFiles
    )

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
        // Inputs para agregar nuevo medicamento
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

        Button(onClick = {
            medications.add(PrescriptionItem(medicationName, dosage, frequency, duration))
            medicationName = ""
            dosage = ""
            frequency = "Cada 8 horas"
            duration = "5 días"
        }) {
            Text(text = stringResource(R.string.new_prescription_medications_list))
        }

        // Lista de medicamentos agregados
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

        // Notas y Fecha de Receta
        OutlinedTextField(
            value = notes,
            onValueChange = { notes = it },
            label = { Text(stringResource(R.string.new_prescription_notes)) },
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
            maxLines = 4
        )

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

        Spacer(modifier = Modifier.height(16.dp))

        // Documentos
        DocumentsSection(
            initialDocuments = prescription.files,
            newDocumentUris = documentUris,
            onRemoveDocument = { uri -> documentUris.remove(uri) },
            onRemoveExistingDocument = { filePath -> deletedDocuments.add(filePath) }
        )

        DocumentButtons(
            context = context,
            onDocumentUris = { newUris -> documentUris.addAll(newUris) },
            onTempFiles = { files -> tempFiles.addAll(files) }
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                loading = true
                coroutineScope.launch {
                    val updatedPrescription = prescription.copy(
                        medications = medications,
                        notes = notes.ifEmpty { null },
                        date = prescriptionDate,
                        files = prescription.files - deletedDocuments + documentUris.map { it.toString() }
                    )
                    viewModel.updatePrescription(patientId, updatedPrescription, documentUris, prescription.files - deletedDocuments)
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            if (loading) {
                CircularProgressIndicator(color = Color.White)
            } else {
                Text(text = stringResource(R.string.new_prescription_save))
            }
        }
    }
}

@Composable
private fun HandleUiStatesPrescription(
    state: UiState<String>,
    context: Context,
    viewModel: PrescriptionViewModel,
    onPrescriptionUpdated: () -> Unit,
    setLoading: (Boolean) -> Unit,
    tempFiles: List<File>,
) {
    when (state) {
        is UiState.Success -> {
            LaunchedEffect(Unit) {
                tempFiles.forEach { deleteImageFile(it) }
                setLoading(false)
                onPrescriptionUpdated()
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