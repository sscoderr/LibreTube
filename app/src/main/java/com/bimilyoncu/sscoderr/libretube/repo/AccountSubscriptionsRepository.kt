package com.bimilyoncu.sscoderr.libretube.repo

import com.bimilyoncu.sscoderr.libretube.api.RetrofitInstance
import com.bimilyoncu.sscoderr.libretube.api.obj.Subscribe
import com.bimilyoncu.sscoderr.libretube.api.obj.Subscription
import com.bimilyoncu.sscoderr.libretube.extensions.toID
import com.bimilyoncu.sscoderr.libretube.helpers.PreferenceHelper

class AccountSubscriptionsRepository : SubscriptionsRepository {
    private val token get() = PreferenceHelper.getToken()

    override suspend fun subscribe(
        channelId: String, name: String, uploaderAvatar: String?, verified: Boolean
    ) {
        runCatching {
            RetrofitInstance.authApi.subscribe(token, Subscribe(channelId))
        }
    }

    override suspend fun unsubscribe(channelId: String) {
        runCatching {
            RetrofitInstance.authApi.unsubscribe(token, Subscribe(channelId))
        }
    }

    override suspend fun isSubscribed(channelId: String): Boolean? {
        return runCatching {
            RetrofitInstance.authApi.isSubscribed(channelId, token)
        }.getOrNull()?.subscribed
    }

    override suspend fun importSubscriptions(newChannels: List<String>) {
        RetrofitInstance.authApi.importSubscriptions(false, token, newChannels)
    }

    override suspend fun getSubscriptions(): List<Subscription> {
        return RetrofitInstance.authApi.subscriptions(token)
    }

    override suspend fun getSubscriptionChannelIds(): List<String> {
        return getSubscriptions().map { it.url.toID() }
    }
}