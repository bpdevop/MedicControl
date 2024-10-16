package com.bpdevop.mediccontrol.data.model

import com.bpdevop.mediccontrol.core.utils.DateAsLongSerializer
import com.google.firebase.firestore.ServerTimestamp
import kotlinx.serialization.Serializable
import java.util.Date

@Serializable
data class MedicalAppointment(
    val id: String = "",
    val patientId: String = "",
    val patientName: String = "",
    val phone: String? = "",
    @Serializable(with = DateAsLongSerializer::class)
    @ServerTimestamp val date: Date? = null,
    @Serializable(with = DateAsLongSerializer::class)
    val time: Date? = null,
    val visitType: VisitType = VisitType.NEW,
    val notificationSent: Boolean = false,
)

enum class VisitType {
    NEW,          // Nueva consulta
    FOLLOW_UP,    // Consulta de seguimiento
    EMERGENCY,    // Emergencia
}