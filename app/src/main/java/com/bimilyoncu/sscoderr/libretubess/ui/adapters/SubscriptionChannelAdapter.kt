package com.bimilyoncu.sscoderr.libretubess.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.recyclerview.widget.ListAdapter
import com.bimilyoncu.sscoderr.libretubess.api.obj.Subscription
import com.bimilyoncu.sscoderr.libretubess.constants.IntentData
import com.bimilyoncu.sscoderr.libretubess.databinding.ChannelSubscriptionRowBinding
import com.bimilyoncu.sscoderr.libretubess.extensions.toID
import com.bimilyoncu.sscoderr.libretubess.helpers.ImageHelper
import com.bimilyoncu.sscoderr.libretubess.helpers.NavigationHelper
import com.bimilyoncu.sscoderr.libretubess.ui.adapters.callbacks.DiffUtilItemCallback
import com.bimilyoncu.sscoderr.libretubess.ui.base.BaseActivity
import com.bimilyoncu.sscoderr.libretubess.ui.extensions.setupSubscriptionButton
import com.bimilyoncu.sscoderr.libretubess.ui.sheets.ChannelOptionsBottomSheet
import com.bimilyoncu.sscoderr.libretubess.ui.viewholders.SubscriptionChannelViewHolder

class SubscriptionChannelAdapter :
    ListAdapter<Subscription, SubscriptionChannelViewHolder>(DiffUtilItemCallback()) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): SubscriptionChannelViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ChannelSubscriptionRowBinding.inflate(layoutInflater, parent, false)
        return SubscriptionChannelViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SubscriptionChannelViewHolder, position: Int) {
        val subscription = getItem(holder.bindingAdapterPosition)

        holder.binding.apply {
            subscriptionChannelName.text = subscription.name
            ImageHelper.loadImage(subscription.avatar, subscriptionChannelImage, true)

            root.setOnClickListener {
                NavigationHelper.navigateChannel(root.context, subscription.url)
            }
            root.setOnLongClickListener {
                val channelOptionsSheet = ChannelOptionsBottomSheet()
                channelOptionsSheet.arguments = bundleOf(
                    IntentData.channelId to subscription.url.toID(),
                    IntentData.channelName to subscription.name,
                    IntentData.isSubscribed to true
                )
                channelOptionsSheet.show((root.context as BaseActivity).supportFragmentManager)
                true
            }

            subscriptionSubscribe.setupSubscriptionButton(
                subscription.url.toID(),
                subscription.name,
                subscription.avatar,
                subscription.verified,
                notificationBell,
                true
            )
        }
    }
}
