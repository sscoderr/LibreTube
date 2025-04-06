package com.bimilyoncu.sscoderr.libretubess.obj

import kotlinx.serialization.Serializable

@Serializable
data class PipedImportPlaylist(
    var name: String? = null,
    val type: String? = null,
    val visibility: String? = null,
    var videos: List<String> = listOf()
)
