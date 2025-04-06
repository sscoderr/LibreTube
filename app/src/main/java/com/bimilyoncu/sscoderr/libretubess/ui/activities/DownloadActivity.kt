package com.bimilyoncu.sscoderr.libretubess.ui.activities

import android.content.Intent
import android.os.Bundle
import androidx.core.net.toUri
import androidx.core.os.bundleOf
import androidx.lifecycle.lifecycleScope
import com.bimilyoncu.sscoderr.libretubess.constants.IntentData
import com.bimilyoncu.sscoderr.libretubess.enums.PlaylistType
import com.bimilyoncu.sscoderr.libretubess.helpers.IntentHelper
import com.bimilyoncu.sscoderr.libretubess.helpers.VersionControlHelper
import com.bimilyoncu.sscoderr.libretubess.ui.base.BaseActivity
import com.bimilyoncu.sscoderr.libretubess.ui.dialogs.DownloadDialog
import com.bimilyoncu.sscoderr.libretubess.ui.dialogs.DownloadPlaylistDialog
import kotlinx.coroutines.launch

class DownloadActivity : BaseActivity() {
    override val isDialogActivity: Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val intentData = intent.getStringExtra(Intent.EXTRA_TEXT)?.let {
            IntentHelper.resolveType(it.toUri())
        }

        val videoId = intentData?.getStringExtra(IntentData.videoId)
        val playlistId = intentData?.getStringExtra(IntentData.playlistId)
        
        // Initialize VersionControlHelper if needed
        if (VersionControlHelper.getCachedControlFeaturesStatus() == null) {
            lifecycleScope.launch {
                VersionControlHelper.shouldShowControls(this@DownloadActivity)
            }
        }
        
        if (videoId != null) {
            supportFragmentManager.setFragmentResultListener(
                DownloadDialog.DOWNLOAD_DIALOG_DISMISSED_KEY,
                this
            ) { _, _ -> finish() }

            DownloadDialog().apply {
                arguments = bundleOf(IntentData.videoId to videoId)
            }.show(supportFragmentManager, null)
        } else if (playlistId != null) {
            // Only show the playlist download dialog if controls are not disabled
            val shouldShowControls = VersionControlHelper.getCachedControlFeaturesStatus()
            if (shouldShowControls != false) { // Includes null (not yet determined) or true
                DownloadPlaylistDialog().apply {
                    arguments = bundleOf(
                        IntentData.playlistId to playlistId,
                        IntentData.playlistType to PlaylistType.PUBLIC,
                        IntentData.playlistName to intent.getStringExtra(Intent.EXTRA_TITLE)
                    )
                }.show(supportFragmentManager, null)
            } else {
                // Controls are disabled, just finish the activity
                finish()
            }
        } else {
            finish()
        }
    }
}