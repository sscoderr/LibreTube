package com.bimilyoncu.sscoderr.libretube.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.recyclerview.widget.ListAdapter
import com.bimilyoncu.sscoderr.libretube.api.obj.Subscription
import com.bimilyoncu.sscoderr.libretube.constants.IntentData
import com.bimilyoncu.sscoderr.libretube.databinding.LegacySubscriptionChannelBinding
import com.bimilyoncu.sscoderr.libretube.extensions.toID
import com.bimilyoncu.sscoderr.libretube.helpers.ImageHelper
import com.bimilyoncu.sscoderr.libretube.helpers.NavigationHelper
import com.bimilyoncu.sscoderr.libretube.ui.adapters.callbacks.DiffUtilItemCallback
import com.bimilyoncu.sscoderr.libretube.ui.base.BaseActivity
import com.bimilyoncu.sscoderr.libretube.ui.sheets.ChannelOptionsBottomSheet
import com.bimilyoncu.sscoderr.libretube.ui.viewholders.LegacySubscriptionViewHolder

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
