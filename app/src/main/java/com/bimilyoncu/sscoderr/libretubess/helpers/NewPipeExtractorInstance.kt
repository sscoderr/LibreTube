package com.bimilyoncu.sscoderr.libretubess.helpers

import com.bimilyoncu.sscoderr.libretubess.util.NewPipeDownloaderImpl
import org.schabi.newpipe.extractor.NewPipe
import org.schabi.newpipe.extractor.ServiceList
import org.schabi.newpipe.extractor.StreamingService

object NewPipeExtractorInstance {
    val extractor: StreamingService by lazy {
        NewPipe.getService(ServiceList.YouTube.serviceId)
    }

    fun init() {
        NewPipe.init(NewPipeDownloaderImpl())
    }
}