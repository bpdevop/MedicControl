package com.bpdevop.mediccontrol.ui.screens.bloodglucose

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
import com.bpdevop.mediccontrol.data.model.BloodGlucose
import com.bpdevop.mediccontrol.data.model.BloodGlucoseType
import com.bpdevop.mediccontrol.ui.components.DateHeader
import com.bpdevop.mediccontrol.ui.components.MessageDialog
import com.bpdevop.mediccontrol.ui.components.MoreOptionsMenu
import com.bpdevop.mediccontrol.ui.viewmodels.BloodGlucoseViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BloodGlucoseHistoryScreen(
    patientId: String,
    viewModel: BloodGlucoseViewModel = hiltViewModel(),
    onEditBloodGlucose: (BloodGlucose) -> Unit,
) {
    val bloodGlucoseHistoryState by viewModel.bloodGlucoseHistoryState.collectAsState()
    val deleteBloodGlucoseState by viewModel.deleteBloodGlucoseState.collectAsState()
    val isRefreshing = bloodGlucoseHistoryState is UiState.Loading
    val coroutineScope = rememberCoroutineScope()

    val context = LocalContext.current

    var bloodGlucoseToDelete by remember { mutableStateOf<BloodGlucose?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.getBloodGlucoseHistory(patientId)
    }

    when (deleteBloodGlucoseState) {
        is UiState.Success -> {
            LaunchedEffect(Unit) {
                viewModel.resetDeleteBloodGlucoseState()
                viewModel.getBloodGlucoseHistory(patientId)
            }
        }

        is UiState.Error -> {
            LaunchedEffect(Unit) {
                Toast.makeText(context, (deleteBloodGlucoseState as UiState.Error).message, Toast.LENGTH_SHORT).show()
            }
        }

        else -> Unit
    }

    if (showDeleteDialog && bloodGlucoseToDelete != null) {
        MessageDialog(
            title = stringResource(R.string.dialog_delete_blood_glucose_title),
            message = stringResource(R.string.dialog_delete_blood_glucose_message),
            dismissButtonText = stringResource(id = android.R.string.cancel),
            onConfirm = {
                viewModel.deleteBloodGlucose(patientId, bloodGlucoseToDelete!!.id)
                showDeleteDialog = false
            },
            onDismiss = { showDeleteDialog = false }
        )
    }

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = {
            coroutineScope.launch { viewModel.getBloodGlucoseHistory(patientId) }
        },
        modifier = Modifier.fillMaxSize()
    ) {
        when (val state = bloodGlucoseHistoryState) {

            is UiState.Success -> {
                val bloodGlucoses = state.data
                if (bloodGlucoses.isEmpty()) {
                    EmptyBloodGlucoseHistoryScreen()
                } else {
                    BloodGlucoseHistoryList(
                        bloodGlucoses = bloodGlucoses,
                        onEditBloodGlucose = onEditBloodGlucose,
                        onDeleteBloodGlucose = { bloodGlucose ->
                            bloodGlucoseToDelete = bloodGlucose
                            showDeleteDialog = true
                        }
                    )
                }
            }

            is UiState.Error -> {
                ErrorBloodGlucoseHistoryScreen(state.message)
            }

            else -> Unit
        }
    }
}

@Composable
fun EmptyBloodGlucoseHistoryScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stringResource(R.string.blood_glucose_history_empty),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

@Composable
fun ErrorBloodGlucoseHistoryScreen(message: String) {
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
fun BloodGlucoseHistoryList(
    bloodGlucoses: List<BloodGlucose>,
    onEditBloodGlucose: (BloodGlucose) -> Unit,
    onDeleteBloodGlucose: (BloodGlucose) -> Unit,
) {
    val groupedBloodGlucoses = bloodGlucoses
        .sortedByDescending { it.date }
        .groupBy { it.date?.formatToString("dd/MM/yyyy") ?: "" }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        groupedBloodGlucoses.forEach { (date, bloodGlucosesForDate) ->
            stickyHeader {
                DateHeader(date)
            }
            items(bloodGlucosesForDate) { bloodGlucose ->
                BloodGlucoseHistoryItem(
                    bloodGlucose = bloodGlucose,
                    onEditBloodGlucose = onEditBloodGlucose,
                    onDeleteBloodGlucose = onDeleteBloodGlucose
                )
            }
        }
    }
}

@Composable
fun BloodGlucoseHistoryItem(
    bloodGlucose: BloodGlucose,
    onEditBloodGlucose: (BloodGlucose) -> Unit,
    onDeleteBloodGlucose: (BloodGlucose) -> Unit,
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
                    text = stringResource(R.string.blood_glucose_history_type),
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = when (bloodGlucose.type) {
                        BloodGlucoseType.FASTING -> stringResource(R.string.blood_glucose_type_fasting)
                        BloodGlucoseType.POSTPRANDIAL -> stringResource(R.string.blood_glucose_type_postprandial)
                        BloodGlucoseType.RANDOM -> stringResource(R.string.blood_glucose_type_random)
                    },
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.blood_glucose_history_result),
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = bloodGlucose.result.toString(),
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.blood_glucose_history_unit),
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = bloodGlucose.unit,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }

        MoreOptionsMenu(
            onEditClick = { onEditBloodGlucose(bloodGlucose) },
            onDeleteClick = { onDeleteBloodGlucose(bloodGlucose) },
            modifier = Modifier.align(Alignment.TopEnd)
        )
    }
}
