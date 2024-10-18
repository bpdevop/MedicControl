package com.bpdevop.mediccontrol.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bpdevop.mediccontrol.core.utils.UiState
import com.bpdevop.mediccontrol.data.model.Allergy
import com.bpdevop.mediccontrol.data.model.BloodGlucose
import com.bpdevop.mediccontrol.data.model.BloodPressure
import com.bpdevop.mediccontrol.data.model.LabTestItem
import com.bpdevop.mediccontrol.data.model.OxygenSaturation
import com.bpdevop.mediccontrol.data.model.PrescriptionItem
import com.bpdevop.mediccontrol.data.repository.AllergyRepository
import com.bpdevop.mediccontrol.data.repository.BloodGlucoseRepository
import com.bpdevop.mediccontrol.data.repository.BloodPressureRepository
import com.bpdevop.mediccontrol.data.repository.LaboratoryRepository
import com.bpdevop.mediccontrol.data.repository.OxygenSaturationRepository
import com.bpdevop.mediccontrol.data.repository.PrescriptionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PatientMedicalHistoryViewModel @Inject constructor(
    private val laboratoryRepository: LaboratoryRepository,
    private val allergyRepository: AllergyRepository,
    private val prescriptionRepository: PrescriptionRepository,
    private val bloodPressureRepository: BloodPressureRepository,
    private val bloodGlucoseRepository: BloodGlucoseRepository,
    private val oxygenSaturationRepository: OxygenSaturationRepository,
) : ViewModel() {

    // LiveData para manejar los datos del historial médico
    private val _uiState = MutableStateFlow(PatientMedicalHistoryUiState())
    val uiState: StateFlow<PatientMedicalHistoryUiState> = _uiState

    // Laboratorios
    private val _labTestSummary = MutableStateFlow<List<LabTestItem>>(emptyList())
    val labTestSummary: StateFlow<List<LabTestItem>> = _labTestSummary

    // Alergias
    private val _allergies = MutableStateFlow<List<Allergy>>(emptyList())
    val allergies: StateFlow<List<Allergy>> = _allergies

    // Resumen de medicamentos (prescripciones)
    private val _prescriptionSummary = MutableStateFlow<List<PrescriptionItem>>(emptyList())
    val prescriptionSummary: StateFlow<List<PrescriptionItem>> = _prescriptionSummary

    // Último registro de presión arterial
    private val _lastBloodPressure = MutableStateFlow<BloodPressure?>(null)
    val lastBloodPressure: StateFlow<BloodPressure?> = _lastBloodPressure

    // Glicemia (Glucosa en sangre)
    private val _lastBloodGlucose = MutableStateFlow<BloodGlucose?>(null)
    val lastBloodGlucose: StateFlow<BloodGlucose?> = _lastBloodGlucose

    // Última saturación de oxígeno
    private val _lastOxygenSaturation = MutableStateFlow<OxygenSaturation?>(null)
    val lastOxygenSaturation: StateFlow<OxygenSaturation?> = _lastOxygenSaturation


    fun loadMedicalHistory(patientId: String) {
        viewModelScope.launch {
            laboratoryRepository.getLaboratorySummaryForPatient(patientId).collect { result ->
                when (result) {
                    is UiState.Success -> _labTestSummary.value = result.data
                    is UiState.Error -> _uiState.value = _uiState.value.copy(errorMessage = result.message)
                    else -> {}
                }
            }
        }

        // Cargar alergias
        viewModelScope.launch {
            val result = allergyRepository.getAllergyHistory(patientId)
            if (result is UiState.Success) {
                _allergies.value = result.data
            } else if (result is UiState.Error) {
                _uiState.value = _uiState.value.copy(errorMessage = result.message)
            }
        }

        // Cargar resumen de prescripciones
        viewModelScope.launch {
            prescriptionRepository.getPrescriptionSummaryForPatient(patientId).collect { result ->
                when (result) {
                    is UiState.Success -> _prescriptionSummary.value = result.data
                    is UiState.Error -> _uiState.value = _uiState.value.copy(errorMessage = result.message)
                    else -> {}
                }
            }
        }

        // Cargar el último registro de presión arterial
        viewModelScope.launch {
            val result = bloodPressureRepository.getLastBloodPressure(patientId)
            if (result is UiState.Success) {
                _lastBloodPressure.value = result.data
            } else if (result is UiState.Error) {
                _uiState.value = _uiState.value.copy(errorMessage = result.message)
            }
        }

        // Cargar último registro de glucosa
        viewModelScope.launch {
            val result = bloodGlucoseRepository.getLastBloodGlucoseRecord(patientId)
            if (result is UiState.Success) {
                _lastBloodGlucose.value = result.data
            } else if (result is UiState.Error) {
                _uiState.value = _uiState.value.copy(errorMessage = result.message)
            }
        }

        viewModelScope.launch {
            val result = oxygenSaturationRepository.getLastOxygenSaturation(patientId)
            if (result is UiState.Success) {
                _lastOxygenSaturation.value = result.data
            } else if (result is UiState.Error) {
                _uiState.value = _uiState.value.copy(errorMessage = result.message)
            }
        }
    }

}

data class PatientMedicalHistoryUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
)
