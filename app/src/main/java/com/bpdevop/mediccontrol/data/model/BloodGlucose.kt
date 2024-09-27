package com.bpdevop.mediccontrol.data.model

import com.bpdevop.mediccontrol.core.utils.DateAsLongSerializer
import com.google.firebase.firestore.ServerTimestamp
import kotlinx.serialization.Serializable
import java.util.Date

@Serializable
data class BloodGlucose(
    val id: String = "",
    @Serializable(with = DateAsLongSerializer::class)
    @ServerTimestamp val date: Date? = null,
    @Serializable(with = DateAsLongSerializer::class)
    @ServerTimestamp val time: Date? = null,
    val type: BloodGlucoseType = BloodGlucoseType.RANDOM,
    val result: Float = 0f,
    val unit: String = "mg/dL",
    val notes: String? = null,
)

enum class BloodGlucoseType {
    FASTING,  // Ayuno
    POSTPRANDIAL,  // Postprandial (después de comer)
    RANDOM  // Aleatorio
}