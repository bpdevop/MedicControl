package com.bpdevop.mediccontrol.ui.viewmodels

import android.app.Application
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bpdevop.mediccontrol.core.extensions.generatePrescriptionPdf
import com.bpdevop.mediccontrol.core.utils.UiState
import com.bpdevop.mediccontrol.data.model.DoctorProfile
import com.bpdevop.mediccontrol.data.model.Patient
import com.bpdevop.mediccontrol.data.model.Prescription
import com.bpdevop.mediccontrol.data.repository.AuthRepository
import com.bpdevop.mediccontrol.data.repository.PatientsRepository
import com.bpdevop.mediccontrol.data.repository.PrescriptionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class PrescriptionViewModel @Inject constructor(
    private val app: Application,
    private val prescriptionRepository: PrescriptionRepository,
    private val authRepository: AuthRepository,
    private val patientsRepository: PatientsRepository,
) : ViewModel() {

    private val _prescriptionHistoryState = MutableStateFlow<UiState<List<Prescription>>>(UiState.Idle)
    val prescriptionHistoryState: StateFlow<UiState<List<Prescription>>> = _prescriptionHistoryState

    private val _prescriptionState = MutableStateFlow<UiState<Prescription>>(UiState.Idle)
    val prescriptionState: StateFlow<UiState<Prescription>> = _prescriptionState

    private val _addPrescriptionState = MutableStateFlow<UiState<String>>(UiState.Idle)
    val addPrescriptionState: StateFlow<UiState<String>> = _addPrescriptionState

    private val _updatePrescriptionState = MutableStateFlow<UiState<String>>(UiState.Idle)
    val updatePrescriptionState: StateFlow<UiState<String>> = _updatePrescriptionState

    private val _deletePrescriptionState = MutableStateFlow<UiState<String>>(UiState.Idle)
    val deletePrescriptionState: StateFlow<UiState<String>> = _deletePrescriptionState

    private val _doctorProfileState = MutableStateFlow<UiState<DoctorProfile?>>(UiState.Idle)
    val doctorProfileState: StateFlow<UiState<DoctorProfile?>> = _doctorProfileState

    private val _patientState = MutableStateFlow<UiState<Patient>>(UiState.Idle)
    val patientState: StateFlow<UiState<Patient>> = _patientState

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    // Añadir una nueva receta con múltiples archivos
    fun addPrescription(patientId: String, prescription: Prescription, documentUris: List<Uri>) {
        viewModelScope.launch {
            _addPrescriptionState.value = UiState.Loading
            val result = prescriptionRepository.addPrescription(patientId, prescription, documentUris)
            _addPrescriptionState.value = result
        }
    }

    // Obtener el historial de recetas
    fun getPrescriptionHistory(patientId: String) {
        viewModelScope.launch {
            _prescriptionHistoryState.value = UiState.Loading
            val result = prescriptionRepository.getPrescriptionHistory(patientId)
            _prescriptionHistoryState.value = result
        }
    }

    // Obtener una receta por su ID
    fun getPrescriptionById(patientId: String, prescriptionId: String) {
        viewModelScope.launch {
            _prescriptionState.value = UiState.Loading
            val result = prescriptionRepository.getPrescriptionById(patientId, prescriptionId)
            _prescriptionState.value = result
        }
    }

    // Actualizar una receta con archivos nuevos y eliminados
    fun updatePrescription(patientId: String, prescription: Prescription, newDocumentUris: List<Uri>?, existingDocumentUrls: List<String>) {
        viewModelScope.launch {
            _updatePrescriptionState.value = UiState.Loading
            val result = prescriptionRepository.updatePrescription(patientId, prescription, newDocumentUris, existingDocumentUrls)
            _updatePrescriptionState.value = result
        }
    }

    // Eliminar una receta
    fun deletePrescription(patientId: String, prescriptionId: String) {
        viewModelScope.launch {
            _deletePrescriptionState.value = UiState.Loading
            val result = prescriptionRepository.deletePrescription(patientId, prescriptionId)
            _deletePrescriptionState.value = result
        }
    }

    fun getDoctorProfile() {
        viewModelScope.launch {
            _doctorProfileState.value = UiState.Loading
            val result = authRepository.getDoctorProfile()
            _doctorProfileState.value = result
        }
    }

    fun getPatientById(patientId: String) {
        viewModelScope.launch {
            _patientState.value = UiState.Loading
            val result = patientsRepository.getPatientById(patientId)
            _patientState.value = result
        }
    }

    fun generatePdfAndOpen(
        doctor: DoctorProfile,
        patient: Patient,
        prescription: Prescription,
        openPdf: (File) -> Unit,
    ) {
        _loading.value = true
        viewModelScope.launch {
            val pdfFile = app.generatePrescriptionPdf(doctor, patient, prescription)
            _loading.value = false
            openPdf(pdfFile)
        }
    }

    // Resetear los estados después de la operación
    fun resetAddPrescriptionState() {
        _addPrescriptionState.value = UiState.Idle
    }

    fun resetUpdatePrescriptionState() {
        _updatePrescriptionState.value = UiState.Idle
    }

    fun resetDeletePrescriptionState() {
        _deletePrescriptionState.value = UiState.Idle
    }
}
