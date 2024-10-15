package com.bpdevop.mediccontrol.data.model

import com.bpdevop.mediccontrol.core.utils.DateAsLongSerializer
import com.google.firebase.firestore.ServerTimestamp
import kotlinx.serialization.Serializable
import java.util.Date

@Serializable
data class Laboratory(
    val id: String = "",
    val tests: List<LabTestItem> = emptyList(),
    val notes: String? = null,
    @Serializable(with = DateAsLongSerializer::class)
    @ServerTimestamp val date: Date? = null,
    val files: List<String> = emptyList(),
)

@Serializable
data class LabTestItem(
    var name: String? = null, // Nombre de la prueba de laboratorio
    var result: String? = null, // Resultado de la prueba, ejemplo: "Positivo"
    var isNormal: Boolean = true, // Indica si el resultado es normal o no
)