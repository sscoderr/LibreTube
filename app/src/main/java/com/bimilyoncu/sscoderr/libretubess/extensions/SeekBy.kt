package com.bimilyoncu.sscoderr.libretubess.extensions

import androidx.media3.common.Player

/**
 * Forward or rewind by the provided [timeDiff] in milliseconds
 */
fun Player.seekBy(timeDiff: Long) {
    seekTo(currentPosition + timeDiff)
}
