package com.bimilyoncu.sscoderr.libretube.db

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.bimilyoncu.sscoderr.libretube.db.dao.CustomInstanceDao
import com.bimilyoncu.sscoderr.libretube.db.dao.DownloadDao
import com.bimilyoncu.sscoderr.libretube.db.dao.LocalPlaylistsDao
import com.bimilyoncu.sscoderr.libretube.db.dao.LocalSubscriptionDao
import com.bimilyoncu.sscoderr.libretube.db.dao.PlaylistBookmarkDao
import com.bimilyoncu.sscoderr.libretube.db.dao.SearchHistoryDao
import com.bimilyoncu.sscoderr.libretube.db.dao.SubscriptionGroupsDao
import com.bimilyoncu.sscoderr.libretube.db.dao.SubscriptionsFeedDao
import com.bimilyoncu.sscoderr.libretube.db.dao.WatchHistoryDao
import com.bimilyoncu.sscoderr.libretube.db.dao.WatchPositionDao
import com.bimilyoncu.sscoderr.libretube.db.obj.CustomInstance
import com.bimilyoncu.sscoderr.libretube.db.obj.Download
import com.bimilyoncu.sscoderr.libretube.db.obj.DownloadChapter
import com.bimilyoncu.sscoderr.libretube.db.obj.DownloadItem
import com.bimilyoncu.sscoderr.libretube.db.obj.LocalPlaylist
import com.bimilyoncu.sscoderr.libretube.db.obj.LocalPlaylistItem
import com.bimilyoncu.sscoderr.libretube.db.obj.LocalSubscription
import com.bimilyoncu.sscoderr.libretube.db.obj.PlaylistBookmark
import com.bimilyoncu.sscoderr.libretube.db.obj.SearchHistoryItem
import com.bimilyoncu.sscoderr.libretube.db.obj.SubscriptionGroup
import com.bimilyoncu.sscoderr.libretube.db.obj.SubscriptionsFeedItem
import com.bimilyoncu.sscoderr.libretube.db.obj.WatchHistoryItem
import com.bimilyoncu.sscoderr.libretube.db.obj.WatchPosition

@Database(
    entities = [
        WatchHistoryItem::class,
        WatchPosition::class,
        SearchHistoryItem::class,
        CustomInstance::class,
        LocalSubscription::class,
        PlaylistBookmark::class,
        LocalPlaylist::class,
        LocalPlaylistItem::class,
        Download::class,
        DownloadItem::class,
        DownloadChapter::class,
        SubscriptionGroup::class,
        SubscriptionsFeedItem::class
    ],
    version = 20,
    exportSchema = true,
    autoMigrations = [
        AutoMigration(from = 7, to = 8),
        AutoMigration(from = 8, to = 9),
        AutoMigration(from = 9, to = 10),
        AutoMigration(from = 10, to = 11),
        AutoMigration(from = 16, to = 17),
        AutoMigration(from = 18, to = 19),
        AutoMigration(from = 19, to = 20)
    ]
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    /**
     * Watch History
     */
    abstract fun watchHistoryDao(): WatchHistoryDao

    /**
     * Watch Positions
     */
    abstract fun watchPositionDao(): WatchPositionDao

    /**
     * Search History
     */
    abstract fun searchHistoryDao(): SearchHistoryDao

    /**
     * Custom Instances
     */
    abstract fun customInstanceDao(): CustomInstanceDao

    /**
     * Local Subscriptions
     */
    abstract fun localSubscriptionDao(): LocalSubscriptionDao

    /**
     * Bookmarked Playlists
     */
    abstract fun playlistBookmarkDao(): PlaylistBookmarkDao

    /**
     * Local playlists
     */
    abstract fun localPlaylistsDao(): LocalPlaylistsDao

    /**
     * Downloads
     */
    abstract fun downloadDao(): DownloadDao

    /**
     * Subscription groups
     */
    abstract fun subscriptionGroupsDao(): SubscriptionGroupsDao

    /**
     * Locally cached subscription feed
     */
    abstract fun feedDao(): SubscriptionsFeedDao
}
