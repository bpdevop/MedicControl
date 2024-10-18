package com.bpdevop.mediccontrol.data.repository

import android.net.Uri
import com.bpdevop.mediccontrol.core.utils.UiState
import com.bpdevop.mediccontrol.data.model.Prescription
import com.bpdevop.mediccontrol.data.model.PrescriptionItem
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject

class PrescriptionRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val firebaseStorage: FirebaseStorage,
) {

    // Agregar una receta con múltiples archivos
    suspend fun addPrescription(patientId: String, prescription: Prescription, documentUris: List<Uri>): UiState<String> =
        runCatching {
            val documentUrls = documentUris.map { uploadDocument(it, patientId) }
            val newPrescription = prescription.copy(
                id = firestore.collection("prescriptions").document().id,
                files = documentUrls
            )
            firestore.collection("patients")
                .document(patientId)
                .collection("prescriptions")
                .document(newPrescription.id)
                .set(newPrescription)
                .await()
            UiState.Success("Prescription added successfully")
        }.getOrElse {
            UiState.Error(it.message ?: "Error adding prescription")
        }

    // Subida de archivos múltiples
    private suspend fun uploadDocument(documentUri: Uri, patientId: String): String {
        val storageRef = firebaseStorage.reference.child("prescription_documents/$patientId/${UUID.randomUUID()}")
        storageRef.putFile(documentUri).await()
        return storageRef.downloadUrl.await().toString()
    }

    // Obtener el historial de recetas de un paciente
    suspend fun getPrescriptionHistory(patientId: String): UiState<List<Prescription>> =
        runCatching {
            val snapshot = firestore.collection("patients")
                .document(patientId)
                .collection("prescriptions")
                .get()
                .await()
            val prescriptions = snapshot.toObjects(Prescription::class.java)
            UiState.Success(prescriptions)
        }.getOrElse {
            UiState.Error(it.message ?: "Error retrieving prescription history")
        }

    suspend fun getPrescriptionById(patientId: String, prescriptionId: String): UiState<Prescription> =
        runCatching {
            val snapshot = firestore.collection("patients")
                .document(patientId)
                .collection("prescriptions")
                .document(prescriptionId)
                .get()
                .await()
            val prescription = snapshot.toObject(Prescription::class.java)!!
            UiState.Success(prescription)
        }.getOrElse {
            UiState.Error(it.message ?: "Error fetching prescription")
        }

    // Método para obtener el resumen de prescripciones de un paciente
    fun getPrescriptionSummaryForPatient(patientId: String): Flow<UiState<List<PrescriptionItem>>> = flow {
        try {
            val snapshot = firestore.collection("patients")
                .document(patientId)
                .collection("prescriptions")
                .get()
                .await()

            // Obtenemos solo la lista de medicamentos dentro de cada prescripción
            val prescriptionSummary = snapshot.toObjects(Prescription::class.java)
                .flatMap { it.medications } // Obtenemos solo los medicamentos (PrescriptionItem)

            emit(UiState.Success(prescriptionSummary))
        } catch (e: Exception) {
            emit(UiState.Error(e.message ?: "Error retrieving prescription summary"))
        }
    }


    // Actualizar una receta
    suspend fun updatePrescription(patientId: String, prescription: Prescription, newDocumentUris: List<Uri>?, existingDocumentUrls: List<String>): UiState<String> =
        runCatching {
            // Eliminar archivos si se eliminaron de la lista
            val removedUrls = prescription.files.filterNot { existingDocumentUrls.contains(it) }
            removedUrls.forEach { deleteDocument(it) }

            // Subir solo los nuevos documentos
            val newDocumentUrls = newDocumentUris?.map { uploadDocument(it, patientId) } ?: emptyList()

            val updatedPrescription = prescription.copy(
                files = existingDocumentUrls + newDocumentUrls
            )

            firestore.collection("patients")
                .document(patientId)
                .collection("prescriptions")
                .document(prescription.id)
                .set(updatedPrescription)
                .await()

            UiState.Success("Prescription updated successfully")
        }.getOrElse {
            UiState.Error(it.message ?: "Error updating prescription")
        }

    // Eliminar una receta
    suspend fun deletePrescription(patientId: String, prescriptionId: String): UiState<String> =
        runCatching {
            firestore.collection("patients")
                .document(patientId)
                .collection("prescriptions")
                .document(prescriptionId)
                .delete()
                .await()
            UiState.Success("Prescription deleted successfully")
        }.getOrElse {
            UiState.Error(it.message ?: "Error deleting prescription")
        }

    // Eliminar archivo del storage
    private suspend fun deleteDocument(fileUrl: String) {
        val storageRef = firebaseStorage.getReferenceFromUrl(fileUrl)
        storageRef.delete().await()
    }
}
