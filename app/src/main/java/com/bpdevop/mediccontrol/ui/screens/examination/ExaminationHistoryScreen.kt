package com.bpdevop.mediccontrol.ui.screens.examination

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
import com.bpdevop.mediccontrol.data.model.Examination
import com.bpdevop.mediccontrol.ui.components.DateHeader
import com.bpdevop.mediccontrol.ui.components.MessageDialog
import com.bpdevop.mediccontrol.ui.components.MoreOptionsMenu
import com.bpdevop.mediccontrol.ui.components.RefreshLoadingScreen
import com.bpdevop.mediccontrol.ui.viewmodels.ExaminationViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExaminationHistoryScreen(
    patientId: String,
    viewModel: ExaminationViewModel = hiltViewModel(),
    onEditExamination: (String) -> Unit,
) {
    val examinationHistoryState by viewModel.examinationHistoryState.collectAsState()
    val deleteExaminationState by viewModel.deleteExaminationState.collectAsState()
    val isRefreshing = examinationHistoryState is UiState.Loading
    val coroutineScope = rememberCoroutineScope()

    val context = LocalContext.current

    var examinationToDelete by remember { mutableStateOf<Examination?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.getExaminationHistory(patientId)
    }

    when (deleteExaminationState) {
        is UiState.Success -> {
            LaunchedEffect(Unit) {
                viewModel.resetDeleteExaminationState()
                viewModel.getExaminationHistory(patientId)
            }
        }

        is UiState.Error -> {
            LaunchedEffect(Unit) {
                Toast.makeText(context, (deleteExaminationState as UiState.Error).message, Toast.LENGTH_SHORT).show()
            }
        }

        else -> Unit
    }

    if (showDeleteDialog && examinationToDelete != null) {
        MessageDialog(
            title = stringResource(R.string.dialog_delete_examination_title),
            message = stringResource(R.string.dialog_delete_examination_message),
            dismissButtonText = stringResource(id = android.R.string.cancel),
            onConfirm = {
                viewModel.deleteExamination(patientId, examinationToDelete!!.id)
                showDeleteDialog = false
            },
            onDismiss = { showDeleteDialog = false }
        )
    }

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = {
            coroutineScope.launch { viewModel.getExaminationHistory(patientId) }
        },
        modifier = Modifier.fillMaxSize()
    ) {
        when (val state = examinationHistoryState) {
            is UiState.Loading -> RefreshLoadingScreen()

            is UiState.Success -> {
                val examinations = state.data
                if (examinations.isEmpty()) {
                    EmptyExaminationHistoryScreen()
                } else {
                    ExaminationHistoryList(
                        examinations = examinations,
                        onEditExamination = onEditExamination,
                        onDeleteExamination = { examination ->
                            examinationToDelete = examination
                            showDeleteDialog = true
                        },
                        onPreviewFile = { fileUrl -> context.openUrlInCustomTab(fileUrl) }
                    )
                }
            }

            is UiState.Error -> {
                ErrorExaminationHistoryScreen(state.message)
            }

            else -> Unit
        }
    }
}

@Composable
fun EmptyExaminationHistoryScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stringResource(R.string.examination_history_empty),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

@Composable
fun ErrorExaminationHistoryScreen(message: String) {
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
fun ExaminationHistoryList(
    examinations: List<Examination>,
    onEditExamination: (String) -> Unit,
    onDeleteExamination: (Examination) -> Unit,
    onPreviewFile: (String) -> Unit,
) {
    val groupedExaminations = examinations
        .sortedByDescending { it.date }
        .groupBy { it.date?.formatToString("dd/MM/yyyy") ?: "" }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        groupedExaminations.forEach { (date, examinationsForDate) ->
            stickyHeader {
                DateHeader(date)
            }
            itemsIndexed(examinationsForDate) { index, examination ->
                ExaminationHistoryItem(
                    examination = examination,
                    onEditExamination = onEditExamination,
                    onDeleteExamination = onDeleteExamination,
                    onPreviewFile = onPreviewFile
                )

                if (index < examinationsForDate.size - 1) HorizontalDivider()
            }
        }
    }
}

@Composable
fun ExaminationHistoryItem(
    examination: Examination,
    onEditExamination: (String) -> Unit,
    onDeleteExamination: (Examination) -> Unit,
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
                        text = stringResource(R.string.examination_history_temperature),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = examination.temperature?.toString() ?: "N/A",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.examination_history_weight),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = examination.weight?.toString() ?: "N/A",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.examination_history_height),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = examination.height?.toString() ?: "N/A",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }

                MoreOptionsMenu(
                    onEditClick = { onEditExamination(examination.id) },
                    onDeleteClick = { onDeleteExamination(examination) },
                    modifier = Modifier.align(Alignment.CenterVertically)
                )
            }

            // Lista de síntomas
            if (examination.symptoms.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.examination_history_symptoms),
                    style = MaterialTheme.typography.bodyMedium
                )
                examination.symptoms.forEach { symptom ->
                    Text(
                        text = symptom,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }

            // Lista de diagnósticos
            if (examination.diagnosis.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.examination_history_diagnosis),
                    style = MaterialTheme.typography.bodyMedium
                )
                examination.diagnosis.forEach { diagnosis ->
                    Text(
                        text = diagnosis,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }

            // Archivos adjuntos
            if (examination.files.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.examination_history_files),
                    style = MaterialTheme.typography.bodyMedium
                )
                examination.files.forEach { file ->
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