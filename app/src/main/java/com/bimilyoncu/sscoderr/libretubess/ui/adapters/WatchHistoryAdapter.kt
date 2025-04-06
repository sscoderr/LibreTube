package com.bimilyoncu.sscoderr.libretubess.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.recyclerview.widget.ListAdapter
import com.bimilyoncu.sscoderr.libretubess.constants.IntentData
import com.bimilyoncu.sscoderr.libretubess.databinding.VideoRowBinding
import com.bimilyoncu.sscoderr.libretubess.db.DatabaseHolder
import com.bimilyoncu.sscoderr.libretubess.db.obj.WatchHistoryItem
import com.bimilyoncu.sscoderr.libretubess.helpers.ImageHelper
import com.bimilyoncu.sscoderr.libretubess.helpers.NavigationHelper
import com.bimilyoncu.sscoderr.libretubess.ui.adapters.callbacks.DiffUtilItemCallback
import com.bimilyoncu.sscoderr.libretubess.ui.base.BaseActivity
import com.bimilyoncu.sscoderr.libretubess.ui.extensions.setFormattedDuration
import com.bimilyoncu.sscoderr.libretubess.ui.extensions.setWatchProgressLength
import com.bimilyoncu.sscoderr.libretubess.ui.sheets.VideoOptionsBottomSheet
import com.bimilyoncu.sscoderr.libretubess.ui.viewholders.WatchHistoryViewHolder
import com.bimilyoncu.sscoderr.libretubess.util.TextUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class WatchHistoryAdapter :
    ListAdapter<WatchHistoryItem, WatchHistoryViewHolder>(DiffUtilItemCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WatchHistoryViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = VideoRowBinding.inflate(layoutInflater, parent, false)
        return WatchHistoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: WatchHistoryViewHolder, position: Int) {
        val video = getItem(holder.bindingAdapterPosition)
        holder.binding.apply {
            videoTitle.text = video.title
            channelName.text = video.uploader
            videoInfo.text =
                video.uploadDate?.takeIf { !video.isLive }?.let { TextUtils.localizeDate(it) }
            ImageHelper.loadImage(video.thumbnailUrl, thumbnail)

            if (video.duration != null) {
                // we pass in 0 for the uploadDate, as a future video cannot be watched already
                thumbnailDuration.setFormattedDuration(video.duration, null, 0)
            } else {
                thumbnailDurationCard.isGone = true
            }

            if (video.uploaderAvatar != null) {
                ImageHelper.loadImage(video.uploaderAvatar, channelImage, true)
            } else {
                channelImageContainer.isGone = true
            }

            channelImage.setOnClickListener {
                NavigationHelper.navigateChannel(root.context, video.uploaderUrl)
            }

            root.setOnClickListener {
                NavigationHelper.navigateVideo(
                    root.context, 
                    video.videoId,
                    resumeFromSavedPosition = false
                )
            }

            val activity = (root.context as BaseActivity)
            val fragmentManager = activity.supportFragmentManager
            root.setOnLongClickListener {
                fragmentManager.setFragmentResultListener(
                    VideoOptionsBottomSheet.VIDEO_OPTIONS_SHEET_REQUEST_KEY,
                    activity
                ) { _, _ ->
                    notifyItemChanged(position)
                }
                val sheet = VideoOptionsBottomSheet()
                sheet.arguments = bundleOf(IntentData.streamItem to video.toStreamItem())
                sheet.show(fragmentManager, WatchHistoryAdapter::class.java.name)
                true
            }

            if (video.duration != null) watchProgress.setWatchProgressLength(
                video.videoId,
                video.duration
            )

            CoroutineScope(Dispatchers.IO).launch {
                val isDownloaded =
                    DatabaseHolder.Database.downloadDao().exists(video.videoId)

                withContext(Dispatchers.Main) {
                    downloadBadge.isVisible = isDownloaded
                }
            }
        }
    }
}
