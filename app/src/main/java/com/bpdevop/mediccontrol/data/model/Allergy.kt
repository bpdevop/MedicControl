package com.bpdevop.mediccontrol.data.model

import androidx.annotation.Keep
import com.bpdevop.mediccontrol.core.utils.DateAsLongSerializer
import com.google.firebase.firestore.ServerTimestamp
import kotlinx.serialization.Serializable
import java.util.Date

@Keep
@Serializable
data class Allergy(
    val id: String = "",
    val description: String = "",
    @Serializable(with = DateAsLongSerializer::class)
    @ServerTimestamp val date: Date? = null,
)
