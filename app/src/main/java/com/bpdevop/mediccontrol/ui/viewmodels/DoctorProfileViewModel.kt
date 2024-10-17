package com.bpdevop.mediccontrol.ui.viewmodels

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bpdevop.mediccontrol.core.utils.UiState
import com.bpdevop.mediccontrol.data.model.DoctorProfile
import com.bpdevop.mediccontrol.data.repository.DoctorProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DoctorProfileViewModel @Inject constructor(
    private val doctorProfileRepository: DoctorProfileRepository
) : ViewModel() {

    private val _doctorProfileState = MutableStateFlow<UiState<DoctorProfile?>>(UiState.Idle)
    val doctorProfileState: StateFlow<UiState<DoctorProfile?>> = _doctorProfileState

    private val _updateProfileState = MutableStateFlow<UiState<String>>(UiState.Idle)
    val updateProfileState: StateFlow<UiState<String>> = _updateProfileState

    // Obtener el perfil del doctor
    fun getDoctorProfile() {
        viewModelScope.launch {
            _doctorProfileState.value = UiState.Loading
            val result = doctorProfileRepository.getDoctorProfile()
            _doctorProfileState.value = result
        }
    }

    fun updateDoctorProfile(doctorProfile: DoctorProfile, newPhotoUri: Uri?) {
        viewModelScope.launch {
            _updateProfileState.value = UiState.Loading
            val result = doctorProfileRepository.updateDoctorProfile(doctorProfile, newPhotoUri)
            _updateProfileState.value = result
        }
    }

    // Reiniciar el estado de actualización después de completar la acción
    fun resetUpdateProfileState() {
        _updateProfileState.value = UiState.Idle
    }
}