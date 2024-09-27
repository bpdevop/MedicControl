package com.bpdevop.mediccontrol.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bpdevop.mediccontrol.core.utils.UiState
import com.bpdevop.mediccontrol.data.model.BloodPressure
import com.bpdevop.mediccontrol.data.repository.BloodPressureRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BloodPressureViewModel @Inject constructor(
    private val repository: BloodPressureRepository
) : ViewModel() {

    private val _bloodPressureHistoryState = MutableStateFlow<UiState<List<BloodPressure>>>(UiState.Idle)
    val bloodPressureHistoryState: StateFlow<UiState<List<BloodPressure>>> = _bloodPressureHistoryState

    private val _addBloodPressureState = MutableStateFlow<UiState<String>>(UiState.Idle)
    val addBloodPressureState: StateFlow<UiState<String>> = _addBloodPressureState

    private val _updateBloodPressureState = MutableStateFlow<UiState<String>>(UiState.Idle)
    val updateBloodPressureState: StateFlow<UiState<String>> = _updateBloodPressureState

    private val _deleteBloodPressureState = MutableStateFlow<UiState<String>>(UiState.Idle)
    val deleteBloodPressureState: StateFlow<UiState<String>> = _deleteBloodPressureState

    fun addBloodPressure(patientId: String, bloodPressure: BloodPressure) {
        viewModelScope.launch {
            _addBloodPressureState.value = UiState.Loading
            val result = repository.addBloodPressureToPatient(patientId, bloodPressure)
            _addBloodPressureState.value = result
        }
    }

    fun getBloodPressureHistory(patientId: String) {
        viewModelScope.launch {
            _bloodPressureHistoryState.value = UiState.Loading
            val result = repository.getBloodPressureHistory(patientId)
            _bloodPressureHistoryState.value = result
        }
    }

    fun updateBloodPressure(patientId: String, bloodPressure: BloodPressure) {
        viewModelScope.launch {
            _updateBloodPressureState.value = UiState.Loading
            val result = repository.updateBloodPressure(patientId, bloodPressure)
            _updateBloodPressureState.value = result
        }
    }

    fun deleteBloodPressure(patientId: String, bloodPressureId: String) {
        viewModelScope.launch {
            _deleteBloodPressureState.value = UiState.Loading
            val result = repository.deleteBloodPressure(patientId, bloodPressureId)
            _deleteBloodPressureState.value = result
        }
    }

    fun resetAddBloodPressureState() {
        _addBloodPressureState.value = UiState.Idle
    }

    fun resetUpdateBloodPressureState() {
        _updateBloodPressureState.value = UiState.Idle
    }

    fun resetDeleteBloodPressureState() {
        _deleteBloodPressureState.value = UiState.Idle
    }
}
