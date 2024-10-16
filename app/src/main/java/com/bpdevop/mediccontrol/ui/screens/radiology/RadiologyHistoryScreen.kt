package com.bpdevop.mediccontrol.ui.screens.radiology

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
import com.bpdevop.mediccontrol.data.model.Radiology
import com.bpdevop.mediccontrol.ui.components.DateHeader
import com.bpdevop.mediccontrol.ui.components.MessageDialog
import com.bpdevop.mediccontrol.ui.components.MoreOptionsMenu
import com.bpdevop.mediccontrol.ui.viewmodels.RadiologyViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RadiologyHistoryScreen(
    patientId: String,
    viewModel: RadiologyViewModel = hiltViewModel(),
    onEditRadiologyRecord: (String) -> Unit,
) {
    val radiologyHistoryState by viewModel.radiologyHistoryState.collectAsState()
    val deleteRadiologyState by viewModel.deleteRadiologyState.collectAsState()
    val isRefreshing = radiologyHistoryState is UiState.Loading
    val coroutineScope = rememberCoroutineScope()

    val context = LocalContext.current

    var radiologyRecordToDelete by remember { mutableStateOf<Radiology?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.getRadiologyHistory(patientId)
    }

    when (deleteRadiologyState) {
        is UiState.Success -> {
            LaunchedEffect(Unit) {
                viewModel.resetDeleteRadiologyState()
                viewModel.getRadiologyHistory(patientId)
            }
        }

        is UiState.Error -> {
            LaunchedEffect(Unit) {
                Toast.makeText(context, (deleteRadiologyState as UiState.Error).message, Toast.LENGTH_SHORT).show()
            }
        }

        else -> Unit
    }

    if (showDeleteDialog && radiologyRecordToDelete != null) {
        MessageDialog(
            title = stringResource(R.string.dialog_delete_radiology_title),
            message = stringResource(R.string.dialog_delete_radiology_message),
            dismissButtonText = stringResource(id = android.R.string.cancel),
            onConfirm = {
                viewModel.deleteRadiology(patientId, radiologyRecordToDelete!!.id)
                showDeleteDialog = false
            },
            onDismiss = { showDeleteDialog = false }
        )
    }

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = {
            coroutineScope.launch { viewModel.getRadiologyHistory(patientId) }
        },
        modifier = Modifier.fillMaxSize()
    ) {
        when (val state = radiologyHistoryState) {
            is UiState.Success -> {
                val radiologyRecords = state.data
                if (radiologyRecords.isEmpty()) {
                    EmptyRadiologyHistoryScreen()
                } else {
                    RadiologyHistoryList(
                        radiologyRecords = radiologyRecords,
                        onEditRadiologyRecord = onEditRadiologyRecord,
                        onDeleteRadiologyRecord = { radiologyRecord ->
                            radiologyRecordToDelete = radiologyRecord
                            showDeleteDialog = true
                        },
                        onPreviewFile = { fileUrl -> context.openUrlInCustomTab(fileUrl) }
                    )
                }
            }

            is UiState.Error -> {
                ErrorRadiologyHistoryScreen(state.message)
            }

            else -> Unit
        }
    }
}

@Composable
fun EmptyRadiologyHistoryScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stringResource(R.string.radiology_history_empty),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

@Composable
fun ErrorRadiologyHistoryScreen(message: String) {
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
fun RadiologyHistoryList(
    radiologyRecords: List<Radiology>,
    onEditRadiologyRecord: (String) -> Unit,
    onDeleteRadiologyRecord: (Radiology) -> Unit,
    onPreviewFile: (String) -> Unit,
) {
    val groupedRadiologyRecords = radiologyRecords
        .sortedByDescending { it.date }
        .groupBy { it.date?.formatToString("dd/MM/yyyy") ?: "" }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        groupedRadiologyRecords.forEach { (date, recordsForDate) ->
            stickyHeader {
                DateHeader(date)
            }
            itemsIndexed(recordsForDate) { index, radiologyRecord ->
                RadiologyHistoryItem(
                    radiologyRecord = radiologyRecord,
                    onEditRadiologyRecord = onEditRadiologyRecord,
                    onDeleteRadiologyRecord = onDeleteRadiologyRecord,
                    onPreviewFile = onPreviewFile
                )

                if (index < recordsForDate.size - 1) HorizontalDivider()
            }
        }
    }
}

@Composable
fun RadiologyHistoryItem(
    radiologyRecord: Radiology,
    onEditRadiologyRecord: (String) -> Unit,
    onDeleteRadiologyRecord: (Radiology) -> Unit,
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
                        text = stringResource(R.string.radiology_history_title, radiologyRecord.title),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    if (radiologyRecord.result.isNotEmpty()) {
                        Text(
                            text = radiologyRecord.result,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }

                MoreOptionsMenu(
                    onEditClick = { onEditRadiologyRecord(radiologyRecord.id) },
                    onDeleteClick = { onDeleteRadiologyRecord(radiologyRecord) },
                    modifier = Modifier.align(Alignment.CenterVertically)
                )
            }

            if (radiologyRecord.files.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.radiology_history_files),
                    style = MaterialTheme.typography.bodyMedium
                )
                radiologyRecord.files.forEach { file ->
                    Text(
                        text = file,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.clickable { onPreviewFile(file) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}