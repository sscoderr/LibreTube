package com.bimilyoncu.sscoderr.libretubess.api.obj

import kotlinx.serialization.Serializable

@Serializable
data class Login(
    val username: String,
    val password: String
)
