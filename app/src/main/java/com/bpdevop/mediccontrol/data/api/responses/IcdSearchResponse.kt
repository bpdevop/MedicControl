package com.bpdevop.mediccontrol.data.api.responses

import com.google.gson.annotations.SerializedName

data class IcdSearchResponse(
    @SerializedName("destinationEntities") val destinationEntities: List<IcdDisease>
)