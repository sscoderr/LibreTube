package com.bimilyoncu.sscoderr.libretubess.obj

import kotlinx.serialization.Serializable

@Serializable
data class PipedPlaylistFile(
    val format: String = "Piped",
    val version: Int = 1,
    val playlists: List<PipedImportPlaylist> = emptyList()
)
