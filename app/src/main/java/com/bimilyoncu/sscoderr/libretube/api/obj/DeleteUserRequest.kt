package com.bimilyoncu.sscoderr.libretube.api.obj

import kotlinx.serialization.Serializable

@Serializable
data class DeleteUserRequest(val password: String)
