package com.bimilyoncu.sscoderr.libretubess.repo

import com.bimilyoncu.sscoderr.libretubess.api.obj.StreamItem
import com.bimilyoncu.sscoderr.libretubess.db.obj.SubscriptionsFeedItem

data class FeedProgress(
    val currentProgress: Int,
    val total: Int
)

interface FeedRepository {
    suspend fun getFeed(
        forceRefresh: Boolean,
        onProgressUpdate: (FeedProgress) -> Unit
    ): List<StreamItem>
    suspend fun submitFeedItemChange(feedItem: SubscriptionsFeedItem) {}
}