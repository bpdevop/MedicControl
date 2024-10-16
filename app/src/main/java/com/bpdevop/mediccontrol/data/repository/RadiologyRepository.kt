package com.bpdevop.mediccontrol.data.repository

import android.net.Uri
import com.bpdevop.mediccontrol.core.utils.UiState
import com.bpdevop.mediccontrol.data.model.Radiology
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject

class RadiologyRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val firebaseStorage: FirebaseStorage,
) {

    // Agregar un estudio de radiología con múltiples archivos
    suspend fun addRadiology(patientId: String, radiology: Radiology, documentUris: List<Uri>): UiState<String> =
        runCatching {
            val documentUrls = documentUris.map { uploadDocument(it, patientId) }
            val newRadiology = radiology.copy(
                id = firestore.collection("radiology").document().id,
                files = documentUrls
            )
            firestore.collection("patients")
                .document(patientId)
                .collection("radiology")
                .document(newRadiology.id)
                .set(newRadiology)
                .await()
            UiState.Success("Radiology study added successfully")
        }.getOrElse {
            UiState.Error(it.message ?: "Error adding radiology study")
        }

    // Subida de archivos
    private suspend fun uploadDocument(documentUri: Uri, patientId: String): String {
        val storageRef = firebaseStorage.reference.child("radiology_documents/$patientId/${UUID.randomUUID()}")
        storageRef.putFile(documentUri).await()
        return storageRef.downloadUrl.await().toString()
    }

    // Eliminar archivo del storage
    private suspend fun deleteDocument(fileUrl: String) {
        val storageRef = firebaseStorage.getReferenceFromUrl(fileUrl)
        storageRef.delete().await()
    }

    // Obtener el historial de estudios de radiología de un paciente
    suspend fun getRadiologyHistory(patientId: String): UiState<List<Radiology>> =
        runCatching {
            val snapshot = firestore.collection("patients")
                .document(patientId)
                .collection("radiology")
                .get()
                .await()
            val radiologyRecords = snapshot.toObjects(Radiology::class.java)
            UiState.Success(radiologyRecords)
        }.getOrElse {
            UiState.Error(it.message ?: "Error retrieving radiology history")
        }

    // Obtener un estudio de radiología por ID
    suspend fun getRadiologyById(patientId: String, radiologyId: String): UiState<Radiology> =
        runCatching {
            val snapshot = firestore.collection("patients")
                .document(patientId)
                .collection("radiology")
                .document(radiologyId)
                .get()
                .await()
            val radiology = snapshot.toObject(Radiology::class.java)!!
            UiState.Success(radiology)
        }.getOrElse {
            UiState.Error(it.message ?: "Error fetching radiology study")
        }

    // Actualizar un estudio de radiología
    suspend fun updateRadiology(patientId: String, radiology: Radiology, newDocumentUris: List<Uri>?, existingDocumentUrls: List<String>): UiState<String> =
        runCatching {
            // Eliminar archivos si se eliminaron de la lista
            val removedUrls = radiology.files.filterNot { existingDocumentUrls.contains(it) }
            removedUrls.forEach { deleteDocument(it) }

            // Subir solo los nuevos documentos
            val newDocumentUrls = newDocumentUris?.map { uploadDocument(it, patientId) } ?: emptyList()

            val updatedRadiology = radiology.copy(
                files = existingDocumentUrls + newDocumentUrls
            )

            firestore.collection("patients")
                .document(patientId)
                .collection("radiology")
                .document(radiology.id)
                .set(updatedRadiology)
                .await()

            UiState.Success("Radiology study updated successfully")
        }.getOrElse {
            UiState.Error(it.message ?: "Error updating radiology study")
        }

    // Eliminar un estudio de radiología junto con sus archivos de almacenamiento
    suspend fun deleteRadiology(patientId: String, radiologyId: String): UiState<String> =
        runCatching {
            // Obtener el estudio de radiología y sus archivos
            val snapshot = firestore.collection("patients")
                .document(patientId)
                .collection("radiology")
                .document(radiologyId)
                .get()
                .await()

            val radiology = snapshot.toObject(Radiology::class.java)

            // Si hay archivos asociados, eliminarlos primero
            radiology?.files?.forEach { fileUrl ->
                deleteDocument(fileUrl)
            }

            // Eliminar el documento de Firestore
            firestore.collection("patients")
                .document(patientId)
                .collection("radiology")
                .document(radiologyId)
                .delete()
                .await()

            UiState.Success("Radiology study deleted successfully")
        }.getOrElse {
            UiState.Error(it.message ?: "Error deleting radiology study")
        }
}