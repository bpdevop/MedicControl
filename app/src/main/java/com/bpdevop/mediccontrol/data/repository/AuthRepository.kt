package com.bpdevop.mediccontrol.data.repository

import android.net.Uri
import com.bpdevop.mediccontrol.core.utils.UiState
import com.bpdevop.mediccontrol.data.model.DoctorProfile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject

class AuthRepository @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val firebaseStorage: FirebaseStorage,

    ) {

    suspend fun signUp(email: String, password: String, name: String, registrationNumber: String, phoneNumber: String, photoUri: Uri?): UiState<FirebaseUser?> {
        return try {
            val result = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            result.user?.let { user ->
                user.sendEmailVerification().await()

                val photoUrl = photoUri?.let { uploadProfilePhoto(user.uid, it) }

                val doctorProfile = DoctorProfile(
                    id = user.uid,
                    name = name,
                    email = email,
                    registrationNumber = registrationNumber,
                    phoneNumber = phoneNumber,
                    photoUrl = photoUrl
                )
                firestore.collection("doctors").document(user.uid).set(doctorProfile).await()

                UiState.Success(user)
            } ?: UiState.Error("No se pudo crear la cuenta")
        } catch (e: Exception) {
            UiState.Error(e.message ?: "Error desconocido")
        }
    }

    private suspend fun uploadProfilePhoto(doctorId: String, photoUri: Uri): String {
        val storageRef = firebaseStorage.reference.child("doctors_photos/$doctorId/${UUID.randomUUID()}")
        storageRef.putFile(photoUri).await() // Subir la imagen
        return storageRef.downloadUrl.await().toString() // Retornar la URL de descarga
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
