package com.bpdevop.mediccontrol.data.repository

import com.bpdevop.mediccontrol.core.utils.UiState
import com.bpdevop.mediccontrol.data.model.Vaccine
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class VaccinationRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {

    suspend fun getVaccinationHistory(patientId: String): UiState<List<Vaccine>> {
        return try {
            val vaccines = firestore.collection("patients")
                .document(patientId)
                .collection("vaccinations")
                .get()
                .await()
                .toObjects(Vaccine::class.java)
            UiState.Success(vaccines)
        } catch (e: Exception) {
            UiState.Error(e.message ?: "Error al obtener el historial de vacunas")
        }
    }

    suspend fun addVaccineToPatient(patientId: String, vaccine: Vaccine): UiState<String> {
        return runCatching {
            // Generar el ID de la vacuna si es necesario
            val vaccineId = firestore.collection("patients")
                .document(patientId)
                .collection("vaccinations")
                .document().id

            val newVaccine = vaccine.copy(id = vaccineId)

            firestore.collection("patients")
                .document(patientId)
                .collection("vaccinations")
                .document(vaccineId)
                .set(newVaccine).await()

            UiState.Success("Vacuna agregada correctamente")
        }.getOrElse {
            UiState.Error(it.message ?: "Error al agregar la vacuna")
        }
    }

    suspend fun deleteVaccine(patientId: String, vaccineId: String): UiState<String> {
        return runCatching {
            firestore.collection("patients")
                .document(patientId)
                .collection("vaccinations")
                .document(vaccineId)
                .delete()
                .await()
            UiState.Success("Vacuna eliminada correctamente")
        }.getOrElse {
            UiState.Error(it.message ?: "Error al eliminar la vacuna")
        }
    }

    suspend fun editVaccine(patientId: String, vaccine: Vaccine): UiState<String> {
        return runCatching {
            firestore.collection("patients")
                .document(patientId)
                .collection("vaccinations")
                .document(vaccine.id)
                .set(vaccine)
                .await()
            UiState.Success("Vacuna editada correctamente")
        }.getOrElse {
            UiState.Error(it.message ?: "Error al editar la vacuna")
        }
    }

}