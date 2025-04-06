package com.bimilyoncu.sscoderr.libretubess.api.obj

import kotlinx.serialization.Serializable

@Serializable
data class SearchResult(
    var items: List<ContentItem> = emptyList(),
    val nextpage: String? = null,
)
