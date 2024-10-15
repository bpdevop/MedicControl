package com.bpdevop.mediccontrol.data.api.responses

import com.google.gson.annotations.SerializedName

data class IcdDisease(
    @SerializedName("id") val id: String,
    @SerializedName("title") val title: String,
    @SerializedName("theCode") val code: String?,
    @SerializedName("chapter") val chapter: String?,
    @SerializedName("isLeaf") val isLeaf: Boolean,
    @SerializedName("stemId") val stemId: String?,
    @SerializedName("descendants") val descendants: List<String>?,
    @SerializedName("matchingPVs") val matchingPVs: List<IcdSynonym>
)
