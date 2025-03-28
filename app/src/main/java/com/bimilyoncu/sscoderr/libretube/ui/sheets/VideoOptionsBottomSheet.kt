package com.bimilyoncu.sscoderr.libretube.ui.sheets

import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.navigation.fragment.NavHostFragment
import com.bimilyoncu.sscoderr.libretube.R
import com.bimilyoncu.sscoderr.libretube.api.obj.StreamItem
import com.bimilyoncu.sscoderr.libretube.constants.IntentData
import com.bimilyoncu.sscoderr.libretube.constants.PreferenceKeys
import com.bimilyoncu.sscoderr.libretube.db.DatabaseHelper
import com.bimilyoncu.sscoderr.libretube.db.DatabaseHolder
import com.bimilyoncu.sscoderr.libretube.db.obj.WatchPosition
import com.bimilyoncu.sscoderr.libretube.enums.ShareObjectType
import com.bimilyoncu.sscoderr.libretube.extensions.parcelable
import com.bimilyoncu.sscoderr.libretube.extensions.toID
import com.bimilyoncu.sscoderr.libretube.helpers.DownloadHelper
import com.bimilyoncu.sscoderr.libretube.helpers.NavigationHelper
import com.bimilyoncu.sscoderr.libretube.helpers.PlayerHelper
import com.bimilyoncu.sscoderr.libretube.helpers.PreferenceHelper
import com.bimilyoncu.sscoderr.libretube.helpers.VersionControlHelper
import com.bimilyoncu.sscoderr.libretube.obj.ShareData
import com.bimilyoncu.sscoderr.libretube.ui.activities.MainActivity
import com.bimilyoncu.sscoderr.libretube.ui.dialogs.AddToPlaylistDialog
import com.bimilyoncu.sscoderr.libretube.ui.dialogs.ShareDialog
import com.bimilyoncu.sscoderr.libretube.ui.fragments.SubscriptionsFragment
import com.bimilyoncu.sscoderr.libretube.util.PlayingQueue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

/**
 * Dialog with different options for a selected video.
 *
 * Needs the [streamItem] to load the content from the right video.
 */
class VideoOptionsBottomSheet : BaseBottomSheet() {
    private lateinit var streamItem: StreamItem
    private var isCurrentlyPlaying = false

    override fun onCreate(savedInstanceState: Bundle?) {
        streamItem = arguments?.parcelable(IntentData.streamItem)!!
        isCurrentlyPlaying = arguments?.getBoolean(IntentData.isCurrentlyPlaying) ?: false

        val videoId = streamItem.url?.toID() ?: return

        setTitle(streamItem.title)

        val optionsList = mutableListOf<Int>()
        if (!isCurrentlyPlaying) {
            optionsList += getOptionsForNotActivePlayback(videoId)
        }

        // Add all options except download first
        optionsList += listOf(R.string.addToPlaylist, R.string.share)
        
        // Add download conditionally based on live status
        if (!streamItem.isLive) {
            // Check cached version control status first
            val cachedStatus = VersionControlHelper.getCachedControlFeaturesStatus()
            if (cachedStatus == null || cachedStatus) {
                // If cached status is null or true, add download option immediately
                optionsList += R.string.download
            }
        }
        
        // Set the items synchronously before super.onCreate() is called
        setSimpleItems(optionsList.map { getString(it) }) { which ->
            when (optionsList[which]) {
                // Start the background mode
                R.string.playOnBackground -> {
                    NavigationHelper.navigateAudio(requireContext(), videoId, minimizeByDefault = true)
                }
                // Add Video to Playlist Dialog
                R.string.addToPlaylist -> {
                    AddToPlaylistDialog().apply {
                        arguments = bundleOf(IntentData.videoInfo to streamItem)
                    }.show(
                        parentFragmentManager,
                        AddToPlaylistDialog::class.java.name
                    )
                }

                R.string.download -> {
                    DownloadHelper.startDownloadDialog(
                        requireContext(),
                        parentFragmentManager,
                        videoId
                    )
                }

                R.string.share -> {
                    val bundle = bundleOf(
                        IntentData.id to videoId,
                        IntentData.shareObjectType to ShareObjectType.VIDEO,
                        IntentData.shareData to ShareData(currentVideo = streamItem.title)
                    )
                    val newShareDialog = ShareDialog()
                    newShareDialog.arguments = bundle
                    // using parentFragmentManager is important here
                    newShareDialog.show(parentFragmentManager, ShareDialog::class.java.name)
                }

                R.string.play_next -> {
                    PlayingQueue.addAsNext(streamItem)
                }

                R.string.add_to_queue -> {
                    PlayingQueue.add(streamItem)
                }

                R.string.mark_as_watched -> {
                    val watchPosition = WatchPosition(videoId, Long.MAX_VALUE)
                    CoroutineScope(Dispatchers.IO).launch {
                        DatabaseHolder.Database.watchPositionDao().insert(watchPosition)

                        if (PlayerHelper.watchHistoryEnabled) {
                            DatabaseHelper.addToWatchHistory(streamItem.toWatchHistoryItem(videoId))
                        }
                    }
                    if (PreferenceHelper.getBoolean(PreferenceKeys.HIDE_WATCHED_FROM_FEED, false)) {
                        // get the host fragment containing the current fragment
                        val navHostFragment = (context as MainActivity).supportFragmentManager
                            .findFragmentById(R.id.fragment) as NavHostFragment?
                        // get the current fragment
                        val fragment = navHostFragment?.childFragmentManager?.fragments
                            ?.firstOrNull() as? SubscriptionsFragment
                        fragment?.removeItem(videoId)
                    }
                    setFragmentResult(VIDEO_OPTIONS_SHEET_REQUEST_KEY, bundleOf())
                }

                R.string.mark_as_unwatched -> {
                    CoroutineScope(Dispatchers.IO).launch {
                        DatabaseHolder.Database.watchPositionDao().deleteByVideoId(videoId)
                        DatabaseHolder.Database.watchHistoryDao().deleteByVideoId(videoId)
                    }
                    setFragmentResult(VIDEO_OPTIONS_SHEET_REQUEST_KEY, bundleOf())
                }
            }
        }

        super.onCreate(savedInstanceState)
    }

    private fun getOptionsForNotActivePlayback(videoId: String): List<Int> {
        // List that stores the different menu options. In the future could be add more options here.
        val optionsList = mutableListOf<Int>()
        
        // Add background play option only if it should be visible based on version control
        val cachedStatus = VersionControlHelper.getCachedControlFeaturesStatus()
        if (cachedStatus == null || cachedStatus) {
            optionsList += R.string.playOnBackground
        }

        // Check whether the player is running and add queue options
        if (PlayingQueue.isNotEmpty()) {
            optionsList += R.string.play_next
            optionsList += R.string.add_to_queue
        }

        // show the mark as watched or unwatched option if watch positions are enabled
        if (PlayerHelper.watchPositionsAny || PlayerHelper.watchHistoryEnabled) {
            val watchHistoryEntry = runBlocking(Dispatchers.IO) {
                DatabaseHolder.Database.watchHistoryDao().findById(videoId)
            }

            val position = DatabaseHelper.getWatchPositionBlocking(videoId) ?: 0
            val isCompleted = DatabaseHelper.isVideoWatched(position, streamItem.duration ?: 0)
            if (position != 0L || watchHistoryEntry != null) {
                optionsList += R.string.mark_as_unwatched
            }

            if (!isCompleted || watchHistoryEntry == null) {
                optionsList += R.string.mark_as_watched
            }
        }

        return optionsList
    }

    companion object {
        const val VIDEO_OPTIONS_SHEET_REQUEST_KEY = "video_options_sheet_request_key"
    }
}
