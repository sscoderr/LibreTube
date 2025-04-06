package com.bimilyoncu.sscoderr.libretubess.ui.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.recyclerview.widget.ListAdapter
import com.bimilyoncu.sscoderr.libretubess.R
import com.bimilyoncu.sscoderr.libretubess.constants.IntentData
import com.bimilyoncu.sscoderr.libretubess.databinding.VideoRowBinding
import com.bimilyoncu.sscoderr.libretubess.db.DatabaseHelper
import com.bimilyoncu.sscoderr.libretubess.db.DatabaseHolder
import com.bimilyoncu.sscoderr.libretubess.db.obj.DownloadWithItems
import com.bimilyoncu.sscoderr.libretubess.extensions.formatAsFileSize
import com.bimilyoncu.sscoderr.libretubess.helpers.BackgroundHelper
import com.bimilyoncu.sscoderr.libretubess.helpers.ImageHelper
import com.bimilyoncu.sscoderr.libretubess.helpers.NavigationHelper
import com.bimilyoncu.sscoderr.libretubess.ui.activities.OfflinePlayerActivity
import com.bimilyoncu.sscoderr.libretubess.ui.adapters.callbacks.DiffUtilItemCallback
import com.bimilyoncu.sscoderr.libretubess.ui.base.BaseActivity
import com.bimilyoncu.sscoderr.libretubess.ui.extensions.setWatchProgressLength
import com.bimilyoncu.sscoderr.libretubess.ui.fragments.DownloadTab
import com.bimilyoncu.sscoderr.libretubess.ui.sheets.DownloadOptionsBottomSheet
import com.bimilyoncu.sscoderr.libretubess.ui.sheets.DownloadOptionsBottomSheet.Companion.DELETE_DOWNLOAD_REQUEST_KEY
import com.bimilyoncu.sscoderr.libretubess.ui.viewholders.DownloadsViewHolder
import com.bimilyoncu.sscoderr.libretubess.util.TextUtils
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlin.io.path.deleteIfExists
import kotlin.io.path.exists
import kotlin.io.path.fileSize

class DownloadsAdapter(
    private val context: Context,
    private val downloadTab: DownloadTab,
    private val toggleDownload: (DownloadWithItems) -> Boolean
) : ListAdapter<DownloadWithItems, DownloadsViewHolder>(DiffUtilItemCallback()) {
    val items get() = (0 until itemCount).map { getItem(it) }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DownloadsViewHolder {
        val binding = VideoRowBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return DownloadsViewHolder(binding)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: DownloadsViewHolder, position: Int) {
        val download = getItem(holder.bindingAdapterPosition).download
        val items = getItem(holder.bindingAdapterPosition).downloadItems

        holder.binding.apply {
            fileSize.isVisible = true

            channelImageContainer.isGone = true
            videoTitle.text = download.title
            channelName.text = download.uploader
            videoInfo.text = download.uploadDate?.let { TextUtils.localizeDate(it) }
            watchProgress.setWatchProgressLength(download.videoId, download.duration ?: 0)

            val downloadSize = items.sumOf { it.downloadSize }
            val currentSize = items.filter { it.path.exists() }.sumOf { it.path.fileSize() }

            if (downloadSize == -1L) {
                progressBar.isIndeterminate = true
            } else {
                progressBar.max = downloadSize.toInt()
                progressBar.progress = currentSize.toInt()
            }

            val totalSizeInfo = if (downloadSize > 0) {
                downloadSize.formatAsFileSize()
            } else {
                context.getString(R.string.unknown)
            }
            if (downloadSize > currentSize) {
                downloadOverlay.isVisible = true
                resumePauseBtn.setImageResource(R.drawable.ic_download)
                fileSize.text = "${currentSize.formatAsFileSize()} / $totalSizeInfo"
            } else {
                downloadOverlay.isGone = true
                fileSize.text = totalSizeInfo
                thumbnailDurationCard.isVisible = true
                download.duration?.let {
                    thumbnailDuration.text = DateUtils.formatElapsedTime(it)
                }
            }

            download.thumbnailPath?.let { path ->
                ImageHelper.loadImage(path.toString(), thumbnail)
            }

            progressBar.setOnClickListener {
                val isDownloading = toggleDownload(getItem(holder.bindingAdapterPosition))

                resumePauseBtn.setImageResource(
                    if (isDownloading) {
                        R.drawable.ic_pause
                    } else {
                        R.drawable.ic_download
                    }
                )
            }

            root.setOnClickListener {
                if (downloadTab == DownloadTab.VIDEO) {
                    val intent = Intent(root.context, OfflinePlayerActivity::class.java)
                    intent.putExtra(IntentData.videoId, download.videoId)
                    root.context.startActivity(intent)
                } else {
                    BackgroundHelper.playOnBackgroundOffline(
                        root.context,
                        download.videoId,
                        downloadTab
                    )
                    NavigationHelper.openAudioPlayerFragment(root.context, offlinePlayer = true)
                }
            }

            root.setOnLongClickListener {
                val activity = root.context as BaseActivity
                val fragmentManager = activity.supportFragmentManager
                fragmentManager.setFragmentResultListener(
                    DELETE_DOWNLOAD_REQUEST_KEY,
                    activity
                ) { _, _ ->
                    showDeleteDialog(root.context, position)
                }
                DownloadOptionsBottomSheet()
                    .apply {
                        arguments = bundleOf(
                            IntentData.videoId to download.videoId,
                            IntentData.channelName to download.uploader,
                            IntentData.downloadTab to downloadTab
                        )
                    }
                    .show(fragmentManager)
                true
            }
        }
    }

    fun showDeleteDialog(context: Context, position: Int) {
        MaterialAlertDialogBuilder(context)
            .setTitle(R.string.delete)
            .setMessage(R.string.irreversible)
            .setPositiveButton(R.string.okay) { _, _ ->
                deleteDownload(position)
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun deleteDownloadContent(downloadWithItems: DownloadWithItems) {
        val download = downloadWithItems.download
        val items = downloadWithItems.downloadItems

        items.forEach {
            it.path.deleteIfExists()
        }
        runCatching {
            download.thumbnailPath?.deleteIfExists()
        }

        runBlocking(Dispatchers.IO) {
            DatabaseHolder.Database.downloadDao().deleteDownload(download)
        }
    }

    private fun deleteDownload(position: Int) {
        deleteDownloadContent(getItem(position))

        submitList(currentList.toMutableList().also {
            it.removeAt(position)
        })
    }

    fun deleteAllDownloads(onlyDeleteWatched: Boolean) {
        val (toDelete, toKeep) = items.partition {
            !onlyDeleteWatched || runBlocking(Dispatchers.IO) {
                DatabaseHelper.isVideoWatched(it.download.videoId, it.download.duration ?: 0)
            }
        }

        for (item in toDelete) {
            deleteDownloadContent(item)
        }

        submitList(toKeep)
    }

    fun restoreItem(position: Int) {
        // moves the item back to its initial horizontal position
        notifyItemRemoved(position)
        notifyItemInserted(position)
    }
}
