package com.bpdevop.mediccontrol.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bpdevop.mediccontrol.core.utils.UiState
import com.bpdevop.mediccontrol.data.model.OxygenSaturation
import com.bpdevop.mediccontrol.data.repository.OxygenSaturationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OxygenSaturationViewModel @Inject constructor(
    private val repository: OxygenSaturationRepository,
) : ViewModel() {

    private val _oxygenSaturationHistoryState = MutableStateFlow<UiState<List<OxygenSaturation>>>(UiState.Idle)
    val oxygenSaturationHistoryState: StateFlow<UiState<List<OxygenSaturation>>> = _oxygenSaturationHistoryState

    private val _addOxygenSaturationState = MutableStateFlow<UiState<String>>(UiState.Idle)
    val addOxygenSaturationState: StateFlow<UiState<String>> = _addOxygenSaturationState

    private val _updateOxygenSaturationState = MutableStateFlow<UiState<String>>(UiState.Idle)
    val updateOxygenSaturationState: StateFlow<UiState<String>> = _updateOxygenSaturationState

    private val _deleteOxygenSaturationState = MutableStateFlow<UiState<String>>(UiState.Idle)
    val deleteOxygenSaturationState: StateFlow<UiState<String>> = _deleteOxygenSaturationState

    fun addOxygenSaturation(patientId: String, oxygenSaturation: OxygenSaturation) {
        viewModelScope.launch {
            _addOxygenSaturationState.value = UiState.Loading
            val result = repository.addOxygenSaturationToPatient(patientId, oxygenSaturation)
            _addOxygenSaturationState.value = result
        }
    }

    fun getOxygenSaturationHistory(patientId: String) {
        viewModelScope.launch {
            _oxygenSaturationHistoryState.value = UiState.Loading
            val result = repository.getOxygenSaturationHistory(patientId)
            _oxygenSaturationHistoryState.value = result
        }
    }

    fun updateOxygenSaturation(patientId: String, oxygenSaturation: OxygenSaturation) {
        viewModelScope.launch {
            _updateOxygenSaturationState.value = UiState.Loading
            val result = repository.updateOxygenSaturation(patientId, oxygenSaturation)
            _updateOxygenSaturationState.value = result
        }
    }

    fun deleteOxygenSaturation(patientId: String, oxygenSaturationId: String) {
        viewModelScope.launch {
            _deleteOxygenSaturationState.value = UiState.Loading
            val result = repository.deleteOxygenSaturation(patientId, oxygenSaturationId)
            _deleteOxygenSaturationState.value = result
        }
    }

    fun resetAddOxygenSaturationState() {
        _addOxygenSaturationState.value = UiState.Idle
    }

    fun resetUpdateOxygenSaturationState() {
        _updateOxygenSaturationState.value = UiState.Idle
    }

    fun resetDeleteOxygenSaturationState() {
        _deleteOxygenSaturationState.value = UiState.Idle
    }
}