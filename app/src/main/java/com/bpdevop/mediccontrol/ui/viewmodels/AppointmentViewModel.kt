package com.bpdevop.mediccontrol.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bpdevop.mediccontrol.core.utils.UiState
import com.bpdevop.mediccontrol.data.model.MedicalAppointment
import com.bpdevop.mediccontrol.data.model.PatientAppointment
import com.bpdevop.mediccontrol.data.repository.AppointmentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AppointmentViewModel @Inject constructor(
    private val repository: AppointmentRepository,
) : ViewModel() {

    private val _patientAppointmentHistoryState = MutableStateFlow<UiState<List<PatientAppointment>>>(UiState.Idle)
    val patientAppointmentHistoryState: StateFlow<UiState<List<PatientAppointment>>> = _patientAppointmentHistoryState

    private val _doctorAppointmentHistoryState = MutableStateFlow<UiState<List<MedicalAppointment>>>(UiState.Idle)
    val doctorAppointmentHistoryState: StateFlow<UiState<List<MedicalAppointment>>> = _doctorAppointmentHistoryState

    private val _addAppointmentState = MutableStateFlow<UiState<String>>(UiState.Idle)
    val addAppointmentState: StateFlow<UiState<String>> = _addAppointmentState

    private val _updateAppointmentState = MutableStateFlow<UiState<String>>(UiState.Idle)
    val updateAppointmentState: StateFlow<UiState<String>> = _updateAppointmentState

    private val _deleteAppointmentState = MutableStateFlow<UiState<String>>(UiState.Idle)
    val deleteAppointmentState: StateFlow<UiState<String>> = _deleteAppointmentState

    // Agregar una nueva cita
    fun addAppointment(patientId: String, appointment: PatientAppointment) {
        viewModelScope.launch {
            _addAppointmentState.value = UiState.Loading
            val result = repository.addAppointment(patientId, appointment)
            _addAppointmentState.value = result
        }
    }

    // Obtener el historial de citas de un paciente
    fun getPatientAppointmentHistory(patientId: String) {
        viewModelScope.launch {
            _patientAppointmentHistoryState.value = UiState.Loading
            val result = repository.getPatientAppointmentHistory(patientId)
            _patientAppointmentHistoryState.value = result
        }
    }

    // Obtener el historial de citas del médico
    fun getDoctorAppointmentHistory() {
        viewModelScope.launch {
            _doctorAppointmentHistoryState.value = UiState.Loading
            val result = repository.getDoctorAppointmentHistory()
            _doctorAppointmentHistoryState.value = result
        }
    }

    // Actualizar una cita
    fun updateAppointment(patientId: String, appointment: PatientAppointment) {
        viewModelScope.launch {
            _updateAppointmentState.value = UiState.Loading
            val result = repository.updateAppointment(patientId, appointment)
            _updateAppointmentState.value = result
        }
    }

    // Eliminar una cita
    fun deleteAppointment(patientId: String, appointmentId: String) {
        viewModelScope.launch {
            _deleteAppointmentState.value = UiState.Loading
            val result = repository.deleteAppointment(patientId, appointmentId)
            _deleteAppointmentState.value = result
        }
    }

    // Reseteo de estados para cada operación
    fun resetAddAppointmentState() {
        _addAppointmentState.value = UiState.Idle
    }

    fun resetUpdateAppointmentState() {
        _updateAppointmentState.value = UiState.Idle
    }

    fun resetDeleteAppointmentState() {
        _deleteAppointmentState.value = UiState.Idle
    }
}