package com.bpdevop.mediccontrol.ui.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bpdevop.mediccontrol.core.extensions.generateHl7ExportPdf
import com.bpdevop.mediccontrol.core.utils.UiState
import com.bpdevop.mediccontrol.data.model.DoctorProfile
import com.bpdevop.mediccontrol.data.model.Patient
import com.bpdevop.mediccontrol.data.repository.DoctorProfileRepository
import com.bpdevop.mediccontrol.data.repository.PatientsRepository
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
class InfectiousPatientsExportViewModel @Inject constructor(
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    private val patientsRepository: PatientsRepository,
    private val doctorProfileRepository: DoctorProfileRepository,
    @ApplicationContext private val context: Context,
) : ViewModel() {

    private val _infectiousPatients = MutableStateFlow<UiState<List<Patient>>>(UiState.Idle)
    val infectiousPatients: StateFlow<UiState<List<Patient>>> = _infectiousPatients

    private val _pdfExportState = MutableStateFlow<UiState<File>>(UiState.Idle)
    val pdfExportState: StateFlow<UiState<File>> = _pdfExportState

    private val _doctorProfileState = MutableStateFlow<UiState<DoctorProfile?>>(UiState.Idle)

    fun loadInfectiousPatients() {
        viewModelScope.launch {
            _infectiousPatients.value = UiState.Loading
            _infectiousPatients.value = patientsRepository.getInfectiousPatients()
        }
    }

    fun loadDoctorProfile() {
        viewModelScope.launch {
            _doctorProfileState.value = UiState.Loading
            _doctorProfileState.value = doctorProfileRepository.getDoctorProfile()
        }
    }

    fun exportToPDF(patients: List<Patient>) {
        viewModelScope.launch(ioDispatcher) {
            _pdfExportState.value = UiState.Loading
            try {

                val doctorProfile = (_doctorProfileState.value as? UiState.Success)?.data
                if (doctorProfile == null) {
                    _pdfExportState.value = UiState.Error("Perfil del doctor no encontrado.")
                    return@launch
                }

                val file = context.generateHl7ExportPdf(doctorProfile, patients)
                _pdfExportState.value = UiState.Success(file)
            } catch (e: Exception) {
                _pdfExportState.value = UiState.Error("Error al exportar a PDF: ${e.message}")
            }
        }
    }

    fun resetPdfExportState() {
        _pdfExportState.value = UiState.Idle
    }
}
