package com.bpdevop.mediccontrol.ui.screens.prescription

import android.content.Context
import android.content.Intent
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
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import com.bpdevop.mediccontrol.BuildConfig
import com.bpdevop.mediccontrol.R
import com.bpdevop.mediccontrol.core.extensions.formatToString
import com.bpdevop.mediccontrol.core.extensions.generatePrescriptionPdf
import com.bpdevop.mediccontrol.core.utils.UiState
import com.bpdevop.mediccontrol.data.model.Prescription
import com.bpdevop.mediccontrol.ui.components.DateHeader
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
    val isRefreshing = prescriptionHistoryState is UiState.Loading
    val coroutineScope = rememberCoroutineScope()

    val context = LocalContext.current

    var prescriptionToDelete by remember { mutableStateOf<Prescription?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.getPrescriptionHistory(patientId)
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
        isRefreshing = isRefreshing,
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
                        context = context,
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
    context: Context,
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
                    context = context,
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
    context: Context,
    viewModel: PrescriptionViewModel, // ViewModel para obtener doctor y paciente
    patientId: String, // ID del paciente
) {
    val doctorProfileState by viewModel.doctorProfileState.collectAsState()
    val patientState by viewModel.patientState.collectAsState()

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
                    prescription.medications?.forEach { item ->
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
                        viewModel.getDoctorProfile()
                        viewModel.getPatientById(patientId)

                        if (doctorProfileState is UiState.Loading || patientState is UiState.Loading) {
                            Toast.makeText(context, "generando documento", Toast.LENGTH_SHORT).show()
                        } else if (doctorProfileState is UiState.Success && patientState is UiState.Success) {
                            val doctorProfile = (doctorProfileState as UiState.Success).data
                            val patient = (patientState as UiState.Success).data

                            if (doctorProfile != null && patient != null) {
                                val pdfFile = context.generatePrescriptionPdf(
                                    doctorProfile,
                                    patient,
                                    prescription
                                )
                                printPrescription(context, pdfFile)
                            } else {
                                Toast.makeText(context, "Doctor profile or patient data is missing", Toast.LENGTH_SHORT).show()
                            }
                        } else if (doctorProfileState is UiState.Error || patientState is UiState.Error) {
                            Toast.makeText(context, "Error loading data", Toast.LENGTH_SHORT).show()
                        }
                    },
                    onSendClick = {
                        viewModel.getDoctorProfile()
                        viewModel.getPatientById(patientId)

                        if (doctorProfileState is UiState.Loading || patientState is UiState.Loading) {
                            Toast.makeText(context, "generando documento", Toast.LENGTH_SHORT).show()
                        } else if (doctorProfileState is UiState.Success && patientState is UiState.Success) {
                            val doctorProfile = (doctorProfileState as UiState.Success).data
                            val patient = (patientState as UiState.Success).data

                            if (doctorProfile != null && patient != null) {
                                val pdfFile = context.generatePrescriptionPdf(
                                    doctorProfile,
                                    patient,
                                    prescription
                                )
                                sendPrescription(context, pdfFile)
                            } else {
                                Toast.makeText(context, "Doctor profile or patient data is missing", Toast.LENGTH_SHORT).show()
                            }
                        } else if (doctorProfileState is UiState.Error || patientState is UiState.Error) {
                            Toast.makeText(context, "Error loading data", Toast.LENGTH_SHORT).show()
                        }
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
                    text = prescription.notes ?: "",
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


fun printPrescription(context: Context, file: File) {
    val pdfUri = FileProvider.getUriForFile(context, "${BuildConfig.APPLICATION_ID}.provider", file)
    val intent = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(pdfUri, "application/pdf")
        flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
    }
    context.startActivity(intent)
}

fun sendPrescription(context: Context, file: File) {
    val pdfUri = FileProvider.getUriForFile(context, "${BuildConfig.APPLICATION_ID}.provider", file)
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "application/pdf"
        putExtra(Intent.EXTRA_STREAM, pdfUri)
        flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
    }
    context.startActivity(Intent.createChooser(intent, "Send Prescription"))
}