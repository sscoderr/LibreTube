package com.bimilyoncu.sscoderr.libretubess.api.obj

import kotlinx.serialization.Serializable

@Serializable
data class DeleteUserRequest(val password: String)
