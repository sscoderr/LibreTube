package com.bimilyoncu.sscoderr.libretube.repo

import com.bimilyoncu.sscoderr.libretube.api.obj.Subscription
import com.bimilyoncu.sscoderr.libretube.db.DatabaseHolder.Database
import com.bimilyoncu.sscoderr.libretube.db.obj.LocalSubscription
import com.bimilyoncu.sscoderr.libretube.extensions.parallelMap
import com.bimilyoncu.sscoderr.libretube.ui.dialogs.ShareDialog.Companion.YOUTUBE_FRONTEND_URL
import org.schabi.newpipe.extractor.channel.ChannelInfo

class LocalSubscriptionsRepository : SubscriptionsRepository {
    override suspend fun subscribe(
        channelId: String, name: String, uploaderAvatar: String?, verified: Boolean
    ) {
        val localSubscription = LocalSubscription(
            channelId = channelId,
            name = name,
            avatar = uploaderAvatar,
            verified = verified
        )

        Database.localSubscriptionDao().insert(localSubscription)
    }

    override suspend fun unsubscribe(channelId: String) {
        Database.localSubscriptionDao().deleteById(channelId)
    }

    override suspend fun isSubscribed(channelId: String): Boolean {
        return Database.localSubscriptionDao().includes(channelId)
    }

    override suspend fun importSubscriptions(newChannels: List<String>) {
        for (chunk in newChannels.chunked(CHANNEL_CHUNK_SIZE)) {
            chunk.parallelMap { channelId ->
                val channelUrl = "$YOUTUBE_FRONTEND_URL/channel/${channelId}"
                val channelInfo = ChannelInfo.getInfo(channelUrl)

                runCatching { subscribe(channelId, channelInfo.name, channelInfo.avatars.maxByOrNull { it.height }?.url, channelInfo.isVerified) }
            }
        }
    }

    override suspend fun getSubscriptions(): List<Subscription> {
        // load all channels that have not been fetched yet
        val unfinished = Database.localSubscriptionDao().getChannelsWithoutMetaInfo()
        runCatching {
            importSubscriptions(unfinished.map { it.channelId })
        }

        return Database.localSubscriptionDao().getAll().map {
            Subscription(
                url = it.channelId,
                name = it.name.orEmpty(),
                avatar = it.avatar,
                verified = it.verified
            )
        }
    }

    override suspend fun getSubscriptionChannelIds(): List<String> {
        return Database.localSubscriptionDao().getAll().map { it.channelId }
    }

    companion object {
        private const val CHANNEL_CHUNK_SIZE = 2
    }
}