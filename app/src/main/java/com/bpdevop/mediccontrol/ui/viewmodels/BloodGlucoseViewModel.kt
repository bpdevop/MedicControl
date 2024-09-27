package com.bpdevop.mediccontrol.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bpdevop.mediccontrol.core.utils.UiState
import com.bpdevop.mediccontrol.data.model.BloodGlucose
import com.bpdevop.mediccontrol.data.repository.BloodGlucoseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BloodGlucoseViewModel @Inject constructor(
    private val repository: BloodGlucoseRepository
) : ViewModel() {

    private val _bloodGlucoseHistoryState = MutableStateFlow<UiState<List<BloodGlucose>>>(UiState.Idle)
    val bloodGlucoseHistoryState: StateFlow<UiState<List<BloodGlucose>>> = _bloodGlucoseHistoryState

    private val _addBloodGlucoseState = MutableStateFlow<UiState<String>>(UiState.Idle)
    val addBloodGlucoseState: StateFlow<UiState<String>> = _addBloodGlucoseState

    private val _updateBloodGlucoseState = MutableStateFlow<UiState<String>>(UiState.Idle)
    val updateBloodGlucoseState: StateFlow<UiState<String>> = _updateBloodGlucoseState

    private val _deleteBloodGlucoseState = MutableStateFlow<UiState<String>>(UiState.Idle)
    val deleteBloodGlucoseState: StateFlow<UiState<String>> = _deleteBloodGlucoseState

    fun addBloodGlucose(patientId: String, bloodGlucose: BloodGlucose) {
        viewModelScope.launch {
            _addBloodGlucoseState.value = UiState.Loading
            val result = repository.addBloodGlucoseToPatient(patientId, bloodGlucose)
            _addBloodGlucoseState.value = result
        }
    }

    fun getBloodGlucoseHistory(patientId: String) {
        viewModelScope.launch {
            _bloodGlucoseHistoryState.value = UiState.Loading
            val result = repository.getBloodGlucoseHistory(patientId)
            _bloodGlucoseHistoryState.value = result
        }
    }

    fun updateBloodGlucose(patientId: String, bloodGlucose: BloodGlucose) {
        viewModelScope.launch {
            _updateBloodGlucoseState.value = UiState.Loading
            val result = repository.updateBloodGlucose(patientId, bloodGlucose)
            _updateBloodGlucoseState.value = result
        }
    }

    fun deleteBloodGlucose(patientId: String, bloodGlucoseId: String) {
        viewModelScope.launch {
            _deleteBloodGlucoseState.value = UiState.Loading
            val result = repository.deleteBloodGlucose(patientId, bloodGlucoseId)
            _deleteBloodGlucoseState.value = result
        }
    }

    fun resetAddBloodGlucoseState() {
        _addBloodGlucoseState.value = UiState.Idle
    }

    fun resetUpdateBloodGlucoseState() {
        _updateBloodGlucoseState.value = UiState.Idle
    }

    fun resetDeleteBloodGlucoseState() {
        _deleteBloodGlucoseState.value = UiState.Idle
    }
}
