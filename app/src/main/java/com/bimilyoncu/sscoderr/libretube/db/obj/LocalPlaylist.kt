package com.bimilyoncu.sscoderr.libretube.db.obj

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity
data class LocalPlaylist(
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,
    var name: String = "",
    var thumbnailUrl: String = "",
    var description: String? = ""
)
