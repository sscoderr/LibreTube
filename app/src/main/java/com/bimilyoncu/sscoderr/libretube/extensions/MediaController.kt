package com.bimilyoncu.sscoderr.libretube.extensions

import androidx.annotation.OptIn
import androidx.core.os.bundleOf
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaController
import com.bimilyoncu.sscoderr.libretube.enums.PlayerCommand
import com.bimilyoncu.sscoderr.libretube.services.AbstractPlayerService

@OptIn(UnstableApi::class)
fun MediaController.navigateVideo(videoId: String) {
    sendCustomCommand(
        AbstractPlayerService.runPlayerActionCommand,
        bundleOf(PlayerCommand.PLAY_VIDEO_BY_ID.name to videoId)
    )
}