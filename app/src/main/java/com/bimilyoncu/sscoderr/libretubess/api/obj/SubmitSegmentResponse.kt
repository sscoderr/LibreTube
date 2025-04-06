package com.bimilyoncu.sscoderr.libretubess.api.obj

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SubmitSegmentResponse(
    @SerialName("UUID") val uuid: String,
    val category: String,
    val segment: List<Float>
)
