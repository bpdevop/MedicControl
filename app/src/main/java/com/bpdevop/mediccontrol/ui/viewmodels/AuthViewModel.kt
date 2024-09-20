package com.bpdevop.mediccontrol.ui.viewmodels

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bpdevop.mediccontrol.core.utils.UiState
import com.bpdevop.mediccontrol.data.model.DoctorProfile
import com.bpdevop.mediccontrol.data.repository.AuthRepository
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<FirebaseUser?>>(UiState.Idle)
    val uiState: StateFlow<UiState<FirebaseUser?>> = _uiState

    private val _passwordResetState = MutableStateFlow<UiState<String>>(UiState.Idle)
    val passwordResetState: StateFlow<UiState<String>> = _passwordResetState

    private val _profileState = MutableStateFlow<UiState<DoctorProfile?>>(UiState.Idle)
    val profileState: StateFlow<UiState<DoctorProfile?>> = _profileState

    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            val result = authRepository.signIn(email, password)
            _uiState.value = result
        }
    }

    fun signUp(email: String, password: String, name: String, registrationNumber: String, phoneNumber: String, photoUri: Uri?) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            val result = authRepository.signUp(email, password, name, registrationNumber, phoneNumber, photoUri)
            _uiState.value = result
        }
    }

    fun getDoctorProfile() {
        viewModelScope.launch {
            _profileState.value = UiState.Loading
            val result = authRepository.getDoctorProfile()
            _profileState.value = result
        }
    }

    fun resetPassword(email: String) {
        viewModelScope.launch {
            _passwordResetState.value = UiState.Loading
            val result = authRepository.resetPassword(email)
            _passwordResetState.value = result
        }
    }

    fun resetUiState() {
        _uiState.value = UiState.Idle
        _profileState.value = UiState.Idle
    }
}