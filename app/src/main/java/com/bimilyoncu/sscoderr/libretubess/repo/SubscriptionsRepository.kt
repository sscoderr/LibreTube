package com.bimilyoncu.sscoderr.libretubess.repo

import com.bimilyoncu.sscoderr.libretubess.api.obj.Subscription

interface SubscriptionsRepository {
    suspend fun subscribe(channelId: String, name: String, uploaderAvatar: String?, verified: Boolean)
    suspend fun unsubscribe(channelId: String)
    suspend fun isSubscribed(channelId: String): Boolean?
    suspend fun importSubscriptions(newChannels: List<String>)
    suspend fun getSubscriptions(): List<Subscription>
    suspend fun getSubscriptionChannelIds(): List<String>
}