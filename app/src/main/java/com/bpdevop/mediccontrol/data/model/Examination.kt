package com.bpdevop.mediccontrol.data.model

import com.bpdevop.mediccontrol.core.utils.DateAsLongSerializer
import com.google.firebase.firestore.ServerTimestamp
import kotlinx.serialization.Serializable
import java.util.Date

@Serializable
data class Examination(
    val id: String = "",
    val temperature: Float? = null,
    val temperatureUnit: String? = null, // °C or °F
    val weight: Float? = null,
    val weightUnit: String? = null, // kg or lb
    val height: Float? = null,
    val heightUnit: String? = null, // cm, inch, feet
    val symptoms: List<String> = emptyList(),
    val diagnosis: List<String> = emptyList(),
    val notes: String? = null,
    @Serializable(with = DateAsLongSerializer::class)
    @ServerTimestamp val date: Date? = null,
    val files: List<String> = emptyList() // Paths to uploaded files, documents, images, etc.
)
