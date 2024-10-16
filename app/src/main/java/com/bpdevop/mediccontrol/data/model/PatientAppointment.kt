package com.bpdevop.mediccontrol.data.model

import com.bpdevop.mediccontrol.core.utils.DateAsLongSerializer
import com.google.firebase.firestore.ServerTimestamp
import kotlinx.serialization.Serializable
import java.util.Date

@Serializable
data class PatientAppointment(
    val id: String = "",
    @Serializable(with = DateAsLongSerializer::class)
    @ServerTimestamp val date: Date? = null,
    @Serializable(with = DateAsLongSerializer::class)
    @ServerTimestamp val time: Date? = null,
    val visitType: VisitType = VisitType.NEW,
    val notes: String? = null,
)
