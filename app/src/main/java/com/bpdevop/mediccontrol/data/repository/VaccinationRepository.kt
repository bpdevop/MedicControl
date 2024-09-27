package com.bpdevop.mediccontrol.data.repository

import com.bpdevop.mediccontrol.core.utils.UiState
import com.bpdevop.mediccontrol.data.model.Vaccine
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class VaccinationRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {

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
}