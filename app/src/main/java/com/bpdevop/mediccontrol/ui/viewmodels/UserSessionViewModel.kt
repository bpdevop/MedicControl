package com.bpdevop.mediccontrol.ui.viewmodels

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class UserSessionViewModel @Inject constructor(
    private val firebaseAuth: FirebaseAuth
) : ViewModel() {

    private val _isUserAuthenticated = MutableStateFlow(false)
    val isUserAuthenticated: StateFlow<Boolean> get() = _isUserAuthenticated

    private val _currentUser = MutableStateFlow<FirebaseUser?>(null)
    val currentUser: StateFlow<FirebaseUser?> get() = _currentUser

    fun checkUserAuthentication() {
        val currentUser = firebaseAuth.currentUser
        currentUser?.reload()?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                _isUserAuthenticated.value = currentUser != null
                _currentUser.value = currentUser
            } else {
                // Maneja el caso de que el usuario haya sido eliminado o el token sea inválido
                signOut()
            }
        }
    }

    fun signOut() {
        firebaseAuth.signOut()
        _isUserAuthenticated.value = false
        _currentUser.value = null
    }
}
