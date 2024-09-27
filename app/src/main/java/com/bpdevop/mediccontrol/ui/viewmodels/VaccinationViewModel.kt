package com.bpdevop.mediccontrol.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bpdevop.mediccontrol.core.utils.UiState
import com.bpdevop.mediccontrol.data.model.Vaccine
import com.bpdevop.mediccontrol.data.repository.VaccinationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class VaccinationViewModel @Inject constructor(
    private val repository: VaccinationRepository,
) : ViewModel() {

    private val _addVaccineState = MutableStateFlow<UiState<String>>(UiState.Idle)
    val addVaccineState: StateFlow<UiState<String>> = _addVaccineState

    fun addVaccine(patientId: String, vaccine: Vaccine) {
        viewModelScope.launch {
            _addVaccineState.value = UiState.Loading
            val result = repository.addVaccineToPatient(patientId, vaccine)
            _addVaccineState.value = result
        }
    }

    fun resetAddVaccineState() {
        _addVaccineState.value = UiState.Idle
    }
}
