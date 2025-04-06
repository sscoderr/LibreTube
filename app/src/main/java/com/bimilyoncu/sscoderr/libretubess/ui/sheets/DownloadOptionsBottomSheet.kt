package com.bimilyoncu.sscoderr.libretubess.ui.sheets

import android.content.Intent
import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import com.bimilyoncu.sscoderr.libretubess.R
import com.bimilyoncu.sscoderr.libretubess.constants.IntentData
import com.bimilyoncu.sscoderr.libretubess.enums.ShareObjectType
import com.bimilyoncu.sscoderr.libretubess.extensions.serializable
import com.bimilyoncu.sscoderr.libretubess.helpers.BackgroundHelper
import com.bimilyoncu.sscoderr.libretubess.helpers.ContextHelper
import com.bimilyoncu.sscoderr.libretubess.helpers.NavigationHelper
import com.bimilyoncu.sscoderr.libretubess.obj.ShareData
import com.bimilyoncu.sscoderr.libretubess.ui.activities.NoInternetActivity
import com.bimilyoncu.sscoderr.libretubess.ui.dialogs.ShareDialog
import com.bimilyoncu.sscoderr.libretubess.ui.fragments.DownloadTab

class DownloadOptionsBottomSheet : BaseBottomSheet() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val videoId = arguments?.getString(IntentData.videoId)!!
        val downloadTab = arguments?.serializable<DownloadTab>(IntentData.downloadTab)!!

        val options = mutableListOf(
            R.string.playOnBackground,
            R.string.go_to_video,
            R.string.share,
            R.string.delete
        )

        // can't navigate to video while in offline activity
        if (ContextHelper.tryUnwrapActivity<NoInternetActivity>(requireContext()) != null) {
            options.remove(R.string.go_to_video)
        }

        setSimpleItems(options.map { getString(it) }) { which ->
            when (options[which]) {
                R.string.playOnBackground -> {
                    BackgroundHelper.playOnBackgroundOffline(requireContext(), videoId, downloadTab)
                }

                R.string.go_to_video -> {
                    NavigationHelper.navigateVideo(
                        requireContext(), 
                        videoUrlOrId = videoId,
                        resumeFromSavedPosition = false
                    )
                }

                R.string.share -> {
                    // Create video share URL
                    val shareUrl = "${ShareDialog.YOUTUBE_SHORT_URL}/$videoId"
                    
                    // Create and launch system share intent directly
                    val intent = Intent(Intent.ACTION_SEND)
                        .putExtra(Intent.EXTRA_TEXT, shareUrl)
                        .putExtra(Intent.EXTRA_SUBJECT, videoId)
                        .setType("text/plain")
                    val shareIntent = Intent.createChooser(intent, getString(R.string.shareTo))
                    requireContext().startActivity(shareIntent)
                }

                R.string.delete -> {
                    setFragmentResult(DELETE_DOWNLOAD_REQUEST_KEY, bundleOf())
                    dialog?.dismiss()
                }
            }
        }

        super.onCreate(savedInstanceState)
    }

    companion object {
        const val DELETE_DOWNLOAD_REQUEST_KEY = "delete_download_request_key"
    }
}
