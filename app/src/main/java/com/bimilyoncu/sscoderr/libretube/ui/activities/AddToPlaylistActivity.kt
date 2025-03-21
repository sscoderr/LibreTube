package com.bimilyoncu.sscoderr.libretube.ui.activities

import android.content.Intent
import android.os.Bundle
import androidx.core.net.toUri
import androidx.core.os.bundleOf
import androidx.lifecycle.lifecycleScope
import com.bimilyoncu.sscoderr.libretube.R
import com.bimilyoncu.sscoderr.libretube.api.MediaServiceRepository
import com.bimilyoncu.sscoderr.libretube.api.obj.StreamItem
import com.bimilyoncu.sscoderr.libretube.constants.IntentData
import com.bimilyoncu.sscoderr.libretube.extensions.toastFromMainDispatcher
import com.bimilyoncu.sscoderr.libretube.helpers.IntentHelper
import com.bimilyoncu.sscoderr.libretube.helpers.PreferenceHelper
import com.bimilyoncu.sscoderr.libretube.ui.base.BaseActivity
import com.bimilyoncu.sscoderr.libretube.ui.dialogs.AddToPlaylistDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AddToPlaylistActivity : BaseActivity() {
    override val isDialogActivity: Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val videoId = intent.getStringExtra(Intent.EXTRA_TEXT)?.let {
            IntentHelper.resolveType(it.toUri())
        }?.getStringExtra(IntentData.videoId)

        if (videoId == null) {
            finish()
            return
        }

        supportFragmentManager.setFragmentResultListener(
            AddToPlaylistDialog.ADD_TO_PLAYLIST_DIALOG_DISMISSED_KEY,
            this
        ) { _, _ -> finish() }

        lifecycleScope.launch(Dispatchers.IO) {
            val videoInfo = if (PreferenceHelper.getToken().isEmpty()) {
                try {
                    MediaServiceRepository.instance.getStreams(videoId).toStreamItem(videoId)
                } catch (e: Exception) {
                    toastFromMainDispatcher(R.string.unknown_error)
                    withContext(Dispatchers.Main) {
                        finish()
                    }
                    return@launch
                }
            } else {
                StreamItem(videoId)
            }

            withContext(Dispatchers.Main) {
                AddToPlaylistDialog().apply {
                    arguments = bundleOf(IntentData.videoInfo to videoInfo)
                }.show(supportFragmentManager, null)
            }
        }
    }
}