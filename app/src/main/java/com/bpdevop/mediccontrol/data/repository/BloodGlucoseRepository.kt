package com.bpdevop.mediccontrol.data.repository

import com.bpdevop.mediccontrol.core.utils.UiState
import com.bpdevop.mediccontrol.data.model.BloodGlucose
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class BloodGlucoseRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
) {

    suspend fun addBloodGlucoseToPatient(patientId: String, bloodGlucose: BloodGlucose): UiState<String> =
        runCatching {
            val newBloodGlucose = bloodGlucose.copy(id = firestore.collection("blood_glucose").document().id)
            firestore.collection("patients")
                .document(patientId)
                .collection("blood_glucose")
                .document(newBloodGlucose.id)
                .set(newBloodGlucose)
                .await()
            UiState.Success("Glicemia agregada correctamente")
        }.getOrElse {
            UiState.Error(it.message ?: "Error al agregar el registro de glicemia")
        }

    suspend fun getBloodGlucoseHistory(patientId: String): UiState<List<BloodGlucose>> =
        runCatching {
            val snapshot = firestore.collection("patients")
                .document(patientId)
                .collection("blood_glucose")
                .get()
                .await()
            val bloodGlucoseRecords = snapshot.toObjects(BloodGlucose::class.java)
            UiState.Success(bloodGlucoseRecords)
        }.getOrElse {
            UiState.Error(it.message ?: "Error al obtener el historial de glicemia")
        }

    suspend fun getLastBloodGlucoseRecord(patientId: String): UiState<BloodGlucose?> =
        runCatching {
            val snapshot = firestore.collection("patients")
                .document(patientId)
                .collection("blood_glucose")
                .orderBy("date", Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .await()

            val lastBloodGlucose = snapshot.documents.firstOrNull()?.toObject(BloodGlucose::class.java)
            UiState.Success(lastBloodGlucose)
        }.getOrElse {
            UiState.Error(it.message ?: "Error al obtener el último registro de glicemia")
        }

    suspend fun updateBloodGlucose(patientId: String, bloodGlucose: BloodGlucose): UiState<String> =
        runCatching {
            firestore.collection("patients")
                .document(patientId)
                .collection("blood_glucose")
                .document(bloodGlucose.id)
                .set(bloodGlucose)
                .await()
            UiState.Success("Glicemia actualizada correctamente")
        }.getOrElse {
            UiState.Error(it.message ?: "Error al actualizar el registro de glicemia")
        }

    suspend fun deleteBloodGlucose(patientId: String, bloodGlucoseId: String): UiState<String> =
        runCatching {
            firestore.collection("patients")
                .document(patientId)
                .collection("blood_glucose")
                .document(bloodGlucoseId)
                .delete()
                .await()
            UiState.Success("Glicemia eliminada correctamente")
        }.getOrElse {
            UiState.Error(it.message ?: "Error al eliminar el registro de glicemia")
        }
}
