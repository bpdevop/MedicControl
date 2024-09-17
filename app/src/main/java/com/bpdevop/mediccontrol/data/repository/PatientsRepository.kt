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
}
