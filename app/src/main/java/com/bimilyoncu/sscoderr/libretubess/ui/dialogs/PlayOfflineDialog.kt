package com.bimilyoncu.sscoderr.libretubess.ui.dialogs

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import com.bimilyoncu.sscoderr.libretubess.R
import com.bimilyoncu.sscoderr.libretubess.constants.IntentData
import com.bimilyoncu.sscoderr.libretubess.databinding.DialogPlayOfflineBinding
import com.bimilyoncu.sscoderr.libretubess.ui.activities.OfflinePlayerActivity
import com.bimilyoncu.sscoderr.libretubess.util.TextUtils
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class PlayOfflineDialog : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val binding = DialogPlayOfflineBinding.inflate(layoutInflater)
        val videoId = requireArguments().getString(IntentData.videoId)
        binding.videoTitle.text = requireArguments().getString(IntentData.videoTitle)

        val downloadInfo = requireArguments().getStringArray(IntentData.downloadInfo)
        binding.downloadInfo.adapter =
            ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, downloadInfo.orEmpty().map {
                TextUtils.SEPARATOR + it
            })

        return MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.dialog_play_offline_title)
            .setView(binding.root)
            .setPositiveButton(R.string.yes) { _, _ ->
                val intent = Intent(requireContext(), OfflinePlayerActivity::class.java)
                    .putExtra(IntentData.videoId, videoId)
                requireContext().startActivity(intent)

                setFragmentResult(
                    PLAY_OFFLINE_DIALOG_REQUEST_KEY,
                    bundleOf(IntentData.isPlayingOffline to true)
                )
            }
            .setNegativeButton(getString(R.string.cancel)) { _, _ ->
                setFragmentResult(
                    PLAY_OFFLINE_DIALOG_REQUEST_KEY,
                    bundleOf(IntentData.isPlayingOffline to false)
                )
            }
            .show()
    }

    companion object {
        const val PLAY_OFFLINE_DIALOG_REQUEST_KEY = "play_offline_dialog_request_key"
    }
}