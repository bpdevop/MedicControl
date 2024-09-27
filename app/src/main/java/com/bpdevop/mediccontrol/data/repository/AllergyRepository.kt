package com.bpdevop.mediccontrol.data.repository

import com.bpdevop.mediccontrol.core.utils.UiState
import com.bpdevop.mediccontrol.data.model.Allergy
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AllergyRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
) {

    suspend fun addAllergyToPatient(patientId: String, allergy: Allergy): UiState<String> =
        runCatching {
            val newAllergy = allergy.copy(id = firestore.collection("allergies").document().id)
            firestore.collection("patients")
                .document(patientId)
                .collection("allergies")
                .document(newAllergy.id)
                .set(newAllergy)
                .await()
            UiState.Success("Alergia agregada correctamente")
        }.getOrElse {
            UiState.Error(it.message ?: "Error al agregar la alergia")
        }

    suspend fun getAllergyHistory(patientId: String): UiState<List<Allergy>> =
        runCatching {
            val snapshot = firestore.collection("patients")
                .document(patientId)
                .collection("allergies")
                .get()
                .await()
            val allergies = snapshot.toObjects(Allergy::class.java)
            UiState.Success(allergies)
        }.getOrElse {
            UiState.Error(it.message ?: "Error al obtener el historial de alergias")
        }

    suspend fun updateAllergy(patientId: String, allergy: Allergy): UiState<String> =
        runCatching {
            firestore.collection("patients")
                .document(patientId)
                .collection("allergies")
                .document(allergy.id)
                .set(allergy)
                .await()
            UiState.Success("Alergia actualizada correctamente")
        }.getOrElse {
            UiState.Error(it.message ?: "Error al actualizar la alergia")
        }

    suspend fun deleteAllergy(patientId: String, allergyId: String): UiState<String> =
        runCatching {
            firestore.collection("patients")
                .document(patientId)
                .collection("allergies")
                .document(allergyId)
                .delete()
                .await()
            UiState.Success("Alergia eliminada correctamente")
        }.getOrElse {
            UiState.Error(it.message ?: "Error al eliminar la alergia")
        }
}
