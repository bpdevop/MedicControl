package com.bpdevop.mediccontrol.data.repository

import com.bpdevop.mediccontrol.core.utils.UiState
import com.bpdevop.mediccontrol.data.model.MedicalAppointment
import com.bpdevop.mediccontrol.data.model.PatientAppointment
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.Calendar
import java.util.Date
import javax.inject.Inject

class AppointmentRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val authRepository: AuthRepository,
    private val patientsRepository: PatientsRepository,
) {

    suspend fun addAppointment(patientId: String, appointment: PatientAppointment): UiState<String> =
        authRepository.getCurrentUserId()?.let { doctorId ->
            runCatching {
                // Generar un nuevo ID para la cita
                val appointmentId = firestore.collection("appointments").document().id

                // Asignar el ID de la cita directamente
                val patientAppointment = appointment.copy(id = appointmentId)

                // Obtener los detalles del paciente
                val patientDetails = patientsRepository.getPatientById(patientId)
                if (patientDetails is UiState.Success) {
                    val patient = patientDetails.data
                    // Preparar el objeto de cita para el doctor
                    val doctorAppointment = MedicalAppointment(
                        id = appointmentId,
                        patientId = patientId,
                        patientName = patient.name,
                        phone = patient.phone ?: "",
                        date = appointment.date,
                        time = appointment.time,
                        visitType = appointment.visitType
                    )

                    // Guardar en la colección del médico
                    firestore.collection("doctors")
                        .document(doctorId)
                        .collection("appointments")
                        .document(appointmentId)
                        .set(doctorAppointment)
                        .await()

                    // Guardar en la colección del paciente
                    firestore.collection("patients")
                        .document(patientId)
                        .collection("appointments")
                        .document(appointmentId)
                        .set(patientAppointment)
                        .await()

                    UiState.Success("Cita asignada correctamente")
                } else {
                    UiState.Error("Error al obtener los datos del paciente")
                }
            }.getOrElse {
                UiState.Error(it.message ?: "Error al asignar la cita")
            }
        } ?: UiState.Error("No se encontró el ID del doctor")

    suspend fun getPatientAppointmentHistory(patientId: String): UiState<List<PatientAppointment>> =
        runCatching {
            val snapshot = firestore.collection("patients")
                .document(patientId)
                .collection("appointments")
                .get()
                .await()
            val appointments = snapshot.toObjects(PatientAppointment::class.java)
            UiState.Success(appointments)
        }.getOrElse {
            UiState.Error(it.message ?: "Error al obtener el historial de citas del paciente")
        }

    suspend fun getDoctorAppointmentHistory(date: Date): UiState<List<MedicalAppointment>> =
        authRepository.getCurrentUserId()?.let { doctorId ->
            runCatching {
                // Convertir la fecha seleccionada al formato de inicio y fin del día
                val calendar = Calendar.getInstance().apply {
                    time = date
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                val startOfDay = calendar.time

                calendar[Calendar.HOUR_OF_DAY] = 23
                calendar[Calendar.MINUTE] = 59
                calendar[Calendar.SECOND] = 59
                calendar[Calendar.MILLISECOND] = 999
                val endOfDay = calendar.time

                // Realizar la consulta a Firestore filtrando entre el inicio y el fin del día seleccionado
                val snapshot = firestore.collection("doctors")
                    .document(doctorId)
                    .collection("appointments")
                    .whereGreaterThanOrEqualTo("date", startOfDay)
                    .whereLessThanOrEqualTo("date", endOfDay)
                    .get()
                    .await()

                val appointments = snapshot.toObjects(MedicalAppointment::class.java)
                UiState.Success(appointments)
            }.getOrElse {
                UiState.Error(it.message ?: "Error al obtener el historial de citas del médico")
            }
        } ?: UiState.Error("No se encontró el ID del médico")


    suspend fun updateAppointment(patientId: String, updatedAppointment: PatientAppointment): UiState<String> =
        authRepository.getCurrentUserId()?.let { doctorId ->
            runCatching {
                val appointmentId = updatedAppointment.id

                // Obtener los detalles del paciente
                val patientDetails = patientsRepository.getPatientById(patientId)
                if (patientDetails is UiState.Success) {
                    val patient = patientDetails.data

                    // Preparar el objeto de cita actualizado para el doctor
                    val doctorAppointment = MedicalAppointment(
                        id = appointmentId,
                        patientId = patientId,
                        patientName = patient.name,
                        phone = patient.phone ?: "",
                        date = updatedAppointment.date,
                        time = updatedAppointment.time,
                        visitType = updatedAppointment.visitType
                    )

                    // Actualizar en la colección del médico
                    firestore.collection("doctors")
                        .document(doctorId)
                        .collection("appointments")
                        .document(appointmentId)
                        .set(doctorAppointment)
                        .await()

                    // Actualizar en la colección del paciente
                    firestore.collection("patients")
                        .document(patientId)
                        .collection("appointments")
                        .document(appointmentId)
                        .set(updatedAppointment)
                        .await()

                    UiState.Success("Cita actualizada correctamente")
                } else {
                    UiState.Error("Error al obtener los datos del paciente")
                }
            }.getOrElse {
                UiState.Error(it.message ?: "Error al actualizar la cita")
            }
        } ?: UiState.Error("No se encontró el ID del doctor")


    suspend fun deleteAppointment(patientId: String, appointmentId: String): UiState<String> =
        authRepository.getCurrentUserId()?.let { doctorId ->
            runCatching {
                // Eliminar la cita de la colección del médico
                firestore.collection("doctors")
                    .document(doctorId)
                    .collection("appointments")
                    .document(appointmentId)
                    .delete()
                    .await()

                // Eliminar la cita de la colección del paciente
                firestore.collection("patients")
                    .document(patientId)
                    .collection("appointments")
                    .document(appointmentId)
                    .delete()
                    .await()

                UiState.Success("Cita eliminada correctamente")
            }.getOrElse {
                UiState.Error(it.message ?: "Error al eliminar la cita")
            }
        } ?: UiState.Error("No se encontró el ID del doctor")
}