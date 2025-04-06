package com.bimilyoncu.sscoderr.libretubess.ui.models

import android.content.Context
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bimilyoncu.sscoderr.libretubess.R
import com.bimilyoncu.sscoderr.libretubess.api.SubscriptionHelper
import com.bimilyoncu.sscoderr.libretubess.api.obj.StreamItem
import com.bimilyoncu.sscoderr.libretubess.api.obj.Subscription
import com.bimilyoncu.sscoderr.libretubess.extensions.TAG
import com.bimilyoncu.sscoderr.libretubess.extensions.toID
import com.bimilyoncu.sscoderr.libretubess.extensions.toastFromMainDispatcher
import com.bimilyoncu.sscoderr.libretubess.helpers.PreferenceHelper
import com.bimilyoncu.sscoderr.libretubess.repo.FeedProgress
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SubscriptionsViewModel : ViewModel() {
    var videoFeed = MutableLiveData<List<StreamItem>?>()

    var subscriptions = MutableLiveData<List<Subscription>?>()
    val feedProgress = MutableLiveData<FeedProgress?>()

    fun fetchFeed(context: Context, forceRefresh: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            val videoFeed = try {
                SubscriptionHelper.getFeed(forceRefresh = forceRefresh) { feedProgress ->
                    this@SubscriptionsViewModel.feedProgress.postValue(feedProgress)
                }
            } catch (e: Exception) {
                context.toastFromMainDispatcher(R.string.server_error)
                Log.e(TAG(), e.toString())
                return@launch
            }
            this@SubscriptionsViewModel.videoFeed.postValue(videoFeed)
            if (videoFeed.isNotEmpty()) {
                // save the last recent video to the prefs for the notification worker
                PreferenceHelper.setLastSeenVideoId(videoFeed[0].url!!.toID())
            }
        }
    }

    fun fetchSubscriptions(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            val subscriptions = try {
                SubscriptionHelper.getSubscriptions()
            } catch (e: Exception) {
                context.toastFromMainDispatcher(R.string.server_error)
                Log.e(TAG(), e.toString())
                return@launch
            }
            this@SubscriptionsViewModel.subscriptions.postValue(subscriptions)
        }
    }
}
