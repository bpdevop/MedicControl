package com.bpdevop.mediccontrol.ui.viewmodels

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bpdevop.mediccontrol.core.extensions.generatePrescriptionPdf
import com.bpdevop.mediccontrol.core.utils.UiState
import com.bpdevop.mediccontrol.data.model.DoctorProfile
import com.bpdevop.mediccontrol.data.model.Prescription
import com.bpdevop.mediccontrol.data.repository.DoctorProfileRepository
import com.bpdevop.mediccontrol.data.repository.PatientsRepository
import com.bpdevop.mediccontrol.data.repository.PrescriptionRepository
import com.bpdevop.mediccontrol.di.IoDispatcher
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class PrescriptionViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val prescriptionRepository: PrescriptionRepository,
    private val doctorProfileRepository: DoctorProfileRepository,
    private val patientsRepository: PatientsRepository,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
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

    private val _shouldOpenPdf = MutableStateFlow(false)
    val shouldOpenPdf: StateFlow<Boolean> = _shouldOpenPdf

    private val _pdfExportState = MutableStateFlow<UiState<File>>(UiState.Idle)
    val pdfExportState: StateFlow<UiState<File>> = _pdfExportState

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
            val result = doctorProfileRepository.getDoctorProfile()
            _doctorProfileState.value = result
        }
    }

    fun setShouldOpenPdf(openPdf: Boolean) {
        _shouldOpenPdf.value = openPdf
    }

    fun exportPrescriptionToPDF(patientId: String, prescription: Prescription) {
        viewModelScope.launch(ioDispatcher) {
            _pdfExportState.value = UiState.Loading
            try {
                // Obtener el perfil del paciente
                val patientState = patientsRepository.getPatientById(patientId)

                if (patientState is UiState.Success) {
                    val patient = patientState.data

                    // Asegurar que el doctor está cargado
                    val doctorProfile = (_doctorProfileState.value as? UiState.Success)?.data
                    if (doctorProfile == null) {
                        _pdfExportState.value = UiState.Error("Perfil del doctor no encontrado.")
                        return@launch
                    }

                    // Generar el PDF con el perfil del paciente y del doctor
                    val file = context.generatePrescriptionPdf(doctorProfile, patient, prescription)
                    _pdfExportState.value = UiState.Success(file)
                } else if (patientState is UiState.Error) {
                    _pdfExportState.value = UiState.Error("Error al cargar el perfil del paciente: ${patientState.message}")
                }
            } catch (e: Exception) {
                _pdfExportState.value = UiState.Error("Error al exportar a PDF: ${e.message}")
            }
        }
    }


    // Resetear los estados después de la operación
    fun resetAddPrescriptionState() {
        _addPrescriptionState.value = UiState.Idle
    }

    fun resetDeletePrescriptionState() {
        _deletePrescriptionState.value = UiState.Idle
    }

    fun resetPdfExportState() {
        _pdfExportState.value = UiState.Idle
    }
}
