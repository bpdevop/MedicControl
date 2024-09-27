package com.bpdevop.mediccontrol.data.model

import androidx.annotation.Keep
import java.util.Date

@Keep
data class Vaccine(
    val id: String = "",
    val name: String,
    val date: Date,
    val notes: String?,
)

