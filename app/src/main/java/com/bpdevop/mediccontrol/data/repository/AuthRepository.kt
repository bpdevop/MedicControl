package com.bpdevop.mediccontrol.data.repository

import com.bpdevop.mediccontrol.core.utils.UiState
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AuthRepository @Inject constructor(
    private val firebaseAuth: FirebaseAuth
) {
    suspend fun signUp(email: String, password: String): UiState<FirebaseUser?> {
        return try {
            val result = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            result.user?.let { user ->
                user.sendEmailVerification().await()
                UiState.Success(user)
            } ?: UiState.Error("No se pudo crear la cuenta")
        } catch (e: Exception) {
            UiState.Error(e.message ?: "Error desconocido")
        }
    }

    suspend fun signIn(email: String, password: String): UiState<FirebaseUser?> {
        return try {
            val result = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            val user = result.user
            if (user != null) {
                if (user.isEmailVerified) {
                    UiState.Success(user)
                } else {
                    UiState.Error("Por favor, verifica tu correo electrónico antes de continuar.")
                }
            } else {
                UiState.Error("Credenciales incorrectas. Por favor, intenta nuevamente.")
            }
        } catch (e: FirebaseAuthInvalidCredentialsException) {
            UiState.Error("Correo electrónico o contraseña incorrectos. ¿Deseas registrarte?")
        } catch (e: FirebaseAuthException) {
            UiState.Error("Error de autenticación. Por favor, intenta nuevamente.")
        }
    }

    suspend fun resendVerificationEmail(): UiState<String> {
        return try {
            val user = firebaseAuth.currentUser
            user?.let {
                it.sendEmailVerification().await()
                UiState.Success("Correo de verificación enviado a ${user.email}")
            } ?: UiState.Error("Usuario no encontrado")
        } catch (e: Exception) {
            UiState.Error(e.message ?: "Error desconocido")
        }
    }

    suspend fun resetPassword(email: String): UiState<String> {
        return try {
            firebaseAuth.sendPasswordResetEmail(email).await()
            UiState.Success("Correo de recuperación enviado a $email")
        } catch (e: Exception) {
            UiState.Error(e.message ?: "Error al enviar el correo de recuperación")
        }
    }

    fun signOut() {
        firebaseAuth.signOut()
    }

    fun getCurrentUserId(): String? {
        return firebaseAuth.currentUser?.uid
    }
}
