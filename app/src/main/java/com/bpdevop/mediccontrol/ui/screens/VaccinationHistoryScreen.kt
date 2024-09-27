package com.bpdevop.mediccontrol.ui.screens

import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.bpdevop.mediccontrol.data.model.Vaccine
import com.bpdevop.mediccontrol.ui.components.MessageDialog
import com.bpdevop.mediccontrol.ui.viewmodels.VaccinationViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VaccinationHistoryScreen(
    patientId: String,
    viewModel: VaccinationViewModel = hiltViewModel(),
    onEditVaccine: (Vaccine) -> Unit,
) {
    val vaccinationHistoryState by viewModel.vaccinationHistoryState.collectAsState()
    val deleteVaccineState by viewModel.deleteVaccineState.collectAsState()
    val isRefreshing = vaccinationHistoryState is UiState.Loading
    val coroutineScope = rememberCoroutineScope()

    val context = LocalContext.current

    var vaccineToDelete by remember { mutableStateOf<Vaccine?>(null) } // Para manejar la vacuna que será eliminada
    var showDeleteDialog by remember { mutableStateOf(false) }


    LaunchedEffect(Unit) {
        viewModel.getVaccinationHistory(patientId)
    }

    // Manejar el estado de eliminación
    when (deleteVaccineState) {
        is UiState.Success -> {
            LaunchedEffect(Unit) {
                viewModel.resetDeleteVaccineState()
                viewModel.getVaccinationHistory(patientId)
            }
        }

        is UiState.Error -> {
            LaunchedEffect(Unit) {
                Toast.makeText(context, (deleteVaccineState as UiState.Error).message, Toast.LENGTH_SHORT).show()
            }
        }

        else -> Unit
    }

    // Mostrar diálogo de confirmación antes de eliminar
    if (showDeleteDialog && vaccineToDelete != null) {
        MessageDialog(
            title = stringResource(R.string.dialog_delete_vaccine_title),
            message = stringResource(R.string.dialog_delete_vaccine_message),
            dismissButtonText = stringResource(id = android.R.string.cancel),
            onConfirm = {
                viewModel.deleteVaccine(patientId, vaccineToDelete!!.id)
                showDeleteDialog = false
            },
            onDismiss = {
                showDeleteDialog = false
            }
        )
    }

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = {
            coroutineScope.launch { viewModel.getVaccinationHistory(patientId) }
        },
        modifier = Modifier.fillMaxSize()
    ) {
        when (val state = vaccinationHistoryState) {
            is UiState.Loading -> LoadingScreen()

            is UiState.Success -> {
                val vaccines = state.data
                if (vaccines.isEmpty()) {
                    EmptyVaccinationHistoryScreen()
                } else {
                    VaccinationHistoryList(
                        vaccines = vaccines,
                        onEditVaccine = onEditVaccine,
                        onDeleteVaccine = { vaccine ->
                            vaccineToDelete = vaccine
                            showDeleteDialog = true
                        }
                    )
                }
            }

            is UiState.Error -> {
                ErrorVaccinationHistoryScreen(state.message)
            }

            else -> Unit
        }
    }
}

@Composable
fun LoadingScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
fun EmptyVaccinationHistoryScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stringResource(R.string.vaccination_history_empty),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

@Composable
fun ErrorVaccinationHistoryScreen(message: String) {
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
fun VaccinationHistoryList(
    vaccines: List<Vaccine>,
    onEditVaccine: (Vaccine) -> Unit,
    onDeleteVaccine: (Vaccine) -> Unit,
) {
    val groupedVaccines = vaccines
        .sortedByDescending { it.date }
        .groupBy { it.date?.formatToString("dd/MM/yyyy") ?: "" }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        groupedVaccines.forEach { (date, vaccinesForDate) ->
            stickyHeader {
                DateHeader(date)
            }
            items(vaccinesForDate) { vaccine ->
                VaccineHistoryItem(
                    vaccine = vaccine,
                    onEditVaccine = onEditVaccine,
                    onDeleteVaccine = onDeleteVaccine
                )
            }
        }
    }
}

@Composable
fun DateHeader(date: String) {
    Text(
        text = date,
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    )
}

@Composable
fun VaccineHistoryItem(
    vaccine: Vaccine,
    onEditVaccine: (Vaccine) -> Unit,
    onDeleteVaccine: (Vaccine) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Text(text = vaccine.name, style = MaterialTheme.typography.bodyLarge)
            vaccine.date?.formatToString()?.let { Text(text = it, style = MaterialTheme.typography.bodySmall) }
            vaccine.notes?.let {
                Text(text = it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface)
            }
        }

        // Icono de 3 puntos para mostrar el menú
        Box(modifier = Modifier.align(Alignment.TopEnd)) {
            IconButton(onClick = { expanded = true }) {
                Icon(Icons.Default.MoreVert, contentDescription = null)
            }

            // Menú contextual
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.align(Alignment.TopEnd)
            ) {
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.vaccination_history_vaccine_edit)) },
                    onClick = {
                        expanded = false
                        onEditVaccine(vaccine)
                    }
                )
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.vaccination_history_vaccine_delete)) },
                    onClick = {
                        expanded = false
                        onDeleteVaccine(vaccine)
                    }
                )
            }
        }
    }
}
