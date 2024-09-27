package com.bpdevop.mediccontrol.data.model

import com.bpdevop.mediccontrol.core.utils.DateAsLongSerializer
import com.google.firebase.firestore.ServerTimestamp
import kotlinx.serialization.Serializable
import java.util.Date

@Serializable
data class OxygenSaturation(
    val id: String = "",
    val saturation: Int = 0, // Nivel de saturación de oxígeno (%)
    val pulse: Int = 0, // Pulso (BPM)
    @Serializable(with = DateAsLongSerializer::class)
    @ServerTimestamp val date: Date? = null,
    @Serializable(with = DateAsLongSerializer::class)
    @ServerTimestamp val time: Date? = null,
    val notes: String? = null,
)
