package com.bpdevop.mediccontrol.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bpdevop.mediccontrol.core.utils.UiState
import com.bpdevop.mediccontrol.data.model.Allergy
import com.bpdevop.mediccontrol.data.repository.AllergyRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AllergyViewModel @Inject constructor(
    private val repository: AllergyRepository
) : ViewModel() {

    private val _allergyHistoryState = MutableStateFlow<UiState<List<Allergy>>>(UiState.Idle)
    val allergyHistoryState: StateFlow<UiState<List<Allergy>>> = _allergyHistoryState

    private val _addAllergyState = MutableStateFlow<UiState<String>>(UiState.Idle)
    val addAllergyState: StateFlow<UiState<String>> = _addAllergyState

    private val _updateAllergyState = MutableStateFlow<UiState<String>>(UiState.Idle)
    val updateAllergyState: StateFlow<UiState<String>> = _updateAllergyState

    private val _deleteAllergyState = MutableStateFlow<UiState<String>>(UiState.Idle)
    val deleteAllergyState: StateFlow<UiState<String>> = _deleteAllergyState

    fun addAllergy(patientId: String, allergy: Allergy) {
        viewModelScope.launch {
            _addAllergyState.value = UiState.Loading
            val result = repository.addAllergyToPatient(patientId, allergy)
            _addAllergyState.value = result
        }
    }

    fun getAllergyHistory(patientId: String) {
        viewModelScope.launch {
            _allergyHistoryState.value = UiState.Loading
            val result = repository.getAllergyHistory(patientId)
            _allergyHistoryState.value = result
        }
    }

    fun updateAllergy(patientId: String, allergy: Allergy) {
        viewModelScope.launch {
            _updateAllergyState.value = UiState.Loading
            val result = repository.updateAllergy(patientId, allergy)
            _updateAllergyState.value = result
        }
    }

    fun deleteAllergy(patientId: String, allergyId: String) {
        viewModelScope.launch {
            _deleteAllergyState.value = UiState.Loading
            val result = repository.deleteAllergy(patientId, allergyId)
            _deleteAllergyState.value = result
        }
    }

    fun resetAddAllergyState() {
        _addAllergyState.value = UiState.Idle
    }

    fun resetUpdateAllergyState() {
        _updateAllergyState.value = UiState.Idle
    }

    fun resetDeleteAllergyState() {
        _deleteAllergyState.value = UiState.Idle
    }
}
