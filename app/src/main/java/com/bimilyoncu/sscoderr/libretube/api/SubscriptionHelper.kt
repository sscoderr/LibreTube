package com.github.libretube.api

import android.content.Context
import com.github.libretube.R
import com.github.libretube.constants.PreferenceKeys
import com.github.libretube.db.obj.SubscriptionsFeedItem
import com.github.libretube.helpers.PreferenceHelper
import com.github.libretube.repo.AccountSubscriptionsRepository
import com.github.libretube.repo.FeedProgress
import com.github.libretube.repo.FeedRepository
import com.github.libretube.repo.LocalFeedRepository
import com.github.libretube.repo.LocalSubscriptionsRepository
import com.github.libretube.repo.PipedAccountFeedRepository
import com.github.libretube.repo.PipedLocalSubscriptionsRepository
import com.github.libretube.repo.PipedNoAccountFeedRepository
import com.github.libretube.repo.SubscriptionsRepository
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.runBlocking

object SubscriptionHelper {
    /**
     * The maximum number of channel IDs that can be passed via a GET request for fetching
     * the subscriptions list and the feed
     */
    const val GET_SUBSCRIPTIONS_LIMIT = 100

    private val localFeedExtraction
        get() = PreferenceHelper.getBoolean(
            PreferenceKeys.LOCAL_FEED_EXTRACTION,
            false
        )
    private val token get() = PreferenceHelper.getToken()
    private val subscriptionsRepository: SubscriptionsRepository
        get() = when {
            localFeedExtraction -> LocalSubscriptionsRepository()
            token.isNotEmpty() -> AccountSubscriptionsRepository()
            else -> PipedLocalSubscriptionsRepository()
        }
    private val feedRepository: FeedRepository
        get() = when {
            localFeedExtraction -> LocalFeedRepository()
            token.isNotEmpty() -> PipedAccountFeedRepository()
            else -> PipedNoAccountFeedRepository()
        }

    suspend fun subscribe(
        channelId: String, name: String, uploaderAvatar: String?, verified: Boolean
    ) = subscriptionsRepository.subscribe(channelId, name, uploaderAvatar, verified)

    suspend fun unsubscribe(channelId: String) = subscriptionsRepository.unsubscribe(channelId)
    suspend fun isSubscribed(channelId: String) = subscriptionsRepository.isSubscribed(channelId)
    suspend fun importSubscriptions(newChannels: List<String>) =
        subscriptionsRepository.importSubscriptions(newChannels)

    suspend fun getSubscriptions() =
        subscriptionsRepository.getSubscriptions().sortedBy { it.name.lowercase() }

    suspend fun getSubscriptionChannelIds() = subscriptionsRepository.getSubscriptionChannelIds()
    suspend fun getFeed(forceRefresh: Boolean, onProgressUpdate: (FeedProgress) -> Unit = {}) =
        feedRepository.getFeed(forceRefresh, onProgressUpdate)

    suspend fun submitFeedItemChange(feedItem: SubscriptionsFeedItem) =
        feedRepository.submitFeedItemChange(feedItem)

    fun handleUnsubscribe(
        context: Context,
        channelId: String,
        channelName: String?,
        onUnsubscribe: () -> Unit
    ) {
        if (!PreferenceHelper.getBoolean(PreferenceKeys.CONFIRM_UNSUBSCRIBE, false)) {
            runBlocking {
                unsubscribe(channelId)
                onUnsubscribe()
            }
            return
        }

        MaterialAlertDialogBuilder(context)
            .setTitle(R.string.unsubscribe)
            .setMessage(context.getString(R.string.confirm_unsubscribe, channelName))
            .setPositiveButton(R.string.unsubscribe) { _, _ ->
                runBlocking {
                    unsubscribe(channelId)
                    onUnsubscribe()
                }
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }
}
