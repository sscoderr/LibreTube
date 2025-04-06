package com.bimilyoncu.sscoderr.libretubess.repo

import com.bimilyoncu.sscoderr.libretubess.api.RetrofitInstance
import com.bimilyoncu.sscoderr.libretubess.api.obj.StreamItem
import com.bimilyoncu.sscoderr.libretubess.helpers.PreferenceHelper

class PipedAccountFeedRepository : FeedRepository {
    override suspend fun getFeed(
        forceRefresh: Boolean,
        onProgressUpdate: (FeedProgress) -> Unit
    ): List<StreamItem> {
        val token = PreferenceHelper.getToken()

        return RetrofitInstance.authApi.getFeed(token)
    }
}