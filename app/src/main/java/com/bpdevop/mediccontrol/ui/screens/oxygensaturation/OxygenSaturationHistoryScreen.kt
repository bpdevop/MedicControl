package com.bpdevop.mediccontrol.ui.screens.oxygensaturation

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
import com.bpdevop.mediccontrol.data.model.OxygenSaturation
import com.bpdevop.mediccontrol.ui.components.DateHeader
import com.bpdevop.mediccontrol.ui.components.MessageDialog
import com.bpdevop.mediccontrol.ui.components.MoreOptionsMenu
import com.bpdevop.mediccontrol.ui.viewmodels.OxygenSaturationViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OxygenSaturationHistoryScreen(
    patientId: String,
    viewModel: OxygenSaturationViewModel = hiltViewModel(),
    onEditOxygenSaturation: (OxygenSaturation) -> Unit,
) {
    val oxygenSaturationHistoryState by viewModel.oxygenSaturationHistoryState.collectAsState()
    val deleteOxygenSaturationState by viewModel.deleteOxygenSaturationState.collectAsState()
    val isRefreshing = oxygenSaturationHistoryState is UiState.Loading
    val coroutineScope = rememberCoroutineScope()

    val context = LocalContext.current

    var oxygenSaturationToDelete by remember { mutableStateOf<OxygenSaturation?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.getOxygenSaturationHistory(patientId)
    }

    when (deleteOxygenSaturationState) {
        is UiState.Success -> {
            LaunchedEffect(Unit) {
                viewModel.resetDeleteOxygenSaturationState()
                viewModel.getOxygenSaturationHistory(patientId)
            }
        }

        is UiState.Error -> {
            LaunchedEffect(Unit) {
                Toast.makeText(context, (deleteOxygenSaturationState as UiState.Error).message, Toast.LENGTH_SHORT).show()
            }
        }

        else -> Unit
    }

    if (showDeleteDialog && oxygenSaturationToDelete != null) {
        MessageDialog(
            title = stringResource(R.string.dialog_delete_oxygen_saturation_title),
            message = stringResource(R.string.dialog_delete_oxygen_saturation_message),
            dismissButtonText = stringResource(id = android.R.string.cancel),
            onConfirm = {
                viewModel.deleteOxygenSaturation(patientId, oxygenSaturationToDelete!!.id)
                showDeleteDialog = false
            },
            onDismiss = { showDeleteDialog = false }
        )
    }

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = {
            coroutineScope.launch { viewModel.getOxygenSaturationHistory(patientId) }
        },
        modifier = Modifier.fillMaxSize()
    ) {
        when (val state = oxygenSaturationHistoryState) {
            is UiState.Success -> {
                val oxygenSaturations = state.data
                if (oxygenSaturations.isEmpty()) {
                    EmptyOxygenSaturationHistoryScreen()
                } else {
                    OxygenSaturationHistoryList(
                        oxygenSaturations = oxygenSaturations,
                        onEditOxygenSaturation = onEditOxygenSaturation,
                        onDeleteOxygenSaturation = { oxygenSaturation ->
                            oxygenSaturationToDelete = oxygenSaturation
                            showDeleteDialog = true
                        }
                    )
                }
            }

            is UiState.Error -> {
                ErrorOxygenSaturationHistoryScreen(state.message)
            }

            else -> Unit
        }
    }
}

@Composable
fun EmptyOxygenSaturationHistoryScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stringResource(R.string.oxygen_saturation_history_empty),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

@Composable
fun ErrorOxygenSaturationHistoryScreen(message: String) {
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
fun OxygenSaturationHistoryList(
    oxygenSaturations: List<OxygenSaturation>,
    onEditOxygenSaturation: (OxygenSaturation) -> Unit,
    onDeleteOxygenSaturation: (OxygenSaturation) -> Unit,
) {
    val groupedOxygenSaturations = oxygenSaturations
        .sortedByDescending { it.date }
        .groupBy { it.date?.formatToString("dd/MM/yyyy") ?: "" }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        groupedOxygenSaturations.forEach { (date, oxygenSaturationsForDate) ->
            stickyHeader {
                DateHeader(date)
            }
            items(oxygenSaturationsForDate) { oxygenSaturation ->
                OxygenSaturationHistoryItem(
                    oxygenSaturation = oxygenSaturation,
                    onEditOxygenSaturation = onEditOxygenSaturation,
                    onDeleteOxygenSaturation = onDeleteOxygenSaturation
                )
            }
        }
    }
}

@Composable
fun OxygenSaturationHistoryItem(
    oxygenSaturation: OxygenSaturation,
    onEditOxygenSaturation: (OxygenSaturation) -> Unit,
    onDeleteOxygenSaturation: (OxygenSaturation) -> Unit,
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
                    text = stringResource(R.string.oxygen_saturation_result),
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = oxygenSaturation.saturation.toString(),
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.oxygen_saturation_pulse),
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = oxygenSaturation.pulse.toString(),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }

        MoreOptionsMenu(
            onEditClick = { onEditOxygenSaturation(oxygenSaturation) },
            onDeleteClick = { onDeleteOxygenSaturation(oxygenSaturation) },
            modifier = Modifier.align(Alignment.TopEnd)
        )
    }
}