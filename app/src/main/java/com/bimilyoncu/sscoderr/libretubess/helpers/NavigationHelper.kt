package com.bimilyoncu.sscoderr.libretubess.helpers

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.os.Process
import androidx.annotation.OptIn
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.core.content.getSystemService
import androidx.core.os.bundleOf
import androidx.core.os.postDelayed
import androidx.fragment.app.commitNow
import androidx.fragment.app.replace
import androidx.media3.common.util.UnstableApi
import com.bimilyoncu.sscoderr.libretubess.NavDirections
import com.bimilyoncu.sscoderr.libretubess.R
import com.bimilyoncu.sscoderr.libretubess.constants.IntentData
import com.bimilyoncu.sscoderr.libretubess.constants.PreferenceKeys
import com.bimilyoncu.sscoderr.libretubess.enums.PlaylistType
import com.bimilyoncu.sscoderr.libretubess.extensions.toID
import com.bimilyoncu.sscoderr.libretubess.parcelable.PlayerData
import com.bimilyoncu.sscoderr.libretubess.ui.activities.MainActivity
import com.bimilyoncu.sscoderr.libretubess.ui.activities.ZoomableImageActivity
import com.bimilyoncu.sscoderr.libretubess.ui.base.BaseActivity
import com.bimilyoncu.sscoderr.libretubess.ui.fragments.AudioPlayerFragment
import com.bimilyoncu.sscoderr.libretubess.ui.fragments.PlayerFragment
import com.bimilyoncu.sscoderr.libretubess.util.PlayingQueue

object NavigationHelper {
    private val handler = Handler(Looper.getMainLooper())

    fun navigateChannel(context: Context, channelUrlOrId: String?) {
        if (channelUrlOrId == null) return

        val activity = ContextHelper.unwrapActivity<MainActivity>(context)
        activity.navController.navigate(NavDirections.openChannel(channelUrlOrId.toID()))
        try {
            // minimize player if currently expanded
            if (activity.binding.mainMotionLayout.progress == 0f) {
                activity.binding.mainMotionLayout.transitionToEnd()
                activity.findViewById<MotionLayout>(R.id.playerMotionLayout)
                    .transitionToEnd()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Navigate to the given video using the other provided parameters as well
     * If the audio only mode is enabled, play it in the background, else as a normal video
     */
    fun navigateVideo(
        context: Context,
        videoUrlOrId: String?,
        playlistId: String? = null,
        channelId: String? = null,
        keepQueue: Boolean = false,
        timestamp: Long = 0,
        forceVideo: Boolean = false,
        resumeFromSavedPosition: Boolean = false
    ) {
        if (videoUrlOrId == null) return

        // Register this video for position tracking if resumeFromSavedPosition is true
        // (meaning it came from "Continue watching" section)
        if (resumeFromSavedPosition) {
            PlayerHelper.registerVideoForPositionTracking(videoUrlOrId.toID())
        }

        if (PreferenceHelper.getBoolean(PreferenceKeys.AUDIO_ONLY_MODE, false) && !forceVideo) {
            navigateAudio(context, videoUrlOrId.toID(), playlistId, channelId, keepQueue, timestamp, resumeFromSavedPosition = resumeFromSavedPosition)
            return
        }

        val activity = ContextHelper.unwrapActivity<MainActivity>(context)
        val attachedToRunningPlayer = activity.runOnPlayerFragment {
            try {
                this.playNextVideo(videoUrlOrId.toID())
                // maximize player
                this.binding.playerMotionLayout.transitionToStart()
                PlayingQueue.clear()
                true
            } catch (e: Exception) {
                this.onDestroy()
                false
            }
        }
        if (attachedToRunningPlayer) return

        val playerData =
            PlayerData(videoUrlOrId.toID(), playlistId, channelId, keepQueue, timestamp, resumeFromSavedPosition)
        val bundle = bundleOf(IntentData.playerData to playerData)
        activity.supportFragmentManager.commitNow {
            replace<PlayerFragment>(R.id.container, args = bundle)
        }
    }

    @OptIn(UnstableApi::class)
    fun navigateAudio(
        context: Context,
        videoId: String,
        playlistId: String? = null,
        channelId: String? = null,
        keepQueue: Boolean = false,
        timestamp: Long = 0,
        minimizeByDefault: Boolean = false,
        resumeFromSavedPosition: Boolean = false
    ) {
        // For audio-only mode, we always start from the beginning, even for videos from "Continue watching"
        // We still register the video for position tracking if it came from "Continue watching" section
        // This ensures video mode remembers the position, but audio mode always starts from beginning
        if (resumeFromSavedPosition) {
            PlayerHelper.registerVideoForPositionTracking(videoId)
        }
        
        val activity = ContextHelper.unwrapActivity<MainActivity>(context)
        val attachedToRunningPlayer = activity.runOnAudioPlayerFragment {
            this.playNextVideo(videoId)
            true
        }
        if (attachedToRunningPlayer) return

        BackgroundHelper.playOnBackground(
            context,
            videoId,
            timestamp,
            playlistId,
            channelId,
            keepQueue,
            resumeFromSavedPosition = false // Always set to false for audio-only mode
        )

        handler.postDelayed(500) {
            openAudioPlayerFragment(context, minimizeByDefault = minimizeByDefault)
        }
    }

    fun navigatePlaylist(context: Context, playlistUrlOrId: String?, playlistType: PlaylistType) {
        if (playlistUrlOrId == null) return

        val activity = ContextHelper.unwrapActivity<MainActivity>(context)
        activity.navController.navigate(
            NavDirections.openPlaylist(playlistUrlOrId.toID(), playlistType)
        )
    }

    /**
     * Start the audio player fragment
     */
    fun openAudioPlayerFragment(
        context: Context,
        offlinePlayer: Boolean = false,
        minimizeByDefault: Boolean = false
    ) {
        val activity = ContextHelper.unwrapActivity<BaseActivity>(context)
        activity.supportFragmentManager.commitNow {
            val args = bundleOf(
                IntentData.minimizeByDefault to minimizeByDefault,
                IntentData.offlinePlayer to offlinePlayer
            )
            replace<AudioPlayerFragment>(R.id.container, args = args)
        }
    }

    /**
     * Open a large, zoomable image preview
     */
    fun openImagePreview(context: Context, url: String) {
        val intent = Intent(context, ZoomableImageActivity::class.java)
        intent.putExtra(IntentData.bitmapUrl, url)
        context.startActivity(intent)
    }

    /**
     * Needed due to different MainActivity Aliases because of the app icons
     */
    fun restartMainActivity(context: Context) {
        // kill player notification
        context.getSystemService<NotificationManager>()!!.cancelAll()
        // start a new Intent of the app
        val pm = context.packageManager
        val intent = pm.getLaunchIntentForPackage(context.packageName)
        intent?.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK
        context.startActivity(intent)
        // kill the old application
        Process.killProcess(Process.myPid())
    }
}
