package com.bpdevop.mediccontrol.data.repository

import android.net.Uri
import com.bpdevop.mediccontrol.core.utils.UiState
import com.bpdevop.mediccontrol.data.model.Examination
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject

class ExaminationRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val firebaseStorage: FirebaseStorage,
) {

    // Agregar un examen con múltiples archivos
    suspend fun addExamination(patientId: String, examination: Examination, documentUris: List<Uri>): UiState<String> =
        runCatching {
            val documentUrls = documentUris.map { uploadDocument(it, patientId) }
            val newExamination = examination.copy(
                id = firestore.collection("examinations").document().id,
                files = documentUrls
            )
            firestore.collection("patients")
                .document(patientId)
                .collection("examinations")
                .document(newExamination.id)
                .set(newExamination)
                .await()
            UiState.Success("Examination added successfully")
        }.getOrElse {
            UiState.Error(it.message ?: "Error adding examination")
        }

    // Subida de archivos múltiples
    private suspend fun uploadDocument(documentUri: Uri, patientId: String): String {
        val storageRef = firebaseStorage.reference.child("examination_documents/$patientId/${UUID.randomUUID()}")
        storageRef.putFile(documentUri).await()
        return storageRef.downloadUrl.await().toString()
    }

    // Eliminar archivo del storage
    private suspend fun deleteDocument(fileUrl: String) {
        val storageRef = firebaseStorage.getReferenceFromUrl(fileUrl)
        storageRef.delete().await()
    }

    // Obtener el historial de exámenes de un paciente
    suspend fun getExaminationHistory(patientId: String): UiState<List<Examination>> =
        runCatching {
            val snapshot = firestore.collection("patients")
                .document(patientId)
                .collection("examinations")
                .get()
                .await()
            val examinations = snapshot.toObjects(Examination::class.java)
            UiState.Success(examinations)
        }.getOrElse {
            UiState.Error(it.message ?: "Error retrieving examination history")
        }

    suspend fun getExaminationById(patientId: String, examinationId: String): UiState<Examination> =
        runCatching {
            val snapshot = firestore.collection("patients")
                .document(patientId)
                .collection("examinations")
                .document(examinationId)
                .get()
                .await()
            val examination = snapshot.toObject(Examination::class.java)!!
            UiState.Success(examination)
        }.getOrElse {
            UiState.Error(it.message ?: "Error fetching examination")
        }

    // Actualizar un examen
    suspend fun updateExamination(patientId: String, examination: Examination, newDocumentUris: List<Uri>?, existingDocumentUrls: List<String>): UiState<String> =
        runCatching {
            // Eliminar archivos si se eliminaron de la lista
            val removedUrls = examination.files.filterNot { existingDocumentUrls.contains(it) }
            removedUrls.forEach { deleteDocument(it) }

            // Subir solo los nuevos documentos
            val newDocumentUrls = newDocumentUris?.map { uploadDocument(it, patientId) } ?: emptyList()

            val updatedExamination = examination.copy(
                files = existingDocumentUrls + newDocumentUrls
            )

            firestore.collection("patients")
                .document(patientId)
                .collection("examinations")
                .document(examination.id)
                .set(updatedExamination)
                .await()

            UiState.Success("Examination updated successfully")
        }.getOrElse {
            UiState.Error(it.message ?: "Error updating examination")
        }

    // Eliminar un examen
    suspend fun deleteExamination(patientId: String, examinationId: String): UiState<String> =
        runCatching {
            firestore.collection("patients")
                .document(patientId)
                .collection("examinations")
                .document(examinationId)
                .delete()
                .await()
            UiState.Success("Examination deleted successfully")
        }.getOrElse {
            UiState.Error(it.message ?: "Error deleting examination")
        }
}