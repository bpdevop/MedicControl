package com.bpdevop.mediccontrol.ui.screens.bloodpressure

import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
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
import com.bpdevop.mediccontrol.core.utils.UiState
import com.bpdevop.mediccontrol.data.model.BloodPressure
import com.bpdevop.mediccontrol.ui.components.DateHeader
import com.bpdevop.mediccontrol.ui.components.MessageDialog
import com.bpdevop.mediccontrol.ui.components.MoreOptionsMenu
import com.bpdevop.mediccontrol.ui.components.RefreshLoadingScreen
import com.bpdevop.mediccontrol.ui.viewmodels.BloodPressureViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BloodPressureHistoryScreen(
    patientId: String,
    viewModel: BloodPressureViewModel = hiltViewModel(),
    onEditBloodPressure: (BloodPressure) -> Unit,
) {
    val bloodPressureHistoryState by viewModel.bloodPressureHistoryState.collectAsState()
    val deleteBloodPressureState by viewModel.deleteBloodPressureState.collectAsState()
    val isRefreshing = bloodPressureHistoryState is UiState.Loading
    val coroutineScope = rememberCoroutineScope()

    val context = LocalContext.current

    var bloodPressureToDelete by remember { mutableStateOf<BloodPressure?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.getBloodPressureHistory(patientId)
    }

    when (deleteBloodPressureState) {
        is UiState.Success -> {
            LaunchedEffect(Unit) {
                viewModel.resetDeleteBloodPressureState()
                viewModel.getBloodPressureHistory(patientId)
            }
        }

        is UiState.Error -> {
            LaunchedEffect(Unit) {
                Toast.makeText(context, (deleteBloodPressureState as UiState.Error).message, Toast.LENGTH_SHORT).show()
            }
        }

        else -> Unit
    }

    if (showDeleteDialog && bloodPressureToDelete != null) {
        MessageDialog(
            title = stringResource(R.string.dialog_delete_blood_pressure_title),
            message = stringResource(R.string.dialog_delete_blood_pressure_message),
            dismissButtonText = stringResource(id = android.R.string.cancel),
            onConfirm = {
                viewModel.deleteBloodPressure(patientId, bloodPressureToDelete!!.id)
                showDeleteDialog = false
            },
            onDismiss = { showDeleteDialog = false }
        )
    }

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = {
            coroutineScope.launch { viewModel.getBloodPressureHistory(patientId) }
        },
        modifier = Modifier.fillMaxSize()
    ) {
        when (val state = bloodPressureHistoryState) {
            is UiState.Loading -> RefreshLoadingScreen()

            is UiState.Success -> {
                val bloodPressures = state.data
                if (bloodPressures.isEmpty()) {
                    EmptyBloodPressureHistoryScreen()
                } else {
                    BloodPressureHistoryList(
                        bloodPressures = bloodPressures,
                        onEditBloodPressure = onEditBloodPressure,
                        onDeleteBloodPressure = { bloodPressure ->
                            bloodPressureToDelete = bloodPressure
                            showDeleteDialog = true
                        }
                    )
                }
            }

            is UiState.Error -> {
                ErrorBloodPressureHistoryScreen(state.message)
            }

            else -> Unit
        }
    }
}

@Composable
fun EmptyBloodPressureHistoryScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stringResource(R.string.blood_pressure_history_empty),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

@Composable
fun ErrorBloodPressureHistoryScreen(message: String) {
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
fun BloodPressureHistoryList(
    bloodPressures: List<BloodPressure>,
    onEditBloodPressure: (BloodPressure) -> Unit,
    onDeleteBloodPressure: (BloodPressure) -> Unit,
) {
    val groupedBloodPressures = bloodPressures
        .sortedByDescending { it.date }
        .groupBy { it.date?.formatToString("dd/MM/yyyy") ?: "" }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        groupedBloodPressures.forEach { (date, bloodPressuresForDate) ->
            stickyHeader {
                DateHeader(date)
            }
            items(bloodPressuresForDate) { bloodPressure ->
                BloodPressureHistoryItem(
                    bloodPressure = bloodPressure,
                    onEditBloodPressure = onEditBloodPressure,
                    onDeleteBloodPressure = onDeleteBloodPressure
                )
            }
        }
    }
}

@Composable
fun BloodPressureHistoryItem(
    bloodPressure: BloodPressure,
    onEditBloodPressure: (BloodPressure) -> Unit,
    onDeleteBloodPressure: (BloodPressure) -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp, 8.dp)
    ) {
        Row(
            modifier = Modifier.align(Alignment.CenterStart),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.blood_pressure_history_systolic),
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = bloodPressure.systolic.toString(),
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.blood_pressure_history_diastolic),
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = bloodPressure.diastolic.toString(),
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.blood_pressure_history_pulse),
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = bloodPressure.pulse.toString(),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }

        MoreOptionsMenu(
            onEditClick = { onEditBloodPressure(bloodPressure) },
            onDeleteClick = { onDeleteBloodPressure(bloodPressure) },
            modifier = Modifier.align(Alignment.TopEnd)
        )
    }
}
