package com.github.libretube.api

import com.github.libretube.api.obj.DeleteUserRequest
import com.github.libretube.api.obj.EditPlaylistBody
import com.github.libretube.api.obj.Login
import com.github.libretube.api.obj.Message
import com.github.libretube.api.obj.Playlist
import com.github.libretube.api.obj.Playlists
import com.github.libretube.api.obj.StreamItem
import com.github.libretube.api.obj.Subscribe
import com.github.libretube.api.obj.Subscribed
import com.github.libretube.api.obj.Subscription
import com.github.libretube.api.obj.Token
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface PipedAuthApi {

    @POST("login")
    suspend fun login(@Body login: Login): Token

    @POST("register")
    suspend fun register(@Body login: Login): Token

    @POST("user/delete")
    suspend fun deleteAccount(
        @Header("Authorization") token: String,
        @Body password: DeleteUserRequest
    )

    @GET("feed")
    suspend fun getFeed(@Query("authToken") token: String?): List<StreamItem>

    @GET("feed/unauthenticated")
    suspend fun getUnauthenticatedFeed(@Query("channels") channels: String): List<StreamItem>

    @POST("feed/unauthenticated")
    suspend fun getUnauthenticatedFeed(@Body channels: List<String>): List<StreamItem>

    @GET("subscribed")
    suspend fun isSubscribed(
        @Query("channelId") channelId: String,
        @Header("Authorization") token: String
    ): Subscribed

    @GET("subscriptions")
    suspend fun subscriptions(@Header("Authorization") token: String): List<Subscription>

    @GET("subscriptions/unauthenticated")
    suspend fun unauthenticatedSubscriptions(
        @Query("channels") channels: String
    ): List<Subscription>

    @POST("subscriptions/unauthenticated")
    suspend fun unauthenticatedSubscriptions(@Body channels: List<String>): List<Subscription>

    @POST("subscribe")
    suspend fun subscribe(
        @Header("Authorization") token: String,
        @Body subscribe: Subscribe
    ): Message

    @POST("unsubscribe")
    suspend fun unsubscribe(
        @Header("Authorization") token: String,
        @Body subscribe: Subscribe
    ): Message

    @POST("import")
    suspend fun importSubscriptions(
        @Query("override") override: Boolean,
        @Header("Authorization") token: String,
        @Body channels: List<String>
    ): Message

    @POST("import/playlist")
    suspend fun clonePlaylist(
        @Header("Authorization") token: String,
        @Body editPlaylistBody: EditPlaylistBody
    ): EditPlaylistBody

    @GET("user/playlists")
    suspend fun getUserPlaylists(@Header("Authorization") token: String): List<Playlists>

    @POST("user/playlists/rename")
    suspend fun renamePlaylist(
        @Header("Authorization") token: String,
        @Body editPlaylistBody: EditPlaylistBody
    ): Message

    @PATCH("user/playlists/description")
    suspend fun changePlaylistDescription(
        @Header("Authorization") token: String,
        @Body editPlaylistBody: EditPlaylistBody
    ): Message

    @POST("user/playlists/delete")
    suspend fun deletePlaylist(
        @Header("Authorization") token: String,
        @Body editPlaylistBody: EditPlaylistBody
    ): Message

    @POST("user/playlists/create")
    suspend fun createPlaylist(
        @Header("Authorization") token: String,
        @Body name: Playlists
    ): EditPlaylistBody

    @POST("user/playlists/add")
    suspend fun addToPlaylist(
        @Header("Authorization") token: String,
        @Body editPlaylistBody: EditPlaylistBody
    ): Message

    @POST("user/playlists/remove")
    suspend fun removeFromPlaylist(
        @Header("Authorization") token: String,
        @Body editPlaylistBody: EditPlaylistBody
    ): Message

    @GET("playlists/{playlistId}")
    suspend fun getPlaylist(@Path("playlistId") playlistId: String): Playlist
}