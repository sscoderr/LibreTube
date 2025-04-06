package com.bimilyoncu.sscoderr.libretubess.ui.sheets

import android.content.Intent
import android.os.Bundle
import androidx.core.os.bundleOf
import com.bimilyoncu.sscoderr.libretubess.R
import com.bimilyoncu.sscoderr.libretubess.api.MediaServiceRepository
import com.bimilyoncu.sscoderr.libretubess.api.PlaylistsHelper
import com.bimilyoncu.sscoderr.libretubess.constants.IntentData
import com.bimilyoncu.sscoderr.libretubess.db.DatabaseHolder
import com.bimilyoncu.sscoderr.libretubess.enums.ImportFormat
import com.bimilyoncu.sscoderr.libretubess.enums.PlaylistType
import com.bimilyoncu.sscoderr.libretubess.enums.ShareObjectType
import com.bimilyoncu.sscoderr.libretubess.extensions.serializable
import com.bimilyoncu.sscoderr.libretubess.extensions.toID
import com.bimilyoncu.sscoderr.libretubess.extensions.toastFromMainDispatcher
import com.bimilyoncu.sscoderr.libretubess.helpers.BackgroundHelper
import com.bimilyoncu.sscoderr.libretubess.helpers.ContextHelper
import com.bimilyoncu.sscoderr.libretubess.helpers.DownloadHelper
import com.bimilyoncu.sscoderr.libretubess.helpers.VersionControlHelper
import com.bimilyoncu.sscoderr.libretubess.obj.ShareData
import com.bimilyoncu.sscoderr.libretubess.ui.activities.MainActivity
import com.bimilyoncu.sscoderr.libretubess.ui.base.BaseActivity
import com.bimilyoncu.sscoderr.libretubess.ui.dialogs.DeletePlaylistDialog
import com.bimilyoncu.sscoderr.libretubess.ui.dialogs.PlaylistDescriptionDialog
import com.bimilyoncu.sscoderr.libretubess.ui.dialogs.RenamePlaylistDialog
import com.bimilyoncu.sscoderr.libretubess.ui.dialogs.ShareDialog
import com.bimilyoncu.sscoderr.libretubess.ui.preferences.BackupRestoreSettings
import com.bimilyoncu.sscoderr.libretubess.util.PlayingQueue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

class PlaylistOptionsBottomSheet : BaseBottomSheet() {
    private lateinit var playlistName: String
    private lateinit var playlistId: String
    private lateinit var playlistType: PlaylistType

    private var exportFormat: ImportFormat = ImportFormat.NEWPIPE

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            playlistName = it.getString(IntentData.playlistName)!!
            playlistId = it.getString(IntentData.playlistId)!!
            playlistType = it.serializable(IntentData.playlistType)!!
        }

        setTitle(playlistName)

        // Check cached version control status first for feature visibility
        val cachedStatus = VersionControlHelper.getCachedControlFeaturesStatus()
        
        // options for the dialog
        val optionsList = mutableListOf<Int>()
        
        // Add background play option only if it should be visible based on version control
        if (cachedStatus == null || cachedStatus) {
            optionsList.add(R.string.playOnBackground)
        }
        
        // Add download option conditionally based on version control status
        if (cachedStatus == null || cachedStatus) {
            // If cached status is null or true, add download option
            optionsList.add(R.string.download)
        }

        if (PlayingQueue.isNotEmpty()) optionsList.add(R.string.add_to_queue)

        val isBookmarked = runBlocking(Dispatchers.IO) {
            DatabaseHolder.Database.playlistBookmarkDao().includes(playlistId)
        }

        if (playlistType == PlaylistType.PUBLIC) {
            // Add share option conditionally based on version control status
            if (cachedStatus == null || cachedStatus) {
                optionsList.add(R.string.share)
            }
            
            optionsList.add(R.string.clonePlaylist)

            // only add the bookmark option to the playlist if public
            optionsList.add(
                if (isBookmarked) R.string.remove_bookmark else R.string.add_to_bookmarks
            )
        } else {
            optionsList.add(R.string.export_playlist)
            optionsList.add(R.string.renamePlaylist)
            optionsList.add(R.string.change_playlist_description)
            optionsList.add(R.string.deletePlaylist)
        }

        setSimpleItems(optionsList.map { getString(it) }) { which ->
            val mFragmentManager = (context as BaseActivity).supportFragmentManager

            when (optionsList[which]) {
                // play the playlist in the background
                R.string.playOnBackground -> {
                    val playlist = withContext(Dispatchers.IO) {
                        runCatching { PlaylistsHelper.getPlaylist(playlistId) }
                    }.getOrElse {
                        context?.toastFromMainDispatcher(R.string.error)
                        return@setSimpleItems
                    }

                    playlist.relatedStreams.firstOrNull()?.let {
                        BackgroundHelper.playOnBackground(
                            requireContext(),
                            it.url!!.toID(),
                            playlistId = playlistId
                        )
                    }
                }

                R.string.add_to_queue -> {
                    PlayingQueue.insertPlaylist(playlistId, null)
                }
                // Clone the playlist to the users Piped account
                R.string.clonePlaylist -> {
                    val context = requireContext()
                    val playlistId = withContext(Dispatchers.IO) {
                        runCatching {
                            PlaylistsHelper.clonePlaylist(playlistId)
                        }.getOrNull()
                    }
                    context.toastFromMainDispatcher(
                        if (playlistId != null) R.string.playlistCloned else R.string.server_error
                    )
                }
                // share the playlist
                R.string.share -> {
                    // Create playlist share URL
                    val playlistUrl = "${ShareDialog.YOUTUBE_FRONTEND_URL}/playlist?list=$playlistId"
                    
                    // Create and launch system share intent directly
                    val intent = Intent(Intent.ACTION_SEND)
                        .putExtra(Intent.EXTRA_TEXT, playlistUrl)
                        .putExtra(Intent.EXTRA_SUBJECT, playlistName)
                        .setType("text/plain")
                    val shareIntent = Intent.createChooser(intent, getString(R.string.shareTo))
                    requireContext().startActivity(shareIntent)
                }

                R.string.deletePlaylist -> {
                    val newDeletePlaylistDialog = DeletePlaylistDialog()
                    newDeletePlaylistDialog.arguments = bundleOf(
                        IntentData.playlistId to playlistId
                    )
                    newDeletePlaylistDialog.show(mFragmentManager, null)
                }

                R.string.renamePlaylist -> {
                    val newRenamePlaylistDialog = RenamePlaylistDialog()
                    newRenamePlaylistDialog.arguments = bundleOf(
                        IntentData.playlistId to playlistId,
                        IntentData.playlistName to playlistName
                    )
                    newRenamePlaylistDialog.show(mFragmentManager, null)
                }

                R.string.change_playlist_description -> {
                    val newPlaylistDescriptionDialog = PlaylistDescriptionDialog()
                    newPlaylistDescriptionDialog.arguments = bundleOf(
                        IntentData.playlistId to playlistId,
                        IntentData.playlistDescription to ""
                    )
                    newPlaylistDescriptionDialog.show(mFragmentManager, null)
                }

                R.string.download -> {
                    DownloadHelper.startDownloadPlaylistDialog(
                        requireContext(),
                        mFragmentManager,
                        playlistId,
                        playlistName,
                        playlistType
                    )
                }

                R.string.export_playlist -> {
                    val context = requireContext()

                    BackupRestoreSettings.createImportFormatDialog(
                        context,
                        R.string.export_playlist,
                        BackupRestoreSettings.exportPlaylistFormatList + listOf(ImportFormat.URLSORIDS)
                    ) {
                        exportFormat = it
                        ContextHelper.unwrapActivity<MainActivity>(context)
                            .startPlaylistExport(playlistId, playlistName, exportFormat)
                    }
                }

                else -> {
                    withContext(Dispatchers.IO) {
                        if (isBookmarked) {
                            DatabaseHolder.Database.playlistBookmarkDao().deleteById(playlistId)
                        } else {
                            val bookmark = try {
                                MediaServiceRepository.instance.getPlaylist(playlistId)
                            } catch (e: Exception) {
                                return@withContext
                            }.toPlaylistBookmark(playlistId)
                            DatabaseHolder.Database.playlistBookmarkDao().insert(bookmark)
                        }
                    }
                }
            }
        }
    }

    companion object {
        const val PLAYLIST_OPTIONS_REQUEST_KEY = "playlist_options_request_key"
    }
}
