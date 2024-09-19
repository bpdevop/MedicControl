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

    suspend fun getPatients(): UiState<List<Patient>> = authRepository.getCurrentUserId()?.let { doctorId ->
        runCatching {
            val snapshot = firestore.collection("patients")
                .whereEqualTo("doctorId", doctorId)
                .get()
                .await()

            val patients = snapshot.toObjects(Patient::class.java)
            UiState.Success(patients)
        }.getOrElse { e ->
            UiState.Error(e.message ?: "Error al obtener los pacientes")
        }
    } ?: UiState.Error("Error: no se pudo obtener el ID del doctor")

    suspend fun getPatientById(patientId: String): UiState<Patient> {
        return runCatching {
            val documentSnapshot = firestore.collection("patients").document(patientId).get().await()
            val patient = documentSnapshot.toObject(Patient::class.java)
            patient?.let { UiState.Success(it) } ?: UiState.Error("Paciente no encontrado")
        }.getOrElse {
            UiState.Error(it.message ?: "Error al obtener los detalles del paciente")
        }
    }

    suspend fun addPatient(patient: Patient, photoUri: Uri?): UiState<String> = authRepository.getCurrentUserId()?.let { doctorId ->
        runCatching {
            var photoUrl: String? = null

            // Si hay una imagen seleccionada, subirla a Firebase Storage
            if (photoUri != null) {
                val storageRef = firebaseStorage.reference.child("patients_photos/$doctorId/${UUID.randomUUID()}")
                storageRef.putFile(photoUri).await()
                photoUrl = storageRef.downloadUrl.await().toString()
            }

            // Crear el paciente con la URL de la imagen (si existe)
            val documentRef = firestore.collection("patients").document()
            val patientWithGeneratedId = patient.copy(id = documentRef.id, doctorId = doctorId, photoUrl = photoUrl)

            documentRef.set(patientWithGeneratedId).await()
            UiState.Success("Paciente agregado correctamente")
        }.getOrElse { e ->
            UiState.Error(e.message ?: "Error al agregar el paciente")
        }
    } ?: UiState.Error("Error: no se pudo obtener el ID del doctor")

    // Método optimizado para actualizar un paciente
    suspend fun updatePatient(patient: Patient, newPhotoUri: Uri?): UiState<String> =
        authRepository.getCurrentUserId()?.let { doctorId ->
            runCatching {
                val documentRef = firestore.collection("patients").document(patient.id)
                val updatedPatient = handlePhotoUpdate(patient, newPhotoUri, doctorId)

                // Actualizar los datos del paciente en Firestore
                documentRef.set(updatedPatient).await()
                UiState.Success("Paciente actualizado correctamente")
            }.getOrElse { e ->
                UiState.Error(e.message ?: "Error al actualizar el paciente")
            }
        } ?: UiState.Error("Error: no se pudo obtener el ID del doctor")

    // Método optimizado para eliminar un paciente
    suspend fun deletePatient(patient: Patient): UiState<String> =
        authRepository.getCurrentUserId()?.let {
            runCatching {
                // Eliminar imagen de Firebase Storage si existe
                patient.photoUrl?.let { photoUrl ->
                    firebaseStorage.getReferenceFromUrl(photoUrl).delete().await()
                }

                // Eliminar los datos del paciente en Firestore
                firestore.collection("patients").document(patient.id).delete().await()
                UiState.Success("Paciente eliminado correctamente")
            }.getOrElse { e ->
                UiState.Error(e.message ?: "Error al eliminar el paciente")
            }
        } ?: UiState.Error("Error: no se pudo obtener el ID del doctor")

    // Función auxiliar para manejar la actualización/eliminación de la imagen del paciente
    private suspend fun handlePhotoUpdate(patient: Patient, newPhotoUri: Uri?, doctorId: String): Patient {
        return when {
            // Subir nueva imagen y eliminar la anterior si es necesario
            newPhotoUri != null -> {
                patient.photoUrl?.let { firebaseStorage.getReferenceFromUrl(it).delete().await() }
                val newPhotoUrl = uploadNewPhoto(newPhotoUri, doctorId)
                patient.copy(photoUrl = newPhotoUrl)
            }
            // Eliminar la imagen si se ha removido en la interfaz
            newPhotoUri == null && patient.photoUrl != null -> {
                firebaseStorage.getReferenceFromUrl(patient.photoUrl).delete().await()
                patient.copy(photoUrl = null)
            }

            else -> patient // No se hizo cambio en la imagen
        }
    }

    // Función auxiliar para subir una nueva imagen a Firebase Storage
    private suspend fun uploadNewPhoto(photoUri: Uri, doctorId: String): String {
        val storageRef = firebaseStorage.reference.child("patients_photos/$doctorId/${UUID.randomUUID()}")
        storageRef.putFile(photoUri).await()
        return storageRef.downloadUrl.await().toString()
    }
}
