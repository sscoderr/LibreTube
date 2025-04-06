package com.bimilyoncu.sscoderr.libretubess.obj

import com.bimilyoncu.sscoderr.libretubess.ui.dialogs.ShareDialog
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FreetubeSubscription(
    val name: String,
    @SerialName("id") val channelId: String,
    val url: String = "${ShareDialog.YOUTUBE_FRONTEND_URL}/channel/$channelId"
)
