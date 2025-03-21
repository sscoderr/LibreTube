package com.bimilyoncu.sscoderr.libretube.repo

import com.bimilyoncu.sscoderr.libretube.api.obj.Playlist
import com.bimilyoncu.sscoderr.libretube.api.obj.Playlists
import com.bimilyoncu.sscoderr.libretube.api.obj.StreamItem
import com.bimilyoncu.sscoderr.libretube.obj.PipedImportPlaylist

interface PlaylistRepository {
    suspend fun getPlaylist(playlistId: String): Playlist
    suspend fun getPlaylists(): List<Playlists>
    suspend fun addToPlaylist(playlistId: String, vararg videos: StreamItem): Boolean
    suspend fun renamePlaylist(playlistId: String, newName: String): Boolean
    suspend fun changePlaylistDescription(playlistId: String, newDescription: String): Boolean
    suspend fun clonePlaylist(playlistId: String): String?
    suspend fun removeFromPlaylist(playlistId: String, index: Int): Boolean
    suspend fun importPlaylists(playlists: List<PipedImportPlaylist>)
    suspend fun createPlaylist(playlistName: String): String?
    suspend fun deletePlaylist(playlistId: String): Boolean
}