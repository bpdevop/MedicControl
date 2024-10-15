package com.bpdevop.mediccontrol.ui.viewmodels

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bpdevop.mediccontrol.core.utils.UiState
import com.bpdevop.mediccontrol.data.model.Laboratory
import com.bpdevop.mediccontrol.data.repository.LaboratoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LaboratoryViewModel @Inject constructor(
    private val repository: LaboratoryRepository
) : ViewModel() {

    private val _laboratoryHistoryState = MutableStateFlow<UiState<List<Laboratory>>>(UiState.Idle)
    val laboratoryHistoryState: StateFlow<UiState<List<Laboratory>>> = _laboratoryHistoryState

    private val _laboratoryState = MutableStateFlow<UiState<Laboratory>>(UiState.Idle)
    val laboratoryState: StateFlow<UiState<Laboratory>> = _laboratoryState

    private val _addLaboratoryState = MutableStateFlow<UiState<String>>(UiState.Idle)
    val addLaboratoryState: StateFlow<UiState<String>> = _addLaboratoryState

    private val _updateLaboratoryState = MutableStateFlow<UiState<String>>(UiState.Idle)
    val updateLaboratoryState: StateFlow<UiState<String>> = _updateLaboratoryState

    private val _deleteLaboratoryState = MutableStateFlow<UiState<String>>(UiState.Idle)
    val deleteLaboratoryState: StateFlow<UiState<String>> = _deleteLaboratoryState

    // Añadir un nuevo registro de laboratorio con múltiples archivos
    fun addLaboratory(patientId: String, laboratory: Laboratory, documentUris: List<Uri>) {
        viewModelScope.launch {
            _addLaboratoryState.value = UiState.Loading
            val result = repository.addLaboratory(patientId, laboratory, documentUris)
            _addLaboratoryState.value = result
        }
    }

    // Obtener el historial de laboratorios de un paciente
    fun getLaboratoryHistory(patientId: String) {
        viewModelScope.launch {
            _laboratoryHistoryState.value = UiState.Loading
            val result = repository.getLaboratoryHistory(patientId)
            _laboratoryHistoryState.value = result
        }
    }

    // Obtener un registro de laboratorio específico
    fun getLaboratoryById(patientId: String, laboratoryId: String) {
        viewModelScope.launch {
            _laboratoryState.value = UiState.Loading
            val result = repository.getLaboratoryById(patientId, laboratoryId)
            _laboratoryState.value = result
        }
    }

    // Actualizar un registro de laboratorio con archivos nuevos y eliminados
    fun updateLaboratory(patientId: String, laboratory: Laboratory, newDocumentUris: List<Uri>?, existingDocumentUrls: List<String>) {
        viewModelScope.launch {
            _updateLaboratoryState.value = UiState.Loading
            val result = repository.updateLaboratory(patientId, laboratory, newDocumentUris, existingDocumentUrls)
            _updateLaboratoryState.value = result
        }
    }

    // Eliminar un registro de laboratorio
    fun deleteLaboratory(patientId: String, laboratoryId: String) {
        viewModelScope.launch {
            _deleteLaboratoryState.value = UiState.Loading
            val result = repository.deleteLaboratory(patientId, laboratoryId)
            _deleteLaboratoryState.value = result
        }
    }

    // Resetear los estados después de cada operación
    fun resetAddLaboratoryState() {
        _addLaboratoryState.value = UiState.Idle
    }

    fun resetUpdateLaboratoryState() {
        _updateLaboratoryState.value = UiState.Idle
    }

    fun resetDeleteLaboratoryState() {
        _deleteLaboratoryState.value = UiState.Idle
    }
}