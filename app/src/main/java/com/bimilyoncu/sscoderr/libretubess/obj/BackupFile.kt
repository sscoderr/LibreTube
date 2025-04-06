package com.bimilyoncu.sscoderr.libretubess.obj

import com.bimilyoncu.sscoderr.libretubess.db.obj.CustomInstance
import com.bimilyoncu.sscoderr.libretubess.db.obj.LocalPlaylistWithVideos
import com.bimilyoncu.sscoderr.libretubess.db.obj.LocalSubscription
import com.bimilyoncu.sscoderr.libretubess.db.obj.PlaylistBookmark
import com.bimilyoncu.sscoderr.libretubess.db.obj.SearchHistoryItem
import com.bimilyoncu.sscoderr.libretubess.db.obj.SubscriptionGroup
import com.bimilyoncu.sscoderr.libretubess.db.obj.WatchHistoryItem
import com.bimilyoncu.sscoderr.libretubess.db.obj.WatchPosition
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames

@Serializable
@OptIn(ExperimentalSerializationApi::class)
data class BackupFile(
    //
    // some stuff for compatibility with Piped imports
    //
    val format: String = "Piped",
    val version: Int = 1,

    //
    // only compatible with LibreTube itself, database objects
    //
    var watchHistory: List<WatchHistoryItem>? = emptyList(),
    var watchPositions: List<WatchPosition>? = emptyList(),
    var searchHistory: List<SearchHistoryItem>? = emptyList(),
    var customInstances: List<CustomInstance>? = emptyList(),
    var playlistBookmarks: List<PlaylistBookmark>? = emptyList(),

    //
    // Preferences, stored as a key value map
    //
    var preferences: List<PreferenceItem>? = emptyList(),

    //
    // Database objects with compatibility for Piped imports/exports
    //
    @JsonNames("groups", "channelGroups")
    var groups: List<SubscriptionGroup>? = emptyList(),

    @JsonNames("subscriptions", "localSubscriptions")
    var subscriptions: List<LocalSubscription>? = emptyList(),

    // playlists are exported in two different formats because the formats differ too much unfortunately
    var localPlaylists: List<LocalPlaylistWithVideos>? = emptyList(),
    var playlists: List<PipedImportPlaylist>? = emptyList()
)
