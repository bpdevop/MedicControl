package com.bpdevop.mediccontrol.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.bpdevop.mediccontrol.R
import com.bpdevop.mediccontrol.data.model.Allergy
import com.bpdevop.mediccontrol.data.model.BloodGlucose
import com.bpdevop.mediccontrol.data.model.BloodPressure
import com.bpdevop.mediccontrol.data.model.LabTestItem
import com.bpdevop.mediccontrol.data.model.OxygenSaturation
import com.bpdevop.mediccontrol.data.model.PrescriptionItem
import com.bpdevop.mediccontrol.ui.icons.Allergies
import com.bpdevop.mediccontrol.ui.icons.BloodPressure
import com.bpdevop.mediccontrol.ui.icons.Glucose
import com.bpdevop.mediccontrol.ui.icons.Labs
import com.bpdevop.mediccontrol.ui.icons.OxygenSaturation
import com.bpdevop.mediccontrol.ui.viewmodels.PatientMedicalHistoryViewModel

@Composable
fun PatientMedicalHistoryScreen(
    patientId: String,
    viewModel: PatientMedicalHistoryViewModel = hiltViewModel(),
) {
    val labTestSummary by viewModel.labTestSummary.collectAsState()
    val allergies by viewModel.allergies.collectAsState()
    val prescriptionSummary by viewModel.prescriptionSummary.collectAsState()
    val lastBloodPressure by viewModel.lastBloodPressure.collectAsState()
    val lastBloodGlucose by viewModel.lastBloodGlucose.collectAsState()
    val lastOxygenSaturation by viewModel.lastOxygenSaturation.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Dashboard resumido
        MedicalDashboard(
            lastBloodPressure = lastBloodPressure,
            lastBloodGlucose = lastBloodGlucose,
            lastOxygenSaturation = lastOxygenSaturation
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Sección de historial médico: Solo laboratorios
        ExpandableSection(
            title = stringResource(id = R.string.patient_option_labs),
            icon = Labs,
            items = labTestSummary,  // Pasamos los LabTestItem
            content = { labTestItem: LabTestItem ->  // Aquí esperamos LabTestItem
                Text(
                    text = "${labTestItem.name}: ${labTestItem.result} (Normal: ${labTestItem.isNormal})",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        )

        Spacer(modifier = Modifier.height(16.dp))
        // Sección de historial médico: Alergias
        ExpandableSection(
            title = stringResource(id = R.string.patient_option_allergies),
            icon = Allergies,
            items = allergies,
            content = { allergy: Allergy ->
                Text(text = allergy.description, style = MaterialTheme.typography.bodyMedium)
            }
        )
        Spacer(modifier = Modifier.height(16.dp))

        ExpandableSection(
            title = stringResource(id = R.string.patient_option_prescriptions),
            icon = Icons.Filled.Receipt,
            items = prescriptionSummary,
            content = { prescriptionItem: PrescriptionItem ->
                Text(
                    text = "${prescriptionItem.name}: ${prescriptionItem.dosage}, ${prescriptionItem.frequency}, ${prescriptionItem.duration}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        )
    }

    LaunchedEffect(Unit) {
        viewModel.loadMedicalHistory(patientId)
    }
}

@Composable
fun MedicalDashboard(
    lastBloodPressure: BloodPressure?,
    lastBloodGlucose: BloodGlucose?,
    lastOxygenSaturation: OxygenSaturation?,
) {
    val bloodPressure = lastBloodPressure?.let { "${it.systolic}/${it.diastolic} mmHg" } ?: "Sin datos"
    val glucose = lastBloodGlucose?.let { "${it.result} ${it.unit}" } ?: "Sin datos" // Se usa result y unit
    val oxygenSaturation = lastOxygenSaturation?.let { "${it.saturation}% (Pulso: ${it.pulse})" } ?: "Sin datos"

    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
    ) {
        MedicalInfoCard(
            title = stringResource(id = R.string.patient_option_blood_pressure),
            value = bloodPressure,
            icon = BloodPressure,
            modifier = Modifier.weight(1f)
        )
        MedicalInfoCard(
            title = stringResource(id = R.string.patient_option_glucose),
            value = glucose,
            icon = Glucose,
            modifier = Modifier.weight(1f)
        )
        MedicalInfoCard(
            title = stringResource(id = R.string.patient_option_oxygen_saturation),
            value = oxygenSaturation,
            icon = OxygenSaturation,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun MedicalInfoCard(title: String, value: String, icon: ImageVector, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .padding(4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(32.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = title, style = MaterialTheme.typography.labelSmall)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = value, style = MaterialTheme.typography.titleMedium)
        }
    }
}

@Composable
fun <T> ExpandableSection(
    title: String,
    icon: ImageVector,
    items: List<T>,
    content: @Composable (T) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = title, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.weight(1f))
            Icon(if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore, contentDescription = null)
        }

        if (expanded) {
            items.forEach { item ->
                content(item)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}