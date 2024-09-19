package com.bpdevop.mediccontrol.data.repository

import android.net.Uri
import com.bpdevop.mediccontrol.core.utils.UiState
import com.bpdevop.mediccontrol.data.model.Patient
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject

class PatientsRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val firebaseStorage: FirebaseStorage,
    private val authRepository: AuthRepository,
) {

    suspend fun getPatients(): UiState<List<Patient>> =
        authRepository.getCurrentUserId()?.let { doctorId ->
            runCatching {
                val patients = firestore.collection("patients")
                    .whereEqualTo("doctorId", doctorId)
                    .get()
                    .await()
                    .toObjects(Patient::class.java)
                UiState.Success(patients)
            }.getOrElse { UiState.Error(it.message ?: "Error al obtener los pacientes") }
        } ?: UiState.Error("No se encontró el ID del doctor")

    suspend fun getPatientById(patientId: String): UiState<Patient> =
        runCatching {
            firestore.collection("patients")
                .document(patientId)
                .get()
                .await()
                .toObject(Patient::class.java)?.let { UiState.Success(it) }
                ?: UiState.Error("Paciente no encontrado")
        }.getOrElse { UiState.Error(it.message ?: "Error al obtener los detalles del paciente") }

    suspend fun addPatient(patient: Patient, photoUri: Uri?): UiState<String> =
        authRepository.getCurrentUserId()?.let { doctorId ->
            runCatching {
                val photoUrl = photoUri?.let { uploadPhoto(it, doctorId) }
                val newPatient = patient.copy(id = firestore.collection("patients").document().id, doctorId = doctorId, photoUrl = photoUrl)
                firestore.collection("patients").document(newPatient.id).set(newPatient).await()
                UiState.Success("Paciente agregado correctamente")
            }.getOrElse { UiState.Error(it.message ?: "Error al agregar el paciente") }
        } ?: UiState.Error("No se encontró el ID del doctor")

    suspend fun updatePatient(patient: Patient, newPhotoUri: Uri?): UiState<String> =
        authRepository.getCurrentUserId()?.let { doctorId ->
            runCatching {
                val updatedPatient = handlePhotoUpdate(patient, newPhotoUri, doctorId)
                firestore.collection("patients").document(updatedPatient.id).set(updatedPatient).await()
                UiState.Success("Paciente actualizado correctamente")
            }.getOrElse { UiState.Error(it.message ?: "Error al actualizar el paciente") }
        } ?: UiState.Error("No se encontró el ID del doctor")

    suspend fun deletePatient(patient: Patient): UiState<String> =
        authRepository.getCurrentUserId()?.let {
            runCatching {
                patient.photoUrl?.let { url -> firebaseStorage.getReferenceFromUrl(url).delete().await() }
                firestore.collection("patients").document(patient.id).delete().await()
                UiState.Success("Paciente eliminado correctamente")
            }.getOrElse { UiState.Error(it.message ?: "Error al eliminar el paciente") }
        } ?: UiState.Error("No se encontró el ID del doctor")

    private suspend fun handlePhotoUpdate(patient: Patient, newPhotoUri: Uri?, doctorId: String): Patient =
        when {
            newPhotoUri != null && patient.photoUrl == null -> {
                val newPhotoUrl = uploadPhoto(newPhotoUri, doctorId)
                patient.copy(photoUrl = newPhotoUrl)
            }
            newPhotoUri != null && patient.photoUrl != null -> {
                firebaseStorage.getReferenceFromUrl(patient.photoUrl).delete().await()
                val newPhotoUrl = uploadPhoto(newPhotoUri, doctorId)
                patient.copy(photoUrl = newPhotoUrl)
            }
            newPhotoUri == null && patient.photoUrl != null -> {
                firebaseStorage.getReferenceFromUrl(patient.photoUrl).delete().await()
                patient.copy(photoUrl = null)
            }
            else -> patient
        }

    private suspend fun uploadPhoto(photoUri: Uri, doctorId: String): String {
        val storageRef = firebaseStorage.reference.child("patients_photos/$doctorId/${UUID.randomUUID()}")
        storageRef.putFile(photoUri).await()
        return storageRef.downloadUrl.await().toString()
    }
}