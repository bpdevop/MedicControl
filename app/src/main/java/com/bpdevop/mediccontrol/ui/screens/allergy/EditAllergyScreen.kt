package com.bpdevop.mediccontrol.ui.screens.allergy

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
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
import com.bpdevop.mediccontrol.ui.components.DatePickerModal
import com.bpdevop.mediccontrol.ui.viewmodels.AllergyViewModel
import kotlinx.coroutines.launch
import java.util.Date

@Composable
fun EditAllergyScreen(
    patientId: String,
    allergy: Allergy,
    viewModel: AllergyViewModel = hiltViewModel(),
    onAllergyUpdated: () -> Unit,
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var allergyDescription by remember { mutableStateOf(allergy.description) }
    var allergyDate by remember { mutableStateOf(allergy.date) }
    var showDatePicker by remember { mutableStateOf(false) }

    val editAllergyState by viewModel.updateAllergyState.collectAsState()

    when (editAllergyState) {
        is UiState.Success -> {
            LaunchedEffect(Unit) {
                onAllergyUpdated()
                viewModel.resetUpdateAllergyState()
            }
        }

        is UiState.Error -> {
            LaunchedEffect(Unit) {
                Toast.makeText(context, (editAllergyState as UiState.Error).message, Toast.LENGTH_LONG).show()
                viewModel.resetUpdateAllergyState()
            }
        }

        else -> Unit
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OutlinedTextField(
            value = allergyDescription,
            onValueChange = { allergyDescription = it },
            label = { Text(stringResource(R.string.new_allergy_description)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        OutlinedTextField(
            value = allergyDate?.formatToString() ?: "",
            onValueChange = {},
            label = { Text(stringResource(R.string.new_allergy_date)) },
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showDatePicker = true },
            enabled = false
        )

        if (showDatePicker) {
            DatePickerModal(
                onDateSelected = { selectedDateMillis ->
                    selectedDateMillis?.let { allergyDate = Date(it) }
                    showDatePicker = false
                },
                onDismiss = { showDatePicker = false }
            )
        }

        Button(
            onClick = {
                coroutineScope.launch {
                    val updatedAllergy = allergy.copy(
                        description = allergyDescription,
                        date = allergyDate!!
                    )
                    viewModel.updateAllergy(patientId, updatedAllergy)
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = stringResource(R.string.new_allergy_save))
        }

        if (editAllergyState is UiState.Loading) {
            CircularProgressIndicator(modifier = Modifier.padding(top = 16.dp))
        }
    }
}
