package com.bpdevop.mediccontrol.data.model

import com.bpdevop.mediccontrol.core.utils.DateAsLongSerializer
import com.google.firebase.firestore.ServerTimestamp
import kotlinx.serialization.Serializable
import java.util.Date

@Serializable
data class Prescription(
    val id: String = "",
    val medications: List<PrescriptionItem> = emptyList(),
    val notes: String? = null, // Notas adicionales del médico
    @Serializable(with = DateAsLongSerializer::class)
    @ServerTimestamp val date: Date? = null, // Fecha de la receta
    val files: List<String> = emptyList(),
)

@Serializable
data class PrescriptionItem(
    var name: String? = null, // Nombre del medicamento
    var dosage: String? = null, // Ejemplo: "500mg"
    var frequency: String? = null, // En horas, ejemplo: cada 6 horas
    var duration: String? = null, // En días, ejemplo: por 3 días
)

