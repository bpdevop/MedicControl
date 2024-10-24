package com.bpdevop.mediccontrol.data.model

import com.bpdevop.mediccontrol.core.utils.DateAsLongSerializer
import com.google.firebase.firestore.ServerTimestamp
import kotlinx.serialization.Serializable
import java.util.Date

@Serializable
data class Radiology(
    val id: String = "",
    val title: String = "", // Nombre del estudio
    val result: String = "", // Resultado del estudio
    @Serializable(with = DateAsLongSerializer::class)
    @ServerTimestamp val date: Date? = null, // Fecha del estudio
    val files: List<String> = emptyList(),
)