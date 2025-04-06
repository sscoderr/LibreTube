package com.bimilyoncu.sscoderr.libretubess.ui.sheets

import android.content.Intent
import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.navigation.fragment.NavHostFragment
import com.bimilyoncu.sscoderr.libretubess.R
import com.bimilyoncu.sscoderr.libretubess.api.obj.StreamItem
import com.bimilyoncu.sscoderr.libretubess.constants.IntentData
import com.bimilyoncu.sscoderr.libretubess.constants.PreferenceKeys
import com.bimilyoncu.sscoderr.libretubess.db.DatabaseHelper
import com.bimilyoncu.sscoderr.libretubess.db.DatabaseHolder
import com.bimilyoncu.sscoderr.libretubess.db.obj.WatchPosition
import com.bimilyoncu.sscoderr.libretubess.enums.ShareObjectType
import com.bimilyoncu.sscoderr.libretubess.extensions.parcelable
import com.bimilyoncu.sscoderr.libretubess.extensions.toID
import com.bimilyoncu.sscoderr.libretubess.helpers.DownloadHelper
import com.bimilyoncu.sscoderr.libretubess.helpers.NavigationHelper
import com.bimilyoncu.sscoderr.libretubess.helpers.PlayerHelper
import com.bimilyoncu.sscoderr.libretubess.helpers.PreferenceHelper
import com.bimilyoncu.sscoderr.libretubess.helpers.VersionControlHelper
import com.bimilyoncu.sscoderr.libretubess.obj.ShareData
import com.bimilyoncu.sscoderr.libretubess.ui.activities.MainActivity
import com.bimilyoncu.sscoderr.libretubess.ui.dialogs.AddToPlaylistDialog
import com.bimilyoncu.sscoderr.libretubess.ui.dialogs.ShareDialog
import com.bimilyoncu.sscoderr.libretubess.ui.fragments.SubscriptionsFragment
import com.bimilyoncu.sscoderr.libretubess.util.PlayingQueue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

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

        // Add add to playlist option
        optionsList += R.string.addToPlaylist
        
        // Check cached version control status for feature visibility
        val cachedStatus = VersionControlHelper.getCachedControlFeaturesStatus()
        
        // Add share option conditionally based on version control status
        if (cachedStatus == null || cachedStatus) {
            optionsList += R.string.share
        }
        
        // Add download conditionally based on live status and version control
        if (!streamItem.isLive && (cachedStatus == null || cachedStatus)) {
            // If cached status is null or true, add download option
            optionsList += R.string.download
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
                    // Create video share URL
                    val shareUrl = "${ShareDialog.YOUTUBE_SHORT_URL}/$videoId"
                    
                    // Create and launch system share intent directly
                    val intent = Intent(Intent.ACTION_SEND)
                        .putExtra(Intent.EXTRA_TEXT, shareUrl)
                        .putExtra(Intent.EXTRA_SUBJECT, streamItem.title)
                        .setType("text/plain")
                    val shareIntent = Intent.createChooser(intent, getString(R.string.shareTo))
                    requireContext().startActivity(shareIntent)
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
