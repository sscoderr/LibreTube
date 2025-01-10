package com.github.libretube.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.github.libretube.db.obj.SubscriptionsFeedItem

@Dao
interface SubscriptionsFeedDao {
    @Query("SELECT * FROM feedItem ORDER BY uploaded DESC")
    suspend fun getAll(): List<SubscriptionsFeedItem>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(feedItems: List<SubscriptionsFeedItem>)

    @Query("DELETE FROM feedItem WHERE uploaded < :olderThan")
    suspend fun cleanUpOlderThan(olderThan: Long)

    @Query("DELETE FROM feedItem")
    suspend fun deleteAll()
}