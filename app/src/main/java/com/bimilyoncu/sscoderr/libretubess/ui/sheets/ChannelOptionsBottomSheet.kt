package com.bimilyoncu.sscoderr.libretubess.ui.sheets

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.core.os.bundleOf
import com.bimilyoncu.sscoderr.libretubess.R
import com.bimilyoncu.sscoderr.libretubess.api.MediaServiceRepository
import com.bimilyoncu.sscoderr.libretubess.constants.IntentData
import com.bimilyoncu.sscoderr.libretubess.enums.ShareObjectType
import com.bimilyoncu.sscoderr.libretubess.extensions.TAG
import com.bimilyoncu.sscoderr.libretubess.extensions.toID
import com.bimilyoncu.sscoderr.libretubess.helpers.BackgroundHelper
import com.bimilyoncu.sscoderr.libretubess.helpers.NavigationHelper
import com.bimilyoncu.sscoderr.libretubess.helpers.VersionControlHelper
import com.bimilyoncu.sscoderr.libretubess.obj.ShareData
import com.bimilyoncu.sscoderr.libretubess.ui.dialogs.ShareDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Dialog with different options for a selected video.
 *
 * Needs the [channelId] to load the content from the right video.
 */
class ChannelOptionsBottomSheet : BaseBottomSheet() {
    private lateinit var channelId: String
    private var channelName: String? = null
    private var subscribed: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        channelId = arguments?.getString(IntentData.channelId)!!
        channelName = arguments?.getString(IntentData.channelName)
        subscribed = arguments?.getBoolean(IntentData.isSubscribed, false) ?: false

        setTitle(channelName)

        // List that stores the different menu options. In the future could be add more options here.
        val optionsList = mutableListOf<Int>()
        
        // Check cached version control status for feature visibility
        val cachedStatus = VersionControlHelper.getCachedControlFeaturesStatus()
        
        // Add share option conditionally based on version control status
        if (cachedStatus == null || cachedStatus) {
            optionsList += R.string.share
        }
        
        // Play latest videos and background play options
        optionsList += R.string.play_latest_videos
        
        // Add background play option only if it should be visible based on version control
        if (cachedStatus == null || cachedStatus) {
            optionsList += R.string.playOnBackground
        }
        
        // Add to group option
        if (subscribed) optionsList.add(R.string.add_to_group)

        setSimpleItems(optionsList.map { getString(it) }) { which ->
            when (optionsList[which]) {
                R.string.share -> {
                    // Create channel share URL
                    val channelUrl = "${ShareDialog.YOUTUBE_FRONTEND_URL}/channel/$channelId"
                    
                    // Create and launch system share intent directly
                    val intent = Intent(Intent.ACTION_SEND)
                        .putExtra(Intent.EXTRA_TEXT, channelUrl)
                        .putExtra(Intent.EXTRA_SUBJECT, channelName)
                        .setType("text/plain")
                    val shareIntent = Intent.createChooser(intent, getString(R.string.shareTo))
                    requireContext().startActivity(shareIntent)
                }

                R.string.add_to_group -> {
                    val sheet = AddChannelToGroupSheet().apply {
                        arguments = bundleOf(IntentData.channelId to channelId)
                    }
                    sheet.show(parentFragmentManager, null)
                }

                R.string.play_latest_videos -> {
                    try {
                        val channel = withContext(Dispatchers.IO) {
                            MediaServiceRepository.instance.getChannel(channelId)
                        }
                        channel.relatedStreams.firstOrNull()?.url?.toID()?.let {
                            NavigationHelper.navigateVideo(
                                requireContext(),
                                it,
                                channelId = channelId,
                                resumeFromSavedPosition = false
                            )
                        }
                    } catch (e: Exception) {
                        Log.e(TAG(), e.toString())
                    }
                }

                R.string.playOnBackground -> {
                    try {
                        val channel = withContext(Dispatchers.IO) {
                            MediaServiceRepository.instance.getChannel(channelId)
                        }
                        channel.relatedStreams.firstOrNull()?.url?.toID()?.let {
                            BackgroundHelper.playOnBackground(
                                requireContext(),
                                videoId = it,
                                channelId = channelId
                            )
                        }
                    } catch (e: Exception) {
                        Log.e(TAG(), e.toString())
                    }
                }
            }
        }
    }
}
