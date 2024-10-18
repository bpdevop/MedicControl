package com.bpdevop.mediccontrol.data.repository

import com.bpdevop.mediccontrol.core.utils.UiState
import com.bpdevop.mediccontrol.data.model.BloodPressure
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class BloodPressureRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
) {

    suspend fun addBloodPressureToPatient(patientId: String, bloodPressure: BloodPressure): UiState<String> =
        runCatching {
            val newBloodPressure = bloodPressure.copy(id = firestore.collection("blood_pressure").document().id)
            firestore.collection("patients")
                .document(patientId)
                .collection("blood_pressure")
                .document(newBloodPressure.id)
                .set(newBloodPressure)
                .await()
            UiState.Success("Presión arterial agregada correctamente")
        }.getOrElse {
            UiState.Error(it.message ?: "Error al agregar la presión arterial")
        }

    suspend fun getBloodPressureHistory(patientId: String): UiState<List<BloodPressure>> =
        runCatching {
            val snapshot = firestore.collection("patients")
                .document(patientId)
                .collection("blood_pressure")
                .get()
                .await()
            val bloodPressures = snapshot.toObjects(BloodPressure::class.java)
            UiState.Success(bloodPressures)
        }.getOrElse {
            UiState.Error(it.message ?: "Error al obtener el historial de presión arterial")
        }

    // Método para obtener el último registro de presión arterial
    suspend fun getLastBloodPressure(patientId: String): UiState<BloodPressure> =
        runCatching {
            val snapshot = firestore.collection("patients")
                .document(patientId)
                .collection("blood_pressure")
                .orderBy("date", Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .await()

            val bloodPressure = snapshot.toObjects(BloodPressure::class.java).firstOrNull()
            bloodPressure?.let {
                UiState.Success(it)
            } ?: UiState.Error("No hay registros de presión arterial disponibles")
        }.getOrElse {
            UiState.Error(it.message ?: "Error al obtener el último registro de presión arterial")
        }

    suspend fun updateBloodPressure(patientId: String, bloodPressure: BloodPressure): UiState<String> =
        runCatching {
            firestore.collection("patients")
                .document(patientId)
                .collection("blood_pressure")
                .document(bloodPressure.id)
                .set(bloodPressure)
                .await()
            UiState.Success("Presión arterial actualizada correctamente")
        }.getOrElse {
            UiState.Error(it.message ?: "Error al actualizar la presión arterial")
        }

    suspend fun deleteBloodPressure(patientId: String, bloodPressureId: String): UiState<String> =
        runCatching {
            firestore.collection("patients")
                .document(patientId)
                .collection("blood_pressure")
                .document(bloodPressureId)
                .delete()
                .await()
            UiState.Success("Presión arterial eliminada correctamente")
        }.getOrElse {
            UiState.Error(it.message ?: "Error al eliminar la presión arterial")
        }
}
