package com.bpdevop.mediccontrol.ui.screens.prescription

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
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
import com.bpdevop.mediccontrol.core.extensions.openPDF
import com.bpdevop.mediccontrol.core.extensions.sendPrescription
import com.bpdevop.mediccontrol.core.utils.UiState
import com.bpdevop.mediccontrol.data.model.Prescription
import com.bpdevop.mediccontrol.ui.components.CommonDialog
import com.bpdevop.mediccontrol.ui.components.DateHeader
import com.bpdevop.mediccontrol.ui.components.LoadingPdfDialog
import com.bpdevop.mediccontrol.ui.components.MessageDialog
import com.bpdevop.mediccontrol.ui.components.MoreOptionsMenu
import com.bpdevop.mediccontrol.ui.viewmodels.PrescriptionViewModel
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrescriptionHistoryScreen(
    patientId: String,
    viewModel: PrescriptionViewModel = hiltViewModel(),
    onEditPrescription: (String) -> Unit,
) {
    val prescriptionHistoryState by viewModel.prescriptionHistoryState.collectAsState()
    val deletePrescriptionState by viewModel.deletePrescriptionState.collectAsState()
    val pdfExportState by viewModel.pdfExportState.collectAsState()
    val shouldOpenPdf by viewModel.shouldOpenPdf.collectAsState()

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var prescriptionToDelete by remember { mutableStateOf<Prescription?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.getPrescriptionHistory(patientId)
        viewModel.getDoctorProfile()
    }

    when (deletePrescriptionState) {
        is UiState.Success -> {
            LaunchedEffect(Unit) {
                viewModel.resetDeletePrescriptionState()
                viewModel.getPrescriptionHistory(patientId)
            }
        }

        is UiState.Error -> {
            LaunchedEffect(Unit) {
                Toast.makeText(context, (deletePrescriptionState as UiState.Error).message, Toast.LENGTH_SHORT).show()
            }
        }

        else -> Unit
    }

    if (showDeleteDialog && prescriptionToDelete != null) {
        MessageDialog(
            title = stringResource(R.string.dialog_delete_prescription_title),
            message = stringResource(R.string.dialog_delete_prescription_message),
            dismissButtonText = stringResource(id = android.R.string.cancel),
            onConfirm = {
                viewModel.deletePrescription(patientId, prescriptionToDelete!!.id)
                showDeleteDialog = false
            },
            onDismiss = { showDeleteDialog = false }
        )
    }

    PullToRefreshBox(
        isRefreshing = prescriptionHistoryState is UiState.Loading,
        onRefresh = {
            coroutineScope.launch { viewModel.getPrescriptionHistory(patientId) }
        },
        modifier = Modifier.fillMaxSize()
    ) {
        when (val state = prescriptionHistoryState) {
            is UiState.Success -> {
                val prescriptions = state.data
                if (prescriptions.isEmpty()) {
                    EmptyPrescriptionHistoryScreen()
                } else {
                    PrescriptionHistoryList(
                        prescriptions = prescriptions,
                        onEditPrescription = onEditPrescription,
                        onDeletePrescription = { prescription ->
                            prescriptionToDelete = prescription
                            showDeleteDialog = true
                        },
                        viewModel = viewModel,
                        patientId = patientId
                    )
                }
            }

            is UiState.Error -> {
                ErrorPrescriptionHistoryScreen(state.message)
            }

            else -> Unit
        }
    }

    HandlePrescriptionPdfExportDialog(
        pdfExportState = pdfExportState,
        onPdfOpened = { viewModel.resetPdfExportState() },
        onResetState = { viewModel.resetPdfExportState() },
        context = context,
        shouldOpenPdf = shouldOpenPdf
    )
}

@Composable
fun EmptyPrescriptionHistoryScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stringResource(R.string.prescription_history_empty),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

@Composable
fun ErrorPrescriptionHistoryScreen(message: String) {
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
fun PrescriptionHistoryList(
    prescriptions: List<Prescription>,
    onEditPrescription: (String) -> Unit,
    onDeletePrescription: (Prescription) -> Unit,
    viewModel: PrescriptionViewModel,
    patientId: String,
) {
    val groupedPrescriptions = prescriptions
        .sortedByDescending { it.date }
        .groupBy { it.date?.formatToString("dd/MM/yyyy") ?: "" }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        groupedPrescriptions.forEach { (date, prescriptionsForDate) ->
            stickyHeader {
                DateHeader(date)
            }
            itemsIndexed(prescriptionsForDate) { index, prescription ->
                PrescriptionHistoryItem(
                    prescription = prescription,
                    onEditPrescription = onEditPrescription,
                    onDeletePrescription = onDeletePrescription,
                    viewModel = viewModel,
                    patientId = patientId
                )

                if (index < prescriptionsForDate.size - 1) HorizontalDivider()
            }
        }
    }
}


@Composable
fun PrescriptionHistoryItem(
    prescription: Prescription,
    onEditPrescription: (String) -> Unit,
    onDeletePrescription: (Prescription) -> Unit,
    viewModel: PrescriptionViewModel,
    patientId: String,
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
                        text = stringResource(R.string.prescription_history_medications),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    prescription.medications.forEach { item ->
                        Text(
                            text = "${item.name}: ${item.dosage}, ${item.frequency}, ${item.duration}",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }

                MoreOptionsMenu(
                    onEditClick = { onEditPrescription(prescription.id) },
                    onDeleteClick = { onDeletePrescription(prescription) },
                    onPrintClick = {
                        viewModel.setShouldOpenPdf(true)
                        viewModel.exportPrescriptionToPDF(patientId, prescription)
                    },
                    onSendClick = {
                        viewModel.setShouldOpenPdf(false)
                        viewModel.exportPrescriptionToPDF(patientId, prescription)
                    },
                    modifier = Modifier.align(Alignment.CenterVertically)
                )
            }

            if (!prescription.notes.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.prescription_history_notes),
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = prescription.notes,
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            // Fecha de la receta
            prescription.date?.let {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.prescription_history_date),
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = it.formatToString("dd/MM/yyyy"),
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun HandlePrescriptionPdfExportDialog(
    pdfExportState: UiState<File>,
    onPdfOpened: () -> Unit,
    onResetState: () -> Unit,
    context: Context,
    shouldOpenPdf: Boolean,
) {
    when (pdfExportState) {
        is UiState.Loading -> LoadingPdfDialog()

        is UiState.Success -> {
            if (shouldOpenPdf) {
                context.openPDF(pdfExportState.data)
                Log.e("PrescriptionHistoryScreen", "pase acá ")
            } else {
                context.sendPrescription(pdfExportState.data)
            }
            onPdfOpened()
            onResetState()
        }

        is UiState.Error -> {
            CommonDialog(
                message = pdfExportState.message,
                onConfirm = {
                    onResetState()
                }
            )
        }

        else -> Unit
    }
}
