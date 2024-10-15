package com.bpdevop.mediccontrol.ui.viewmodels

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bpdevop.mediccontrol.core.utils.UiState
import com.bpdevop.mediccontrol.data.model.Disease
import com.bpdevop.mediccontrol.data.model.Patient
import com.bpdevop.mediccontrol.data.repository.IcdRepository
import com.bpdevop.mediccontrol.data.repository.PatientsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PatientsViewModel @Inject constructor(
    private val repository: PatientsRepository,
    private val icdRepository: IcdRepository,
) : ViewModel() {

    private val _patientsState = MutableStateFlow<UiState<List<Patient>>>(UiState.Idle)
    val patientsState: StateFlow<UiState<List<Patient>>> = _patientsState

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing

    private val _patientDetailState = MutableStateFlow<UiState<Patient>>(UiState.Idle)
    val patientDetailState: StateFlow<UiState<Patient>> = _patientDetailState

    private val _addPatientState = MutableStateFlow<UiState<String>>(UiState.Idle)
    val addPatientState: StateFlow<UiState<String>> = _addPatientState

    private val _updatePatientState = MutableStateFlow<UiState<String>>(UiState.Idle)
    val updatePatientState: StateFlow<UiState<String>> = _updatePatientState

    private val _deletePatientState = MutableStateFlow<UiState<String>>(UiState.Idle)
    val deletePatientState: StateFlow<UiState<String>> = _deletePatientState

    private val _diseaseSearchState = MutableStateFlow<UiState<List<Disease>>>(UiState.Idle)
    val diseaseSearchState: StateFlow<UiState<List<Disease>>> = _diseaseSearchState


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

    fun updatePatient(patient: Patient, newPhotoUri: Uri?) {
        viewModelScope.launch {
            _updatePatientState.value = UiState.Loading
            val result = repository.updatePatient(patient, newPhotoUri)
            _updatePatientState.value = result
        }
    }

    fun deletePatient(patient: Patient) {
        viewModelScope.launch {
            _deletePatientState.value = UiState.Loading
            val result = repository.deletePatient(patient)
            _deletePatientState.value = result
        }
    }

    fun searchDiseases(query: String) {
        viewModelScope.launch {
            _diseaseSearchState.value = UiState.Loading
            val result = icdRepository.searchInfectiousDiseases(query)
            _diseaseSearchState.value = result
        }
    }

    fun resetAddPatientState() {
        _addPatientState.value = UiState.Idle
    }

    fun resetUpdatePatientState() {
        _updatePatientState.value = UiState.Idle
    }

    fun resetDeletePatientState() {
        _deletePatientState.value = UiState.Idle
    }

    fun resetDiseaseSearchState() {
        _diseaseSearchState.value = UiState.Idle
    }
}