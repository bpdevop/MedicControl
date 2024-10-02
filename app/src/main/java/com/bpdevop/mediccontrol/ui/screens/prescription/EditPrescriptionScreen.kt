package com.bpdevop.mediccontrol.ui.screens.prescription

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import com.bpdevop.mediccontrol.ui.viewmodels.PrescriptionViewModel

@Composable
fun EditPrescriptionScreen(
    patientId: String,
    prescriptionId: String,
    viewModel: PrescriptionViewModel = hiltViewModel(),
    onPrescriptionUpdated: () -> Unit,
) {
    val prescriptionState by viewModel.prescriptionState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.getPrescriptionById(patientId, prescriptionId)
    }

}