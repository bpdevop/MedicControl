package com.bpdevop.mediccontrol.data.repository

import android.net.Uri
import com.bpdevop.mediccontrol.core.utils.UiState
import com.bpdevop.mediccontrol.data.model.LabTestItem
import com.bpdevop.mediccontrol.data.model.Laboratory
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject

class LaboratoryRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val firebaseStorage: FirebaseStorage,
) {

    // Agregar un laboratorio con múltiples archivos
    suspend fun addLaboratory(patientId: String, laboratory: Laboratory, documentUris: List<Uri>): UiState<String> =
        runCatching {
            val documentUrls = documentUris.map { uploadDocument(it, patientId) }
            val newLaboratory = laboratory.copy(
                id = firestore.collection("laboratories").document().id,
                files = documentUrls
            )
            firestore.collection("patients")
                .document(patientId)
                .collection("laboratories")
                .document(newLaboratory.id)
                .set(newLaboratory)
                .await()
            UiState.Success("Laboratory record added successfully")
        }.getOrElse {
            UiState.Error(it.message ?: "Error adding laboratory record")
        }

    // Subida de archivos múltiples
    private suspend fun uploadDocument(documentUri: Uri, patientId: String): String {
        val storageRef = firebaseStorage.reference.child("laboratory_documents/$patientId/${UUID.randomUUID()}")
        storageRef.putFile(documentUri).await()
        return storageRef.downloadUrl.await().toString()
    }

    // Eliminar archivo del storage
    private suspend fun deleteDocument(fileUrl: String) {
        val storageRef = firebaseStorage.getReferenceFromUrl(fileUrl)
        storageRef.delete().await()
    }

    // Obtener el historial de laboratorios de un paciente
    suspend fun getLaboratoryHistory(patientId: String): UiState<List<Laboratory>> =
        runCatching {
            val snapshot = firestore.collection("patients")
                .document(patientId)
                .collection("laboratories")
                .get()
                .await()
            val laboratories = snapshot.toObjects(Laboratory::class.java)
            UiState.Success(laboratories)
        }.getOrElse {
            UiState.Error(it.message ?: "Error retrieving laboratory history")
        }

    suspend fun getLaboratoryById(patientId: String, laboratoryId: String): UiState<Laboratory> =
        runCatching {
            val snapshot = firestore.collection("patients")
                .document(patientId)
                .collection("laboratories")
                .document(laboratoryId)
                .get()
                .await()
            val laboratory = snapshot.toObject(Laboratory::class.java)!!
            UiState.Success(laboratory)
        }.getOrElse {
            UiState.Error(it.message ?: "Error fetching laboratory record")
        }

    // Nuevo método para el historial médico del paciente
    fun getLaboratorySummaryForPatient(patientId: String): Flow<UiState<List<LabTestItem>>> = flow {
        try {
            val snapshot = firestore.collection("patients")
                .document(patientId)
                .collection("laboratories")
                .get()
                .await()

            // Resumimos los datos del laboratorio, obteniendo solo los tests
            val labTestsSummary = snapshot.toObjects(Laboratory::class.java)
                .flatMap { it.tests } // Obtenemos solo las pruebas de laboratorio (LabTestItem)

            emit(UiState.Success(labTestsSummary))
        } catch (e: Exception) {
            emit(UiState.Error(e.message ?: "Error retrieving laboratory summary"))
        }
    }

    // Actualizar un registro de laboratorio
    suspend fun updateLaboratory(patientId: String, laboratory: Laboratory, newDocumentUris: List<Uri>?, existingDocumentUrls: List<String>): UiState<String> =
        runCatching {
            // Eliminar archivos si se eliminaron de la lista
            val removedUrls = laboratory.files.filterNot { existingDocumentUrls.contains(it) }
            removedUrls.forEach { deleteDocument(it) }

            // Subir solo los nuevos documentos
            val newDocumentUrls = newDocumentUris?.map { uploadDocument(it, patientId) } ?: emptyList()

            val updatedLaboratory = laboratory.copy(
                files = existingDocumentUrls + newDocumentUrls
            )

            firestore.collection("patients")
                .document(patientId)
                .collection("laboratories")
                .document(laboratory.id)
                .set(updatedLaboratory)
                .await()

            UiState.Success("Laboratory record updated successfully")
        }.getOrElse {
            UiState.Error(it.message ?: "Error updating laboratory record")
        }

    // Eliminar un registro de laboratorio junto con sus archivos de almacenamiento
    suspend fun deleteLaboratory(patientId: String, laboratoryId: String): UiState<String> =
        runCatching {
            // Obtener el laboratorio y sus archivos
            val snapshot = firestore.collection("patients")
                .document(patientId)
                .collection("laboratories")
                .document(laboratoryId)
                .get()
                .await()

            val laboratory = snapshot.toObject(Laboratory::class.java)

            // Si hay archivos asociados, eliminarlos primero
            laboratory?.files?.forEach { fileUrl ->
                deleteDocument(fileUrl)
            }

            // Eliminar el documento de Firestore
            firestore.collection("patients")
                .document(patientId)
                .collection("laboratories")
                .document(laboratoryId)
                .delete()
                .await()

            UiState.Success("Laboratory record deleted successfully")
        }.getOrElse {
            UiState.Error(it.message ?: "Error deleting laboratory record")
        }

}
