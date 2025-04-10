package com.bimilyoncu.sscoderr.libretubess.services

import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi

@OptIn(UnstableApi::class)
class VideoOnlinePlayerService : OnlinePlayerService() {
    override val isAudioOnlyPlayer: Boolean = false
}