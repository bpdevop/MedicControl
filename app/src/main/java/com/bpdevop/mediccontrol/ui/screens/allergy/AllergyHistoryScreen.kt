package com.bpdevop.mediccontrol.ui.screens.allergy

import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import com.bpdevop.mediccontrol.data.model.Allergy
import com.bpdevop.mediccontrol.ui.components.DateHeader
import com.bpdevop.mediccontrol.ui.components.MessageDialog
import com.bpdevop.mediccontrol.ui.components.MoreOptionsMenu
import com.bpdevop.mediccontrol.ui.viewmodels.AllergyViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllergyHistoryScreen(
    patientId: String,
    viewModel: AllergyViewModel = hiltViewModel(),
    onEditAllergy: (Allergy) -> Unit,
) {
    val allergyHistoryState by viewModel.allergyHistoryState.collectAsState()
    val deleteAllergyState by viewModel.deleteAllergyState.collectAsState()
    val isRefreshing = allergyHistoryState is UiState.Loading
    val coroutineScope = rememberCoroutineScope()

    val context = LocalContext.current

    var allergyToDelete by remember { mutableStateOf<Allergy?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.getAllergyHistory(patientId)
    }

    // Manejar el estado de eliminación
    when (deleteAllergyState) {
        is UiState.Success -> {
            LaunchedEffect(Unit) {
                viewModel.resetDeleteAllergyState()
                viewModel.getAllergyHistory(patientId)
            }
        }

        is UiState.Error -> {
            LaunchedEffect(Unit) {
                Toast.makeText(context, (deleteAllergyState as UiState.Error).message, Toast.LENGTH_SHORT).show()
            }
        }

        else -> Unit
    }

    // Mostrar diálogo de confirmación antes de eliminar
    if (showDeleteDialog && allergyToDelete != null) {
        MessageDialog(
            title = stringResource(R.string.dialog_delete_allergy_title),
            message = stringResource(R.string.dialog_delete_allergy_message),
            dismissButtonText = stringResource(id = android.R.string.cancel),
            onConfirm = {
                viewModel.deleteAllergy(patientId, allergyToDelete!!.id)
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
            coroutineScope.launch { viewModel.getAllergyHistory(patientId) }
        },
        modifier = Modifier.fillMaxSize()
    ) {
        when (val state = allergyHistoryState) {
            is UiState.Success -> {
                val allergies = state.data
                if (allergies.isEmpty()) {
                    EmptyAllergyHistoryScreen()
                } else {
                    AllergyHistoryList(
                        allergies = allergies,
                        onEditAllergy = onEditAllergy,
                        onDeleteAllergy = { allergy ->
                            allergyToDelete = allergy
                            showDeleteDialog = true
                        }
                    )
                }
            }

            is UiState.Error -> {
                ErrorAllergyHistoryScreen(state.message)
            }

            else -> Unit
        }
    }
}

@Composable
fun EmptyAllergyHistoryScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stringResource(R.string.allergy_history_empty),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

@Composable
fun ErrorAllergyHistoryScreen(message: String) {
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
fun AllergyHistoryList(
    allergies: List<Allergy>,
    onEditAllergy: (Allergy) -> Unit,
    onDeleteAllergy: (Allergy) -> Unit,
) {
    val groupedAllergies = allergies
        .sortedByDescending { it.date }
        .groupBy { it.date?.formatToString("dd/MM/yyyy") ?: "" }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        groupedAllergies.forEach { (date, allergiesForDate) ->
            stickyHeader {
                DateHeader(date)
            }
            items(allergiesForDate) { allergy ->
                AllergyHistoryItem(
                    allergy = allergy,
                    onEditAllergy = onEditAllergy,
                    onDeleteAllergy = onDeleteAllergy
                )
            }
        }
    }
}

@Composable
fun AllergyHistoryItem(
    allergy: Allergy,
    onEditAllergy: (Allergy) -> Unit,
    onDeleteAllergy: (Allergy) -> Unit,
) {
    Box(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Text(text = allergy.description, style = MaterialTheme.typography.bodyLarge)
            allergy.date?.formatToString()?.let { Text(text = it, style = MaterialTheme.typography.bodySmall) }
        }

        MoreOptionsMenu(
            onEditClick = { onEditAllergy(allergy) },
            onDeleteClick = { onDeleteAllergy(allergy) },
            modifier = Modifier.align(Alignment.TopEnd)
        )
    }
}
