package com.bimilyoncu.sscoderr.libretubess.obj

import android.graphics.Bitmap
import com.bimilyoncu.sscoderr.libretubess.api.obj.Streams

data class DownloadedFile(
    val name: String,
    val size: Long,
    var metadata: Streams? = null,
    var thumbnail: Bitmap? = null
)
