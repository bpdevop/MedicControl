package com.bpdevop.mediccontrol.data.model

import androidx.annotation.Keep
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

@Keep
data class Patient(
    val id: String = "",
    val name: String = "",
    @ServerTimestamp val birthDate: Date? = null,
    val gender: String? = null,
    val bloodType: String? = null,
    val rhFactor: Boolean? = null,
    val address: String? = null,
    val phone: String? = null,
    val email: String? = null,
    val notes: String? = null,
    val photoUrl: String? = null,
    val doctorId: String = "",
)