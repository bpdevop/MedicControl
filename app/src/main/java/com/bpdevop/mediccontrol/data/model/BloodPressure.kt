package com.bpdevop.mediccontrol.data.model

import com.bpdevop.mediccontrol.core.utils.DateAsLongSerializer
import com.google.firebase.firestore.ServerTimestamp
import kotlinx.serialization.Serializable
import java.util.Date

@Serializable
data class BloodPressure(
    val id: String = "",
    val systolic: Int = 0,     // Sistólica
    val diastolic: Int = 0,    // Diastólica
    val pulse: Int = 0,        // Pulso
    @Serializable(with = DateAsLongSerializer::class)
    @ServerTimestamp val date: Date? = null,  // Fecha de registro
    @Serializable(with = DateAsLongSerializer::class)
    @ServerTimestamp val time: Date? = null,     // Hora de registro
    val notes: String? = null,  // Notas opcionales
)
