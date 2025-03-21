package com.bimilyoncu.sscoderr.libretube.ui.models

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bimilyoncu.sscoderr.libretube.api.MediaServiceRepository
import com.bimilyoncu.sscoderr.libretube.api.PlaylistsHelper
import com.bimilyoncu.sscoderr.libretube.api.SubscriptionHelper
import com.bimilyoncu.sscoderr.libretube.api.obj.Playlists
import com.bimilyoncu.sscoderr.libretube.api.obj.StreamItem
import com.bimilyoncu.sscoderr.libretube.constants.PreferenceKeys.HIDE_WATCHED_FROM_FEED
import com.bimilyoncu.sscoderr.libretube.db.DatabaseHelper
import com.bimilyoncu.sscoderr.libretube.db.DatabaseHolder
import com.bimilyoncu.sscoderr.libretube.db.obj.PlaylistBookmark
import com.bimilyoncu.sscoderr.libretube.enums.ContentFilter
import com.bimilyoncu.sscoderr.libretube.extensions.runSafely
import com.bimilyoncu.sscoderr.libretube.extensions.updateIfChanged
import com.bimilyoncu.sscoderr.libretube.helpers.LocaleHelper
import com.bimilyoncu.sscoderr.libretube.helpers.PlayerHelper
import com.bimilyoncu.sscoderr.libretube.helpers.PreferenceHelper
import com.bimilyoncu.sscoderr.libretube.util.deArrow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HomeViewModel : ViewModel() {
    private val hideWatched get() = PreferenceHelper.getBoolean(HIDE_WATCHED_FROM_FEED, false)

    val trending: MutableLiveData<List<StreamItem>> = MutableLiveData(null)
    val feed: MutableLiveData<List<StreamItem>> = MutableLiveData(null)
    val bookmarks: MutableLiveData<List<PlaylistBookmark>> = MutableLiveData(null)
    val playlists: MutableLiveData<List<Playlists>> = MutableLiveData(null)
    val continueWatching: MutableLiveData<List<StreamItem>> = MutableLiveData(null)
    val isLoading: MutableLiveData<Boolean> = MutableLiveData(true)
    val loadedSuccessfully: MutableLiveData<Boolean> = MutableLiveData(false)

    private val sections get() = listOf(trending, feed, bookmarks, playlists, continueWatching)

    private var loadHomeJob: Job? = null

    fun loadHomeFeed(
        context: Context,
        subscriptionsViewModel: SubscriptionsViewModel,
        visibleItems: Set<String>,
        onUnusualLoadTime: () -> Unit
    ) {
        isLoading.value = true

        loadHomeJob?.cancel()
        loadHomeJob = viewModelScope.launch {
            val result = async {
                awaitAll(
                    async { if (visibleItems.contains(TRENDING)) loadTrending(context) },
                    async { if (visibleItems.contains(FEATURED)) loadFeed(subscriptionsViewModel) },
                    async { if (visibleItems.contains(BOOKMARKS)) loadBookmarks() },
                    async { if (visibleItems.contains(PLAYLISTS)) loadPlaylists() },
                    async { if (visibleItems.contains(WATCHING)) loadVideosToContinueWatching() }
                )
                loadedSuccessfully.value = sections.any { !it.value.isNullOrEmpty() }
                isLoading.value = false
            }

            withContext(Dispatchers.IO) {
                delay(UNUSUAL_LOAD_TIME_MS)
                if (result.isActive) {
                    onUnusualLoadTime.invoke()
                }
            }
        }
    }
    private suspend fun loadTrending(context: Context) {
        val region = LocaleHelper.getTrendingRegion(context)

        runSafely(
            onSuccess = { videos -> trending.updateIfChanged(videos) },
            ioBlock = { 
                MediaServiceRepository.instance.getTrending(region)
                    .deArrow()
                    .filter { !it.isShort }
                    .take(10) 
            }
        )
    }

    private suspend fun loadFeed(subscriptionsViewModel: SubscriptionsViewModel) {
        runSafely(
            onSuccess = { videos -> feed.updateIfChanged(videos) },
            ioBlock = { tryLoadFeed(subscriptionsViewModel).deArrow().take(20) }
        )
    }

    private suspend fun loadBookmarks() {
        runSafely(
            onSuccess = { newBookmarks -> bookmarks.updateIfChanged(newBookmarks) },
            ioBlock = { DatabaseHolder.Database.playlistBookmarkDao().getAll() }
        )
    }

    private suspend fun loadPlaylists() {
        runSafely(
            onSuccess = { newPlaylists -> playlists.updateIfChanged(newPlaylists) },
            ioBlock = { PlaylistsHelper.getPlaylists().take(20) }
        )
    }

    private suspend fun loadVideosToContinueWatching() {
        if (!PlayerHelper.watchHistoryEnabled) return
        runSafely(
            onSuccess = { videos -> continueWatching.updateIfChanged(videos) },
            ioBlock = ::loadWatchingFromDB
        )
    }

    private suspend fun loadWatchingFromDB(): List<StreamItem> {
        val videos = DatabaseHelper.getWatchHistoryPage(1, 50)

        return DatabaseHelper
            .filterUnwatched(videos.map { it.toStreamItem() })
            .take(20)
    }

    private suspend fun tryLoadFeed(subscriptionsViewModel: SubscriptionsViewModel): List<StreamItem> {
        subscriptionsViewModel.videoFeed.value?.let { return it }

        val feed = SubscriptionHelper.getFeed(forceRefresh = false)
        subscriptionsViewModel.videoFeed.postValue(feed)

        return if (hideWatched) feed.filterWatched() else feed
    }

    private suspend fun List<StreamItem>.filterWatched(): List<StreamItem> {
        val allowShorts = ContentFilter.SHORTS.isEnabled
        val allowVideos = ContentFilter.VIDEOS.isEnabled
        val allowAll = (!allowShorts && !allowVideos)

        val filteredFeed = this.filter {
            allowAll || (allowShorts && it.isShort) || (allowVideos && !it.isShort)
        }
        return DatabaseHelper.filterUnwatched(filteredFeed)
    }

    companion object {
        private const val UNUSUAL_LOAD_TIME_MS = 10000L
        private const val FEATURED = "featured"
        private const val WATCHING = "watching"
        private const val TRENDING = "trending"
        private const val BOOKMARKS = "bookmarks"
        private const val PLAYLISTS = "playlists"
    }
}
