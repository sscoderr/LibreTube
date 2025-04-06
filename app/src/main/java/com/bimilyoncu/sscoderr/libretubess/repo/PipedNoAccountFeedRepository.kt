package com.bimilyoncu.sscoderr.libretubess.repo

import com.bimilyoncu.sscoderr.libretubess.api.RetrofitInstance
import com.bimilyoncu.sscoderr.libretubess.api.SubscriptionHelper
import com.bimilyoncu.sscoderr.libretubess.api.SubscriptionHelper.GET_SUBSCRIPTIONS_LIMIT
import com.bimilyoncu.sscoderr.libretubess.api.obj.StreamItem

class PipedNoAccountFeedRepository : FeedRepository {
    override suspend fun getFeed(
        forceRefresh: Boolean,
        onProgressUpdate: (FeedProgress) -> Unit
    ): List<StreamItem> {
        val channelIds = SubscriptionHelper.getSubscriptionChannelIds()

        return when {
            channelIds.size > GET_SUBSCRIPTIONS_LIMIT ->
                RetrofitInstance.authApi
                    .getUnauthenticatedFeed(channelIds)

            else -> RetrofitInstance.authApi.getUnauthenticatedFeed(
                channelIds.joinToString(",")
            )
        }
    }
}