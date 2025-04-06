package com.bimilyoncu.sscoderr.libretubess.api.obj

import kotlinx.serialization.Serializable

@Serializable
data class Message(
    val error: String? = null,
    val message: String? = null
)
