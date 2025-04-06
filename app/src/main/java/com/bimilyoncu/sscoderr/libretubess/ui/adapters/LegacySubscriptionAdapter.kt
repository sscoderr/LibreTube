package com.bimilyoncu.sscoderr.libretubess.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.recyclerview.widget.ListAdapter
import com.bimilyoncu.sscoderr.libretubess.api.obj.Subscription
import com.bimilyoncu.sscoderr.libretubess.constants.IntentData
import com.bimilyoncu.sscoderr.libretubess.databinding.LegacySubscriptionChannelBinding
import com.bimilyoncu.sscoderr.libretubess.extensions.toID
import com.bimilyoncu.sscoderr.libretubess.helpers.ImageHelper
import com.bimilyoncu.sscoderr.libretubess.helpers.NavigationHelper
import com.bimilyoncu.sscoderr.libretubess.ui.adapters.callbacks.DiffUtilItemCallback
import com.bimilyoncu.sscoderr.libretubess.ui.base.BaseActivity
import com.bimilyoncu.sscoderr.libretubess.ui.sheets.ChannelOptionsBottomSheet
import com.bimilyoncu.sscoderr.libretubess.ui.viewholders.LegacySubscriptionViewHolder

class LegacySubscriptionAdapter :
    ListAdapter<Subscription, LegacySubscriptionViewHolder>(DiffUtilItemCallback()) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): LegacySubscriptionViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = LegacySubscriptionChannelBinding.inflate(layoutInflater, parent, false)
        return LegacySubscriptionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: LegacySubscriptionViewHolder, position: Int) {
        val subscription = getItem(holder.bindingAdapterPosition)
        holder.binding.apply {
            channelName.text = subscription.name
            ImageHelper.loadImage(
                subscription.avatar,
                channelAvatar,
                true
            )
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
        }
    }
}
