package com.bimilyoncu.sscoderr.libretubess.obj

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FreeTubeImportPlaylist(
    @SerialName("playlistName") val name: String = "",
    var videos: List<FreeTubeVideo> = listOf(),
    var protected: Boolean = true
)
