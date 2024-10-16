package com.bpdevop.mediccontrol.ui.screens.radiology

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import com.bpdevop.mediccontrol.data.model.Radiology
import com.bpdevop.mediccontrol.ui.components.DatePickerModal
import com.bpdevop.mediccontrol.ui.components.DocumentButtons
import com.bpdevop.mediccontrol.ui.components.DocumentsSection
import com.bpdevop.mediccontrol.ui.viewmodels.RadiologyViewModel
import kotlinx.coroutines.launch
import java.io.File
import java.util.Date

@Composable
fun NewRadiologyScreen(
    patientId: String,
    viewModel: RadiologyViewModel = hiltViewModel(),
    onRadiologyAdded: () -> Unit,
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var title by remember { mutableStateOf("") }
    var result by remember { mutableStateOf("") }
    var radiologyDate by remember { mutableStateOf<Date?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }
    var loading by remember { mutableStateOf(false) }

    val documentUris = remember { mutableStateListOf<Uri>() }
    val tempFiles = remember { mutableStateListOf<File>() }
    val addRadiologyState by viewModel.addRadiologyState.collectAsState()

    HandleUiStatesRadiology(
        addRadiologyState,
        context,
        viewModel,
        onRadiologyAdded,
        setLoading = { isLoading -> loading = isLoading },
        tempFiles = tempFiles
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
        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text(stringResource(R.string.new_radiology_title)) },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = result,
            onValueChange = { result = it },
            label = { Text(stringResource(R.string.new_radiology_result)) },
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
            maxLines = 4
        )

        // Selector de fecha
        OutlinedTextField(
            value = radiologyDate?.formatToString() ?: "",
            onValueChange = {},
            label = { Text(stringResource(R.string.new_radiology_date)) },
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showDatePicker = true },
            enabled = false
        )

        if (showDatePicker) {
            DatePickerModal(
                onDateSelected = { selectedDateMillis ->
                    selectedDateMillis?.let { radiologyDate = Date(it) }
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
                    val radiology = Radiology(
                        title = title,
                        result = result.ifEmpty { null }.orEmpty(),
                        date = radiologyDate,
                        files = emptyList()
                    )
                    viewModel.addRadiology(patientId, radiology, documentUris)
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            if (loading) {
                CircularProgressIndicator(color = Color.White)
            } else {
                Text(text = stringResource(R.string.new_radiology_save))
            }
        }

        if (addRadiologyState is UiState.Loading) {
            CircularProgressIndicator(modifier = Modifier.padding(top = 16.dp))
        }
    }
}

@Composable
private fun HandleUiStatesRadiology(
    state: UiState<String>,
    context: Context,
    viewModel: RadiologyViewModel,
    onRadiologyAdded: () -> Unit,
    setLoading: (Boolean) -> Unit,
    tempFiles: List<File>,
) {
    when (state) {
        is UiState.Success -> {
            LaunchedEffect(Unit) {
                tempFiles.forEach { deleteImageFile(it) }
                setLoading(false)
                onRadiologyAdded()
                viewModel.resetAddRadiologyState()
            }
        }

        is UiState.Error -> {
            LaunchedEffect(Unit) {
                setLoading(false)
                Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
                viewModel.resetAddRadiologyState()
            }
        }

        else -> Unit
    }
}