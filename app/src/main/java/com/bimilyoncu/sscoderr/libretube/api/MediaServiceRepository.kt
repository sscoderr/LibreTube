package com.bimilyoncu.sscoderr.libretube.api

import com.bimilyoncu.sscoderr.libretube.api.obj.Channel
import com.bimilyoncu.sscoderr.libretube.api.obj.ChannelTabResponse
import com.bimilyoncu.sscoderr.libretube.api.obj.CommentsPage
import com.bimilyoncu.sscoderr.libretube.api.obj.DeArrowContent
import com.bimilyoncu.sscoderr.libretube.api.obj.Playlist
import com.bimilyoncu.sscoderr.libretube.api.obj.SearchResult
import com.bimilyoncu.sscoderr.libretube.api.obj.SegmentData
import com.bimilyoncu.sscoderr.libretube.api.obj.StreamItem
import com.bimilyoncu.sscoderr.libretube.api.obj.Streams
import com.bimilyoncu.sscoderr.libretube.helpers.PlayerHelper

interface MediaServiceRepository {
    suspend fun getTrending(region: String): List<StreamItem>
    suspend fun getStreams(videoId: String): Streams
    suspend fun getComments(videoId: String): CommentsPage
    suspend fun getSegments(
        videoId: String,
        category: List<String>,
        actionType: List<String>? = null
    ): SegmentData

    suspend fun getDeArrowContent(videoIds: String): Map<String, DeArrowContent>
    suspend fun getCommentsNextPage(videoId: String, nextPage: String): CommentsPage
    suspend fun getSearchResults(searchQuery: String, filter: String): SearchResult
    suspend fun getSearchResultsNextPage(
        searchQuery: String,
        filter: String,
        nextPage: String
    ): SearchResult

    suspend fun getSuggestions(query: String): List<String>
    suspend fun getChannel(channelId: String): Channel
    suspend fun getChannelTab(data: String, nextPage: String? = null): ChannelTabResponse
    suspend fun getChannelByName(channelName: String): Channel
    suspend fun getChannelNextPage(channelId: String, nextPage: String): Channel
    suspend fun getPlaylist(playlistId: String): Playlist
    suspend fun getPlaylistNextPage(playlistId: String, nextPage: String): Playlist

    companion object {
        val instance: MediaServiceRepository
            get() = when {
                PlayerHelper.fullLocalMode -> NewPipeMediaServiceRepository()
                PlayerHelper.localStreamExtraction -> LocalStreamsExtractionPipedMediaServiceRepository()
                else -> PipedMediaServiceRepository()
            }
    }
}