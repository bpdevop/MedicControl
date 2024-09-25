package com.bpdevop.mediccontrol.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.People
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import com.bpdevop.mediccontrol.R
import com.bpdevop.mediccontrol.core.utils.UiState
import com.bpdevop.mediccontrol.data.model.Patient
import com.bpdevop.mediccontrol.ui.viewmodels.PatientsViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatientsScreen(
    viewModel: PatientsViewModel = hiltViewModel(),
    onPatientClick: (String) -> Unit,
    onAddPatientClick: () -> Unit,
) {
    val patientsState by viewModel.patientsState.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        viewModel.refreshPatients() // Refresca los pacientes al entrar
    }

    Box(modifier = Modifier.fillMaxSize()) {
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = { coroutineScope.launch { viewModel.refreshPatients() } },
            modifier = Modifier.fillMaxSize()
        ) {
            when (val state = patientsState) {
                is UiState.Loading -> LoadingPatientsScreen()
                is UiState.Success -> {
                    val patients = state.data
                    if (patients.isEmpty()) EmptyPatientsScreen() else PatientsList(patients, onPatientClick)
                }

                is UiState.Error -> ErrorPatientsScreen(state.message)
                else -> Unit
            }
        }

        FloatingActionButton(
            onClick = onAddPatientClick,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = stringResource(R.string.patients_screen_fab_add_patient))
        }
    }
}

@Composable
fun LoadingPatientsScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = stringResource(R.string.patients_screen_loading))
    }
}

@Composable
fun EmptyPatientsScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(imageVector = Icons.Default.People, contentDescription = null, modifier = Modifier.size(100.dp))
            Text(stringResource(R.string.patients_screen_no_patients_message))
        }
    }
}

@Composable
fun ErrorPatientsScreen(errorMessage: String?) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            text = errorMessage ?: stringResource(R.string.patients_screen_error_loading),
            color = MaterialTheme.colorScheme.error
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PatientsList(patients: List<Patient>, onPatientClick: (String) -> Unit) {
    val groupedPatients = patients.sortedBy { it.name }.groupBy { it.name.firstOrNull()?.uppercaseChar() ?: '#' }

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        groupedPatients.forEach { (initial, patientsForInitial) ->
            stickyHeader { HeaderLetter(letter = initial) }
            items(patientsForInitial) { patient -> PatientItem(patient, onPatientClick) }
        }
    }
}

@Composable
fun HeaderLetter(letter: Char) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, top = 8.dp, bottom = 8.dp)
            .height(40.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = letter.toString(),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
        Spacer(modifier = Modifier.width(8.dp))  // Espacio opcional entre la letra y otros elementos
    }
}

@Composable
fun PatientItem(patient: Patient, onClick: (String) -> Unit) {
    val painter = rememberAsyncImagePainter(model = patient.photoUrl ?: R.drawable.ic_person_placeholder)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable { onClick(patient.id) }
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
            Text(text = patient.phone ?: stringResource(R.string.patients_screen_no_phone), style = MaterialTheme.typography.bodyMedium)
        }
    }
}