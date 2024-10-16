package com.bpdevop.mediccontrol.ui.viewmodels

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bpdevop.mediccontrol.core.utils.UiState
import com.bpdevop.mediccontrol.data.model.Radiology
import com.bpdevop.mediccontrol.data.repository.RadiologyRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RadiologyViewModel @Inject constructor(
    private val repository: RadiologyRepository,
) : ViewModel() {

    private val _radiologyHistoryState = MutableStateFlow<UiState<List<Radiology>>>(UiState.Idle)
    val radiologyHistoryState: StateFlow<UiState<List<Radiology>>> = _radiologyHistoryState

    private val _radiologyState = MutableStateFlow<UiState<Radiology>>(UiState.Idle)
    val radiologyState: StateFlow<UiState<Radiology>> = _radiologyState

    private val _addRadiologyState = MutableStateFlow<UiState<String>>(UiState.Idle)
    val addRadiologyState: StateFlow<UiState<String>> = _addRadiologyState

    private val _updateRadiologyState = MutableStateFlow<UiState<String>>(UiState.Idle)
    val updateRadiologyState: StateFlow<UiState<String>> = _updateRadiologyState

    private val _deleteRadiologyState = MutableStateFlow<UiState<String>>(UiState.Idle)
    val deleteRadiologyState: StateFlow<UiState<String>> = _deleteRadiologyState

    // Añadir un nuevo estudio de radiología con múltiples archivos
    fun addRadiology(patientId: String, radiology: Radiology, documentUris: List<Uri>) {
        viewModelScope.launch {
            _addRadiologyState.value = UiState.Loading
            val result = repository.addRadiology(patientId, radiology, documentUris)
            _addRadiologyState.value = result
        }
    }

    // Obtener el historial de estudios de radiología de un paciente
    fun getRadiologyHistory(patientId: String) {
        viewModelScope.launch {
            _radiologyHistoryState.value = UiState.Loading
            val result = repository.getRadiologyHistory(patientId)
            _radiologyHistoryState.value = result
        }
    }

    // Obtener un estudio de radiología específico
    fun getRadiologyById(patientId: String, radiologyId: String) {
        viewModelScope.launch {
            _radiologyState.value = UiState.Loading
            val result = repository.getRadiologyById(patientId, radiologyId)
            _radiologyState.value = result
        }
    }

    // Actualizar un estudio de radiología con archivos nuevos y eliminados
    fun updateRadiology(patientId: String, radiology: Radiology, newDocumentUris: List<Uri>?, existingDocumentUrls: List<String>) {
        viewModelScope.launch {
            _updateRadiologyState.value = UiState.Loading
            val result = repository.updateRadiology(patientId, radiology, newDocumentUris, existingDocumentUrls)
            _updateRadiologyState.value = result
        }
    }

    // Eliminar un estudio de radiología
    fun deleteRadiology(patientId: String, radiologyId: String) {
        viewModelScope.launch {
            _deleteRadiologyState.value = UiState.Loading
            val result = repository.deleteRadiology(patientId, radiologyId)
            _deleteRadiologyState.value = result
        }
    }

    // Resetear los estados después de cada operación
    fun resetAddRadiologyState() {
        _addRadiologyState.value = UiState.Idle
    }

    fun resetUpdateRadiologyState() {
        _updateRadiologyState.value = UiState.Idle
    }

    fun resetDeleteRadiologyState() {
        _deleteRadiologyState.value = UiState.Idle
    }
}