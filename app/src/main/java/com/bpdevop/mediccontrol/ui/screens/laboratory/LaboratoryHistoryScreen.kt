package com.bpdevop.mediccontrol.ui.screens.laboratory

import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.bpdevop.mediccontrol.R
import com.bpdevop.mediccontrol.core.extensions.formatToString
import com.bpdevop.mediccontrol.core.extensions.openUrlInCustomTab
import com.bpdevop.mediccontrol.core.utils.UiState
import com.bpdevop.mediccontrol.data.model.Laboratory
import com.bpdevop.mediccontrol.ui.components.DateHeader
import com.bpdevop.mediccontrol.ui.components.MessageDialog
import com.bpdevop.mediccontrol.ui.components.MoreOptionsMenu
import com.bpdevop.mediccontrol.ui.viewmodels.LaboratoryViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LaboratoryHistoryScreen(
    patientId: String,
    viewModel: LaboratoryViewModel = hiltViewModel(),
    onEditLabRecord: (String) -> Unit,
) {
    val labHistoryState by viewModel.laboratoryHistoryState.collectAsState()
    val deleteLabRecordState by viewModel.deleteLaboratoryState.collectAsState()
    val isRefreshing = labHistoryState is UiState.Loading
    val coroutineScope = rememberCoroutineScope()

    val context = LocalContext.current

    var labRecordToDelete by remember { mutableStateOf<Laboratory?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.getLaboratoryHistory(patientId)
    }

    when (deleteLabRecordState) {
        is UiState.Success -> {
            LaunchedEffect(Unit) {
                viewModel.resetDeleteLaboratoryState()
                viewModel.getLaboratoryHistory(patientId)
            }
        }

        is UiState.Error -> {
            LaunchedEffect(Unit) {
                Toast.makeText(context, (deleteLabRecordState as UiState.Error).message, Toast.LENGTH_SHORT).show()
            }
        }

        else -> Unit
    }

    if (showDeleteDialog && labRecordToDelete != null) {
        MessageDialog(
            title = stringResource(R.string.dialog_delete_laboratory_title),
            message = stringResource(R.string.dialog_delete_laboratory_message),
            dismissButtonText = stringResource(id = android.R.string.cancel),
            onConfirm = {
                viewModel.deleteLaboratory(patientId, labRecordToDelete!!.id)
                showDeleteDialog = false
            },
            onDismiss = { showDeleteDialog = false }
        )
    }

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = {
            coroutineScope.launch { viewModel.getLaboratoryHistory(patientId) }
        },
        modifier = Modifier.fillMaxSize()
    ) {
        when (val state = labHistoryState) {
            is UiState.Success -> {
                val labRecords = state.data
                if (labRecords.isEmpty()) {
                    EmptyLabHistoryScreen()
                } else {
                    LabHistoryList(
                        labRecords = labRecords,
                        onEditLabRecord = onEditLabRecord,
                        onDeleteLabRecord = { labRecord ->
                            labRecordToDelete = labRecord
                            showDeleteDialog = true
                        },
                        onPreviewFile = { fileUrl -> context.openUrlInCustomTab(fileUrl) }
                    )
                }
            }

            is UiState.Error -> {
                ErrorLabHistoryScreen(state.message)
            }

            else -> Unit
        }
    }
}

@Composable
fun EmptyLabHistoryScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stringResource(R.string.laboratory_history_empty),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

@Composable
fun ErrorLabHistoryScreen(message: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LabHistoryList(
    labRecords: List<Laboratory>,
    onEditLabRecord: (String) -> Unit,
    onDeleteLabRecord: (Laboratory) -> Unit,
    onPreviewFile: (String) -> Unit,
) {
    val groupedLabRecords = labRecords
        .sortedByDescending { it.date }
        .groupBy { it.date?.formatToString("dd/MM/yyyy") ?: "" }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        groupedLabRecords.forEach { (date, recordsForDate) ->
            stickyHeader {
                DateHeader(date)
            }
            itemsIndexed(recordsForDate) { index, labRecord ->
                LabHistoryItem(
                    labRecord = labRecord,
                    onEditLabRecord = onEditLabRecord,
                    onDeleteLabRecord = onDeleteLabRecord,
                    onPreviewFile = onPreviewFile
                )

                if (index < recordsForDate.size - 1) HorizontalDivider()
            }
        }
    }
}

@Composable
fun LabHistoryItem(
    labRecord: Laboratory,
    onEditLabRecord: (String) -> Unit,
    onDeleteLabRecord: (Laboratory) -> Unit,
    onPreviewFile: (String) -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.laboratory_history_tests),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    labRecord.tests.forEach { test ->
                        Text(
                            text = "${test.name ?: "N/A"} - ${test.result} - ${if (test.isNormal) stringResource(R.string.laboratory_history_normal) else stringResource(R.string.laboratory_history_abnormal)}",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }

                MoreOptionsMenu(
                    onEditClick = { onEditLabRecord(labRecord.id) },
                    onDeleteClick = { onDeleteLabRecord(labRecord) },
                    modifier = Modifier.align(Alignment.CenterVertically)
                )
            }

            // Notas
            if (!labRecord.notes.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.laboratory_history_notes),
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = labRecord.notes,
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            // Archivos adjuntos
            if (labRecord.files.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.laboratory_history_files),
                    style = MaterialTheme.typography.bodyMedium
                )
                labRecord.files.forEach { file ->
                    Text(
                        text = file,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.clickable { onPreviewFile(file) }
                    )
                }
            }

            // Línea inferior como divisora
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
