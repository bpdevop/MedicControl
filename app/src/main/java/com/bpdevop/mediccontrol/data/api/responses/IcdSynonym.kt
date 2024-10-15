package com.bpdevop.mediccontrol.data.api.responses

import com.google.gson.annotations.SerializedName

data class IcdSynonym(
    @SerializedName("label") val label: String
)