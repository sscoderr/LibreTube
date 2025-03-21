package com.bimilyoncu.sscoderr.libretube.ui.dialogs

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.DialogFragment
import com.bimilyoncu.sscoderr.libretube.R
import com.bimilyoncu.sscoderr.libretube.api.PlaylistsHelper
import com.bimilyoncu.sscoderr.libretube.constants.IntentData
import com.bimilyoncu.sscoderr.libretube.extensions.TAG
import com.bimilyoncu.sscoderr.libretube.extensions.toastFromMainDispatcher
import com.bimilyoncu.sscoderr.libretube.obj.PipedImportPlaylist
import com.bimilyoncu.sscoderr.libretube.util.TextUtils
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