package com.bimilyoncu.sscoderr.libretubess.ui.dialogs

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.DialogFragment
import com.bimilyoncu.sscoderr.libretubess.R
import com.bimilyoncu.sscoderr.libretubess.api.PlaylistsHelper
import com.bimilyoncu.sscoderr.libretubess.constants.IntentData
import com.bimilyoncu.sscoderr.libretubess.extensions.TAG
import com.bimilyoncu.sscoderr.libretubess.extensions.toastFromMainDispatcher
import com.bimilyoncu.sscoderr.libretubess.obj.PipedImportPlaylist
import com.bimilyoncu.sscoderr.libretubess.util.TextUtils
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ImportTempPlaylistDialog : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val title = arguments?.getString(IntentData.playlistName)
            ?.takeIf { it.isNotEmpty() }
            ?: TextUtils.getFileSafeTimeStampNow()
        val videoIds = arguments?.getStringArray(IntentData.videoIds).orEmpty()

        return MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.import_temp_playlist)
            .setMessage(
                requireContext()
                    .getString(R.string.import_temp_playlist_summary, title, videoIds.size)
            )
            .setNegativeButton(R.string.cancel, null)
            .setPositiveButton(R.string.okay) { _, _ ->
                val context = requireContext().applicationContext

                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val playlist = PipedImportPlaylist(
                            name = title,
                            videos = videoIds.toList()
                        )

                        PlaylistsHelper.importPlaylists(listOf(playlist))
                        context.toastFromMainDispatcher(R.string.playlistCreated)
                    } catch (e: Exception) {
                        Log.e(TAG(), e.toString())
                        e.localizedMessage?.let {
                            context.toastFromMainDispatcher(it)
                        }
                    }
                }
            }
            .create()
    }
}