package com.bpdevop.mediccontrol.data.model

import androidx.annotation.Keep
import com.bpdevop.mediccontrol.core.utils.DateAsLongSerializer
import com.google.firebase.firestore.ServerTimestamp
import kotlinx.serialization.Serializable
import java.util.Date

@Keep
@Serializable
data class Vaccine(
    val id: String = "",
    val name: String = "",
    @Serializable(with = DateAsLongSerializer::class)
    @ServerTimestamp val date: Date? = null,
    val notes: String? = null,
)