package com.bpdevop.mediccontrol.data.repository

import com.bpdevop.mediccontrol.core.utils.UiState
import com.bpdevop.mediccontrol.data.model.OxygenSaturation
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class OxygenSaturationRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
) {

    suspend fun addOxygenSaturationToPatient(patientId: String, oxygenSaturation: OxygenSaturation): UiState<String> =
        runCatching {
            val newOxygenSaturation = oxygenSaturation.copy(id = firestore.collection("oxygen_saturation").document().id)
            firestore.collection("patients")
                .document(patientId)
                .collection("oxygen_saturation")
                .document(newOxygenSaturation.id)
                .set(newOxygenSaturation)
                .await()
            UiState.Success("Saturación de oxígeno agregada correctamente")
        }.getOrElse {
            UiState.Error(it.message ?: "Error al agregar la saturación de oxígeno")
        }

    suspend fun getOxygenSaturationHistory(patientId: String): UiState<List<OxygenSaturation>> =
        runCatching {
            val snapshot = firestore.collection("patients")
                .document(patientId)
                .collection("oxygen_saturation")
                .get()
                .await()
            val oxygenSaturations = snapshot.toObjects(OxygenSaturation::class.java)
            UiState.Success(oxygenSaturations)
        }.getOrElse {
            UiState.Error(it.message ?: "Error al obtener el historial de saturación de oxígeno")
        }

    suspend fun getLastOxygenSaturation(patientId: String): UiState<OxygenSaturation?> =
        runCatching {
            val snapshot = firestore.collection("patients")
                .document(patientId)
                .collection("oxygen_saturation")
                .orderBy("date", Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .await()

            val oxygenSaturation = snapshot.documents.firstOrNull()?.toObject(OxygenSaturation::class.java)
            UiState.Success(oxygenSaturation)
        }.getOrElse {
            UiState.Error(it.message ?: "Error al obtener la última saturación de oxígeno")
        }


    suspend fun updateOxygenSaturation(patientId: String, oxygenSaturation: OxygenSaturation): UiState<String> =
        runCatching {
            firestore.collection("patients")
                .document(patientId)
                .collection("oxygen_saturation")
                .document(oxygenSaturation.id)
                .set(oxygenSaturation)
                .await()
            UiState.Success("Saturación de oxígeno actualizada correctamente")
        }.getOrElse {
            UiState.Error(it.message ?: "Error al actualizar la saturación de oxígeno")
        }

    suspend fun deleteOxygenSaturation(patientId: String, oxygenSaturationId: String): UiState<String> =
        runCatching {
            firestore.collection("patients")
                .document(patientId)
                .collection("oxygen_saturation")
                .document(oxygenSaturationId)
                .delete()
                .await()
            UiState.Success("Saturación de oxígeno eliminada correctamente")
        }.getOrElse {
            UiState.Error(it.message ?: "Error al eliminar la saturación de oxígeno")
        }
}