package com.bimilyoncu.sscoderr.libretubess.api.obj

import android.os.Parcelable
import com.bimilyoncu.sscoderr.libretubess.db.obj.DownloadItem
import com.bimilyoncu.sscoderr.libretubess.enums.FileType
import com.bimilyoncu.sscoderr.libretubess.helpers.ProxyHelper
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable
import kotlin.io.path.Path

@Serializable
@Parcelize
data class PipedStream(
    var url: String? = null,
    val format: String? = null,
    val quality: String? = null,
    val mimeType: String? = null,
    val codec: String? = null,
    val videoOnly: Boolean? = null,
    val bitrate: Int? = null,
    val initStart: Int? = null,
    val initEnd: Int? = null,
    val indexStart: Int? = null,
    val indexEnd: Int? = null,
    val width: Int? = null,
    val height: Int? = null,
    val fps: Int? = null,
    val audioTrackName: String? = null,
    val audioTrackId: String? = null,
    val contentLength: Long = -1,
    val audioTrackType: String? = null,
    val audioTrackLocale: String? = null
): Parcelable {
    private fun getQualityString(fileName: String): String {
        return "${fileName}_${quality?.replace(" ", "_")}_$format." +
            mimeType?.split("/")?.last()
    }

    fun toDownloadItem(fileType: FileType, videoId: String, fileName: String) = DownloadItem(
        type = fileType,
        videoId = videoId,
        fileName = getQualityString(fileName),
        path = Path(""),
        url = url?.let { ProxyHelper.unwrapUrl(it) },
        format = format,
        quality = quality,
        language = audioTrackLocale,
        downloadSize = contentLength
    )
}
