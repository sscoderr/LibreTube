package com.bimilyoncu.sscoderr.libretubess.api.obj

import kotlinx.serialization.Serializable

@Serializable
data class Subscription(
    val url: String,
    val name: String,
    val avatar: String? = null,
    val verified: Boolean
)
