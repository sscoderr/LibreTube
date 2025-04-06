package com.bimilyoncu.sscoderr.libretubess.util

import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import com.bimilyoncu.sscoderr.libretubess.extensions.toAndroidUri
import com.bimilyoncu.sscoderr.libretubess.ui.interfaces.TimeFrameReceiver
import java.nio.file.Path

class OfflineTimeFrameReceiver(
    private val context: Context,
    private val videoSource: Path
) : TimeFrameReceiver() {
    private val metadataRetriever = MediaMetadataRetriever().apply {
        setDataSource(context, videoSource.toAndroidUri())
    }
    override suspend fun getFrameAtTime(position: Long): Bitmap? {
        return metadataRetriever.getFrameAtTime(position * 1000)
    }
}
