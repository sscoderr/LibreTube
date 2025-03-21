package com.bimilyoncu.sscoderr.libretube.ui.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import androidx.lifecycle.lifecycleScope
import com.bimilyoncu.sscoderr.libretube.R
import com.bimilyoncu.sscoderr.libretube.api.PlaylistsHelper
import com.bimilyoncu.sscoderr.libretube.constants.IntentData
import com.bimilyoncu.sscoderr.libretube.extensions.toastFromMainDispatcher
import com.bimilyoncu.sscoderr.libretube.ui.sheets.PlaylistOptionsBottomSheet
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DeletePlaylistDialog : DialogFragment() {
    private lateinit var playlistId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            playlistId = it.getString(IntentData.playlistId)!!
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.deletePlaylist)
            .setMessage(R.string.areYouSure)
            .setPositiveButton(R.string.yes, null)
            .setNegativeButton(R.string.cancel, null)
            .show()
            .apply {
                getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                    lifecycleScope.launch(Dispatchers.IO) {
                        val success = PlaylistsHelper.deletePlaylist(playlistId)
                        context.toastFromMainDispatcher(
                            if (success) R.string.success else R.string.fail
                        )
                        withContext(Dispatchers.Main) {
                            dismiss()

                            setFragmentResult(
                                PlaylistOptionsBottomSheet.PLAYLIST_OPTIONS_REQUEST_KEY,
                                bundleOf(IntentData.playlistTask to true)
                            )
                        }
                    }
                }
            }
    }
}
