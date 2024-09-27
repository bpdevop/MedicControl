package com.bpdevop.mediccontrol.ui.screens.allergy

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
fun NewAllergyScreen(
    patientId: String,
    viewModel: AllergyViewModel = hiltViewModel(),
    onAllergyAdded: () -> Unit,
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var allergyDescription by remember { mutableStateOf("") }
    var allergyDate by remember { mutableStateOf<Date?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }

    var allergyDescriptionError by remember { mutableStateOf(false) }
    var allergyDateError by remember { mutableStateOf(false) }

    val addAllergyState by viewModel.addAllergyState.collectAsState()


    HandleUiStatesAllergy(addAllergyState, context, viewModel, onAllergyAdded)

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
            onValueChange = {
                allergyDescription = it
                allergyDescriptionError = allergyDescription.isEmpty()
            },
            label = { Text(stringResource(R.string.new_allergy_description)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            isError = allergyDescriptionError,
            supportingText = {
                if (allergyDescriptionError) {
                    Text(stringResource(R.string.new_allergy_error_description_required))
                }
            }
        )

        OutlinedTextField(
            value = allergyDate?.formatToString() ?: "",
            onValueChange = {},
            label = { Text(stringResource(R.string.new_allergy_date)) },
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showDatePicker = true },
            enabled = false,
            isError = allergyDateError,
            supportingText = {
                if (allergyDateError) {
                    Text(stringResource(R.string.new_allergy_error_date_required))
                }
            }
        )

        if (showDatePicker) {
            DatePickerModal(
                onDateSelected = { selectedDateMillis ->
                    selectedDateMillis?.let { allergyDate = Date(it) }
                    showDatePicker = false
                    allergyDateError = allergyDate == null
                },
                onDismiss = { showDatePicker = false }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                coroutineScope.launch {
                    allergyDescriptionError = allergyDescription.isEmpty()
                    allergyDateError = allergyDate == null

                    if (!allergyDescriptionError && !allergyDateError) {
                        coroutineScope.launch {
                            val allergy = Allergy(
                                description = allergyDescription,
                                date = allergyDate,
                            )
                            viewModel.addAllergy(patientId, allergy)
                        }
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = stringResource(R.string.new_allergy_save))
        }

        if (addAllergyState is UiState.Loading) {
            CircularProgressIndicator(modifier = Modifier.padding(top = 16.dp))
        }
    }
}

@Composable
fun HandleUiStatesAllergy(
    state: UiState<String>,
    context: Context,
    viewModel: AllergyViewModel,
    onAllergyAdded: () -> Unit,
) {
    when (state) {
        is UiState.Success -> {
            LaunchedEffect(Unit) {
                onAllergyAdded()
                viewModel.resetAddAllergyState()
            }
        }

        is UiState.Error -> {
            LaunchedEffect(Unit) {
                Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
                viewModel.resetAddAllergyState()
            }
        }

        else -> Unit
    }
}
