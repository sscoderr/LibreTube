package com.bimilyoncu.sscoderr.libretubess.api.obj

import kotlinx.serialization.Serializable

@Serializable
data class ChannelTabResponse(
    var content: List<ContentItem> = emptyList(),
    val nextpage: String? = null
)
