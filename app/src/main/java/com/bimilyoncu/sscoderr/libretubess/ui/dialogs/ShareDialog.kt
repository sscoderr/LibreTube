package com.bimilyoncu.sscoderr.libretubess.ui.dialogs

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import com.bimilyoncu.sscoderr.libretubess.R
import com.bimilyoncu.sscoderr.libretubess.constants.IntentData
import com.bimilyoncu.sscoderr.libretubess.constants.PreferenceKeys
import com.bimilyoncu.sscoderr.libretubess.databinding.DialogShareBinding
import com.bimilyoncu.sscoderr.libretubess.db.DatabaseHelper
import com.bimilyoncu.sscoderr.libretubess.enums.ShareObjectType
import com.bimilyoncu.sscoderr.libretubess.extensions.parcelable
import com.bimilyoncu.sscoderr.libretubess.extensions.serializable
import com.bimilyoncu.sscoderr.libretubess.helpers.ClipboardHelper
import com.bimilyoncu.sscoderr.libretubess.helpers.PreferenceHelper
import com.bimilyoncu.sscoderr.libretubess.obj.ShareData
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class ShareDialog : DialogFragment() {
    private lateinit var id: String
    private lateinit var shareObjectType: ShareObjectType
    private lateinit var shareData: ShareData

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            id = it.getString(IntentData.id)!!
            shareObjectType = it.serializable(IntentData.shareObjectType)!!
            shareData = it.parcelable(IntentData.shareData)!!
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val shareableTitle = shareData.currentChannel
            ?: shareData.currentVideo
            ?: shareData.currentPlaylist.orEmpty()

        val binding = DialogShareBinding.inflate(layoutInflater)

        // Hide the radio button group as we're always using YouTube now
        binding.shareHostGroup.isVisible = false

        if (shareObjectType == ShareObjectType.VIDEO) {
            binding.timeStampSwitchLayout.isVisible = true
            binding.timeCodeSwitch.isChecked = PreferenceHelper.getBoolean(
                PreferenceKeys.SHARE_WITH_TIME_CODE,
                false
            )
            binding.timeCodeSwitch.setOnCheckedChangeListener { _, isChecked ->
                binding.timeStampInputLayout.isVisible = isChecked
                PreferenceHelper.putBoolean(PreferenceKeys.SHARE_WITH_TIME_CODE, isChecked)
            }
            val timeStamp = shareData.currentPosition ?: DatabaseHelper.getWatchPositionBlocking(id)?.div(1000)
            binding.timeStamp.setText((timeStamp ?: 0L).toString())
            if (binding.timeCodeSwitch.isChecked) {
                binding.timeStampInputLayout.isVisible = true
            }
        }

        binding.copyLink.setOnClickListener {
            val url = generateLinkText(binding)
            ClipboardHelper.save(requireContext(), text = url)
        }

        return MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.share))
            .setView(binding.root)
            .setPositiveButton(R.string.share) { _, _ ->
                val url = generateLinkText(binding)
                val intent = Intent(Intent.ACTION_SEND)
                    .putExtra(Intent.EXTRA_TEXT, url)
                    .putExtra(Intent.EXTRA_SUBJECT, shareableTitle)
                    .setType("text/plain")
                val shareIntent = Intent.createChooser(intent, getString(R.string.shareTo))
                requireContext().startActivity(shareIntent)
            }
            .show()
    }

    private fun generateLinkText(binding: DialogShareBinding): String {
        // Always use YouTube as the host
        val host = YOUTUBE_FRONTEND_URL
        
        var url = when {
            shareObjectType == ShareObjectType.VIDEO -> "$YOUTUBE_SHORT_URL/$id"
            shareObjectType == ShareObjectType.PLAYLIST -> "$host/playlist?list=$id"
            else -> "$host/channel/$id"
        }

        if (shareObjectType == ShareObjectType.VIDEO && binding.timeCodeSwitch.isChecked) {
            url += "&t=${binding.timeStamp.text}"
        }

        return url
    }

    companion object {
        const val YOUTUBE_FRONTEND_URL = "https://www.youtube.com"
        const val YOUTUBE_MUSIC_URL = "https://music.youtube.com"
        const val YOUTUBE_SHORT_URL = "https://youtu.be"
        const val PIPED_FRONTEND_URL = "https://piped.video"
    }
}
