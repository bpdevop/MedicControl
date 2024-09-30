package com.bpdevop.mediccontrol.ui.viewmodels

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bpdevop.mediccontrol.core.utils.UiState
import com.bpdevop.mediccontrol.data.model.Examination
import com.bpdevop.mediccontrol.data.repository.ExaminationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ExaminationViewModel @Inject constructor(
    private val repository: ExaminationRepository,
) : ViewModel() {

    private val _examinationHistoryState = MutableStateFlow<UiState<List<Examination>>>(UiState.Idle)
    val examinationHistoryState: StateFlow<UiState<List<Examination>>> = _examinationHistoryState

    private val _examinationState = MutableStateFlow<UiState<Examination>>(UiState.Idle)
    val examinationState: StateFlow<UiState<Examination>> = _examinationState

    private val _addExaminationState = MutableStateFlow<UiState<String>>(UiState.Idle)
    val addExaminationState: StateFlow<UiState<String>> = _addExaminationState

    private val _updateExaminationState = MutableStateFlow<UiState<String>>(UiState.Idle)
    val updateExaminationState: StateFlow<UiState<String>> = _updateExaminationState

    private val _deleteExaminationState = MutableStateFlow<UiState<String>>(UiState.Idle)
    val deleteExaminationState: StateFlow<UiState<String>> = _deleteExaminationState

    // Añadir un nuevo examen con múltiples archivos
    fun addExamination(patientId: String, examination: Examination, documentUris: List<Uri>) {
        viewModelScope.launch {
            _addExaminationState.value = UiState.Loading
            val result = repository.addExamination(patientId, examination, documentUris)
            _addExaminationState.value = result
        }
    }

    // Obtener el historial de exámenes
    fun getExaminationHistory(patientId: String) {
        viewModelScope.launch {
            _examinationHistoryState.value = UiState.Loading
            val result = repository.getExaminationHistory(patientId)
            _examinationHistoryState.value = result
        }
    }

    fun getExaminationById(patientId: String, examinationId: String) {
        viewModelScope.launch {
            _examinationState.value = UiState.Loading
            val result = repository.getExaminationById(patientId, examinationId)
            _examinationState.value = result
        }
    }

    // Actualizar un examen existente con archivos nuevos y eliminados
    fun updateExamination(patientId: String, examination: Examination, newDocumentUris: List<Uri>?, existingDocumentUrls: List<String>) {
        viewModelScope.launch {
            _updateExaminationState.value = UiState.Loading
            val result = repository.updateExamination(patientId, examination, newDocumentUris, existingDocumentUrls)
            _updateExaminationState.value = result
        }
    }

    // Eliminar un examen
    fun deleteExamination(patientId: String, examinationId: String) {
        viewModelScope.launch {
            _deleteExaminationState.value = UiState.Loading
            val result = repository.deleteExamination(patientId, examinationId)
            _deleteExaminationState.value = result
        }
    }

    // Resetear los estados después de la operación
    fun resetAddExaminationState() {
        _addExaminationState.value = UiState.Idle
    }

    fun resetUpdateExaminationState() {
        _updateExaminationState.value = UiState.Idle
    }

    fun resetDeleteExaminationState() {
        _deleteExaminationState.value = UiState.Idle
    }
}