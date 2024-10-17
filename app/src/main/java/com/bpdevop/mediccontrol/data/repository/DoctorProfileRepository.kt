package com.bpdevop.mediccontrol.data.repository

import android.net.Uri
import com.bpdevop.mediccontrol.core.utils.UiState
import com.bpdevop.mediccontrol.data.model.DoctorProfile
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject

class DoctorProfileRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val firebaseStorage: FirebaseStorage,
    private val authRepository: AuthRepository
) {

    suspend fun getDoctorProfile(): UiState<DoctorProfile?> =
        authRepository.getCurrentUserId()?.let { doctorId ->
            runCatching {
                firestore.collection("doctors").document(doctorId)
                    .get()
                    .await()
                    .toObject(DoctorProfile::class.java)
                    ?.let { UiState.Success(it) } ?: UiState.Error("Perfil no encontrado")
            }.getOrElse { UiState.Error(it.localizedMessage ?: "Error al obtener el perfil del médico") }
        } ?: UiState.Error("No se ha iniciado sesión")

    suspend fun updateDoctorProfile(updatedProfile: DoctorProfile, newPhotoUri: Uri?): UiState<String> =
        authRepository.getCurrentUserId()?.let { doctorId ->
            runCatching {
                val updatedProfileWithPhoto = handlePhotoUpdate(updatedProfile, newPhotoUri, doctorId)
                firestore.collection("doctors").document(doctorId)
                    .set(updatedProfileWithPhoto)
                    .await()
                UiState.Success("Perfil actualizado correctamente")
            }.getOrElse { UiState.Error(it.localizedMessage ?: "Error al actualizar el perfil del médico") }
        } ?: UiState.Error("No se ha iniciado sesión")

    private suspend fun handlePhotoUpdate(profile: DoctorProfile, newPhotoUri: Uri?, doctorId: String): DoctorProfile =
        when {
            // Caso 1: No hay foto actual y se agrega una nueva
            newPhotoUri != null && profile.photoUrl == null -> {
                val newPhotoUrl = uploadPhoto(newPhotoUri, doctorId)
                profile.copy(photoUrl = newPhotoUrl)
            }
            // Caso 2: Hay una foto actual y se elimina
            newPhotoUri == null && profile.photoUrl != null -> {
                firebaseStorage.getReferenceFromUrl(profile.photoUrl).delete().await()
                profile.copy(photoUrl = null)
            }
            // Caso 3: Hay una foto actual y se reemplaza por una nueva
            newPhotoUri != null && profile.photoUrl != null -> {
                firebaseStorage.getReferenceFromUrl(profile.photoUrl).delete().await()
                val newPhotoUrl = uploadPhoto(newPhotoUri, doctorId)
                profile.copy(photoUrl = newPhotoUrl)
            }
            // No se realiza ningún cambio en la foto
            else -> profile
        }

    private suspend fun uploadPhoto(photoUri: Uri, doctorId: String): String {
        val storageRef = firebaseStorage.reference.child("doctors_photos/$doctorId/${UUID.randomUUID()}")
        storageRef.putFile(photoUri).await()
        return storageRef.downloadUrl.await().toString()
    }
}