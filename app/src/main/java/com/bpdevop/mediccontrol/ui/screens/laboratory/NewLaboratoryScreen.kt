package com.bpdevop.mediccontrol.ui.screens.laboratory

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
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
import com.bpdevop.mediccontrol.data.model.LabTestItem
import com.bpdevop.mediccontrol.data.model.Laboratory
import com.bpdevop.mediccontrol.ui.components.DatePickerModal
import com.bpdevop.mediccontrol.ui.components.DocumentButtons
import com.bpdevop.mediccontrol.ui.components.DocumentsSection
import com.bpdevop.mediccontrol.ui.viewmodels.LaboratoryViewModel
import kotlinx.coroutines.launch
import java.io.File
import java.util.Date

@Composable
fun NewLaboratoryScreen(
    patientId: String,
    viewModel: LaboratoryViewModel = hiltViewModel(),
    onLaboratoryAdded: () -> Unit,
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val documentUris = remember { mutableStateListOf<Uri>() }
    val tempFiles = remember { mutableStateListOf<File>() }

    // Estado para manejar datos de laboratorio
    val labTests = remember { mutableStateListOf<LabTestItem>() }
    var notes by remember { mutableStateOf("") }
    var laboratoryDate by remember { mutableStateOf<Date?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }
    var loading by remember { mutableStateOf(false) }
    val addLaboratoryState by viewModel.addLaboratoryState.collectAsState()

    HandleUiStatesLaboratory(
        addLaboratoryState,
        context,
        viewModel,
        onLaboratoryAdded,
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
        // Sección para agregar pruebas de laboratorio
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

        // Selector de fecha
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
            },
            onTempFiles = { file ->
                tempFiles.addAll(file)
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Botón de guardar
        Button(
            onClick = {
                loading = true
                coroutineScope.launch {
                    val laboratory = Laboratory(
                        tests = emptyList(),
                        notes = notes.ifEmpty { null },
                        date = laboratoryDate,
                        files = listOf() // You may handle file storage differently here
                    )
                    viewModel.addLaboratory(patientId, laboratory, documentUris)
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

        if (addLaboratoryState is UiState.Loading) {
            CircularProgressIndicator(modifier = Modifier.padding(top = 16.dp))
        }
    }
}

@Composable
private fun HandleUiStatesLaboratory(
    state: UiState<String>,
    context: Context,
    viewModel: LaboratoryViewModel,
    onLaboratoryAdded: () -> Unit,
    setLoading: (Boolean) -> Unit,
    tempFiles: List<File>,
) {
    when (state) {
        is UiState.Success -> {
            LaunchedEffect(Unit) {
                tempFiles.forEach { deleteImageFile(it) }
                setLoading(false)
                onLaboratoryAdded()
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

@Composable
fun AddLabTest(
    labTests: List<LabTestItem>,
    onAddTest: (LabTestItem) -> Unit,
    onRemoveTest: (LabTestItem) -> Unit,
) {
    var testName by remember { mutableStateOf("") }
    var testResult by remember { mutableStateOf("") }
    var isNormal by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // Fila para nombre y resultado de la prueba
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = testName,
                onValueChange = { testName = it },
                label = { Text(stringResource(R.string.new_lab_test_name)) },
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp),
            )
            OutlinedTextField(
                value = testResult,
                onValueChange = { testResult = it },
                label = { Text(stringResource(R.string.new_lab_test_result)) },
                modifier = Modifier.weight(1f)
            )
        }

        // Fila para Checkbox y botón de agregar prueba
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = isNormal,
                    onCheckedChange = { isNormal = it },
                )
                Text(text = stringResource(R.string.new_lab_test_normal_checkbox))
            }
            IconButton(onClick = {
                if (testName.isNotEmpty() && testResult.isNotEmpty()) {
                    onAddTest(LabTestItem(name = testName, result = testResult, isNormal = isNormal))
                    testName = ""
                    testResult = ""
                    isNormal = false
                }
            }) {
                Icon(imageVector = Icons.Default.Add, contentDescription = null)
            }
        }

        // Mostrar las pruebas agregadas
        labTests.forEach { labTest ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${labTest.name} - ${labTest.result} (${if (labTest.isNormal) stringResource(R.string.new_lab_test_normal) else stringResource(R.string.new_lab_test_abnormal)})",
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = { onRemoveTest(labTest) }) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = null)
                }
            }
        }
    }
}