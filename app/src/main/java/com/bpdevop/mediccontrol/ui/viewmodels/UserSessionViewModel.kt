package com.bpdevop.mediccontrol.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserSessionViewModel @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
) : ViewModel() {
    private val _currentUser = MutableStateFlow<FirebaseUser?>(null)
    val currentUser: StateFlow<FirebaseUser?> get() = _currentUser

    private val _navigationState = MutableStateFlow<NavigationState>(NavigationState.Loading)
    val navigationState: StateFlow<NavigationState> = _navigationState.asStateFlow()

    private val _isEmailVerified = MutableStateFlow(true)
    val isEmailVerified: StateFlow<Boolean> get() = _isEmailVerified

    init {
        checkUserAuthentication()
    }

    fun checkUserAuthentication() {
        val currentUser = firebaseAuth.currentUser

        if (currentUser != null) {
            _navigationState.value = NavigationState.MainScreen
        } else {
            _navigationState.value = NavigationState.LoginScreen
        }
    }

    fun checkEmailVerification() {
        val currentUser = firebaseAuth.currentUser
        _currentUser.value = currentUser

        if (currentUser != null) {
            viewModelScope.launch {
                currentUser.reload().addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        _isEmailVerified.value = currentUser.isEmailVerified
                    }
                }
            }
        } else {
            _isEmailVerified.value = false
        }
    }

    fun signOut() {
        firebaseAuth.signOut()
        _currentUser.value = null
    }

    sealed class NavigationState {
        data object Loading : NavigationState()
        data object LoginScreen : NavigationState()
        data object MainScreen : NavigationState()
    }
}
