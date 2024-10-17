package com.bpdevop.mediccontrol.ui.screens.export

import android.content.Context
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.People
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.text.HtmlCompat
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import com.bpdevop.mediccontrol.R
import com.bpdevop.mediccontrol.core.extensions.openPDF
import com.bpdevop.mediccontrol.core.utils.UiState
import com.bpdevop.mediccontrol.data.model.Patient
import com.bpdevop.mediccontrol.ui.components.CommonDialog
import com.bpdevop.mediccontrol.ui.components.LoadingPdfDialog
import com.bpdevop.mediccontrol.ui.screens.patient.HeaderLetter
import com.bpdevop.mediccontrol.ui.viewmodels.InfectiousPatientsExportViewModel
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InfectiousPatientsExportScreen(
    viewModel: InfectiousPatientsExportViewModel = hiltViewModel(),
) {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val infectiousPatientsState by viewModel.infectiousPatients.collectAsState()
    val pdfExportState by viewModel.pdfExportState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadInfectiousPatients()
        viewModel.loadDoctorProfile()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        PullToRefreshBox(
            isRefreshing = infectiousPatientsState is UiState.Loading,
            onRefresh = { coroutineScope.launch { viewModel.loadInfectiousPatients() } },
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                when (val state = infectiousPatientsState) {
                    is UiState.Success -> {
                        val patients = state.data
                        if (patients.isEmpty()) {
                            EmptyInfectiousPatientsScreen()
                        } else {
                            InfectiousPatientsList(patients)
                        }
                    }

                    is UiState.Error -> {
                        Text(
                            text = state.message,
                            color = MaterialTheme.colorScheme.error
                        )
                    }

                    else -> Unit
                }
            }
        }

        FloatingActionButton(
            onClick = {
                if (infectiousPatientsState is UiState.Success) {
                    viewModel.exportToPDF((infectiousPatientsState as UiState.Success).data)
                }
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(Icons.Filled.Download, contentDescription = stringResource(R.string.export_screen_fab_export))
        }

        HandlePdfExportDialog(
            pdfExportState = pdfExportState,
            onPdfOpened = { viewModel.resetPdfExportState() },
            onResetState = { viewModel.resetPdfExportState() },
            context = context
        )
    }
}

@Composable
fun HandlePdfExportDialog(
    pdfExportState: UiState<File>,
    onPdfOpened: () -> Unit,
    onResetState: () -> Unit,
    context: Context,
) {
    when (pdfExportState) {
        is UiState.Loading -> LoadingPdfDialog()

        is UiState.Success -> {
            LaunchedEffect(Unit) {
                context.openPDF(pdfExportState.data)
                onPdfOpened()
            }
        }

        is UiState.Error -> {
            CommonDialog(
                message = pdfExportState.message,
                onConfirm = onResetState
            )
        }

        else -> Unit
    }
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun InfectiousPatientsList(patients: List<Patient>) {
    val groupedPatients = patients.sortedBy { it.name }.groupBy { it.name.firstOrNull()?.uppercaseChar() ?: '#' }
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        groupedPatients.forEach { (initial, patientsForInitial) ->
            stickyHeader { HeaderLetter(letter = initial) }
            items(patientsForInitial) { patient ->
                InfectiousPatientItem(patient)
            }
        }
    }
}

@Composable
fun InfectiousPatientItem(patient: Patient) {
    val painter = rememberAsyncImagePainter(model = patient.photoUrl ?: R.drawable.ic_person_placeholder)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painter,
            contentDescription = null,
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(text = patient.name, style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(bottom = 4.dp))
            Text(
                text = HtmlCompat.fromHtml(patient.diseaseTitle ?: stringResource(R.string.export_screen_unknown_disease), HtmlCompat.FROM_HTML_MODE_LEGACY).toString(),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun EmptyInfectiousPatientsScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(imageVector = Icons.Default.People, contentDescription = null, modifier = Modifier.size(100.dp))
            Text(stringResource(R.string.export_screen_no_patients_message))
        }
    }
}
