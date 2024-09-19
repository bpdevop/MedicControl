package com.bpdevop.mediccontrol.ui.viewmodels

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bpdevop.mediccontrol.core.utils.UiState
import com.bpdevop.mediccontrol.data.model.Patient
import com.bpdevop.mediccontrol.data.repository.PatientsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PatientsViewModel @Inject constructor(
    private val repository: PatientsRepository,
) : ViewModel() {

    private val _patientsState = MutableStateFlow<UiState<List<Patient>>>(UiState.Idle)
    val patientsState: StateFlow<UiState<List<Patient>>> = _patientsState

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing

    private val _patientDetailState = MutableStateFlow<UiState<Patient>>(UiState.Idle)
    val patientDetailState: StateFlow<UiState<Patient>> = _patientDetailState

    private val _addPatientState = MutableStateFlow<UiState<String>>(UiState.Idle)
    val addPatientState: StateFlow<UiState<String>> = _addPatientState


    fun refreshPatients() {
        viewModelScope.launch {
            _isRefreshing.value = true
            val result = repository.getPatients()
            _patientsState.value = result
            _isRefreshing.value = false
        }
    }

    fun loadPatientDetail(patientId: String) {
        viewModelScope.launch {
            _patientDetailState.value = UiState.Loading
            val result = repository.getPatientById(patientId)
            _patientDetailState.value = result
        }
    }


    fun addPatient(patient: Patient, photoUri: Uri?) {
        viewModelScope.launch {
            _addPatientState.value = UiState.Loading
            val result = repository.addPatient(patient, photoUri)
            _addPatientState.value = result
        }
    }

    fun resetAddPatientState() {
        _addPatientState.value = UiState.Idle
    }
}
