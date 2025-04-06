package com.bimilyoncu.sscoderr.libretubess.api

import com.bimilyoncu.sscoderr.libretubess.api.RetrofitInstance.PIPED_API_URL
import com.bimilyoncu.sscoderr.libretubess.api.obj.Channel
import com.bimilyoncu.sscoderr.libretubess.api.obj.ChannelTabResponse
import com.bimilyoncu.sscoderr.libretubess.api.obj.CommentsPage
import com.bimilyoncu.sscoderr.libretubess.api.obj.DeArrowContent
import com.bimilyoncu.sscoderr.libretubess.api.obj.Message
import com.bimilyoncu.sscoderr.libretubess.api.obj.Playlist
import com.bimilyoncu.sscoderr.libretubess.api.obj.SearchResult
import com.bimilyoncu.sscoderr.libretubess.api.obj.SegmentData
import com.bimilyoncu.sscoderr.libretubess.api.obj.StreamItem
import com.bimilyoncu.sscoderr.libretubess.api.obj.Streams
import com.bimilyoncu.sscoderr.libretubess.constants.PreferenceKeys
import com.bimilyoncu.sscoderr.libretubess.helpers.PreferenceHelper
import kotlinx.serialization.encodeToString
import retrofit2.HttpException

open class PipedMediaServiceRepository : MediaServiceRepository {
    override suspend fun getTrending(region: String): List<StreamItem> =
        api.getTrending(region)

    override suspend fun getStreams(videoId: String): Streams {
        return try {
            api.getStreams(videoId)
        } catch (e: HttpException) {
            val errorMessage = e.response()?.errorBody()?.string()?.runCatching {
                JsonHelper.json.decodeFromString<Message>(this).message
            }?.getOrNull()

            throw Exception(errorMessage)
        }
    }

    override suspend fun getComments(videoId: String): CommentsPage =
        api.getComments(videoId)

    override suspend fun getSegments(
        videoId: String,
        category: List<String>,
        actionType: List<String>?
    ): SegmentData = api.getSegments(
        videoId,
        JsonHelper.json.encodeToString(category),
        JsonHelper.json.encodeToString(actionType)
    )

    override suspend fun getDeArrowContent(videoIds: String): Map<String, DeArrowContent> =
        api.getDeArrowContent(videoIds)

    override suspend fun getCommentsNextPage(videoId: String, nextPage: String): CommentsPage =
        api.getCommentsNextPage(videoId, nextPage)

    override suspend fun getSearchResults(searchQuery: String, filter: String): SearchResult =
        api.getSearchResults(searchQuery, filter)

    override suspend fun getSearchResultsNextPage(
        searchQuery: String,
        filter: String,
        nextPage: String
    ): SearchResult = api.getSearchResultsNextPage(searchQuery, filter, nextPage)

    override suspend fun getSuggestions(query: String): List<String> =
        api.getSuggestions(query)

    override suspend fun getChannel(channelId: String): Channel =
        api.getChannel(channelId)

    override suspend fun getChannelTab(data: String, nextPage: String?): ChannelTabResponse =
        api.getChannelTab(data, nextPage)

    override suspend fun getChannelByName(channelName: String): Channel =
        api.getChannelByName(channelName)

    override suspend fun getChannelNextPage(channelId: String, nextPage: String): Channel =
        api.getChannelNextPage(channelId, nextPage)

    override suspend fun getPlaylist(playlistId: String): Playlist =
        api.getPlaylist(playlistId)

    override suspend fun getPlaylistNextPage(playlistId: String, nextPage: String): Playlist =
        api.getPlaylistNextPage(playlistId, nextPage)

    companion object {
        val apiUrl get() = PreferenceHelper.getString(PreferenceKeys.FETCH_INSTANCE, PIPED_API_URL)

        private val api by resettableLazy(RetrofitInstance.apiLazyMgr) {
            RetrofitInstance.buildRetrofitInstance<PipedApi>(apiUrl)
        }
    }
}