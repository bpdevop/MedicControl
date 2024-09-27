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

    private val _vaccinationHistoryState = MutableStateFlow<UiState<List<Vaccine>>>(UiState.Idle)
    val vaccinationHistoryState: StateFlow<UiState<List<Vaccine>>> = _vaccinationHistoryState

    private val _addVaccineState = MutableStateFlow<UiState<String>>(UiState.Idle)
    val addVaccineState: StateFlow<UiState<String>> = _addVaccineState

    private val _updateVaccineState = MutableStateFlow<UiState<String>>(UiState.Idle)
    val updateVaccineState: StateFlow<UiState<String>> = _updateVaccineState

    private val _deleteVaccineState = MutableStateFlow<UiState<String>>(UiState.Idle)
    val deleteVaccineState: StateFlow<UiState<String>> = _deleteVaccineState


    fun getVaccinationHistory(patientId: String) {
        viewModelScope.launch {
            _vaccinationHistoryState.value = UiState.Loading
            val result = repository.getVaccinationHistory(patientId)
            _vaccinationHistoryState.value = result
        }
    }

    fun addVaccine(patientId: String, vaccine: Vaccine) {
        viewModelScope.launch {
            _addVaccineState.value = UiState.Loading
            val result = repository.addVaccineToPatient(patientId, vaccine)
            _addVaccineState.value = result
        }
    }

    fun editVaccine(patientId: String, vaccine: Vaccine) {
        viewModelScope.launch {
            _updateVaccineState.value = UiState.Loading
            val result = repository.editVaccine(patientId, vaccine)
            _updateVaccineState.value = result
        }
    }

    fun deleteVaccine(patientId: String, vaccineId: String) {
        viewModelScope.launch {
            _deleteVaccineState.value = UiState.Loading
            val result = repository.deleteVaccine(patientId, vaccineId)
            _deleteVaccineState.value = result
            getVaccinationHistory(patientId)
        }
    }

    fun resetAddVaccineState() {
        _addVaccineState.value = UiState.Idle
    }

    fun resetUpdateVaccineState() {
        _updateVaccineState.value = UiState.Idle
    }

    fun resetDeleteVaccineState() {
        _deleteVaccineState.value = UiState.Idle
    }
}
