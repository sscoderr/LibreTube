package com.bimilyoncu.sscoderr.libretubess.ui.extensions

import android.widget.TextView
import androidx.core.view.isGone
import androidx.core.view.isVisible
import com.bimilyoncu.sscoderr.libretubess.R
import com.bimilyoncu.sscoderr.libretubess.api.SubscriptionHelper
import com.bimilyoncu.sscoderr.libretubess.constants.PreferenceKeys
import com.bimilyoncu.sscoderr.libretubess.helpers.PreferenceHelper
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

fun TextView.setupSubscriptionButton(
    channelId: String?,
    channelName: String,
    channelAvatar: String?,
    channelVerified: Boolean,
    notificationBell: MaterialButton? = null,
    isSubscribed: Boolean? = null,
    onIsSubscribedChange: (Boolean) -> Unit = {}
) {
    if (channelId == null) return

    var subscribed: Boolean? = false

    CoroutineScope(Dispatchers.IO).launch {
        subscribed = isSubscribed ?: SubscriptionHelper.isSubscribed(channelId)

        withContext(Dispatchers.Main) {
            subscribed?.let { subscribed -> onIsSubscribedChange(subscribed) }

            if (subscribed == true) {
                this@setupSubscriptionButton.text = context.getString(R.string.unsubscribe)
            } else {
                notificationBell?.isGone = true
            }
            this@setupSubscriptionButton.isVisible = true
        }
    }

    notificationBell?.setupNotificationBell(channelId)

    setOnClickListener {
        if (subscribed == true) {
            SubscriptionHelper.handleUnsubscribe(context, channelId, channelName) {
                text = context.getString(R.string.subscribe)
                notificationBell?.isGone = true

                subscribed = false
                onIsSubscribedChange(false)
            }
        } else {
            CoroutineScope(Dispatchers.Main).launch {
                withContext(Dispatchers.IO) {
                    SubscriptionHelper.subscribe(channelId, channelName, channelAvatar, channelVerified)
                }

                text = context.getString(R.string.unsubscribe)
                notificationBell?.isVisible = PreferenceHelper
                    .getBoolean(PreferenceKeys.NOTIFICATION_ENABLED, true)

                subscribed = true
                onIsSubscribedChange(true)
            }
        }
    }
}
