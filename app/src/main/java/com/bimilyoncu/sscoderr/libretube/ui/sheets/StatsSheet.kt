package com.bimilyoncu.sscoderr.libretube.ui.sheets

import android.os.Bundle
import android.view.View
import com.bimilyoncu.sscoderr.libretube.R
import com.bimilyoncu.sscoderr.libretube.constants.IntentData
import com.bimilyoncu.sscoderr.libretube.databinding.DialogStatsBinding
import com.bimilyoncu.sscoderr.libretube.extensions.parcelable
import com.bimilyoncu.sscoderr.libretube.helpers.ClipboardHelper
import com.bimilyoncu.sscoderr.libretube.obj.VideoStats

class StatsSheet : ExpandedBottomSheet(R.layout.dialog_stats) {
    private lateinit var stats: VideoStats

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        stats = arguments?.parcelable(IntentData.videoStats)!!
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val binding = DialogStatsBinding.bind(view)
        binding.videoId.setText(stats.videoId)
        binding.videoIdCopy.setEndIconOnClickListener {
            ClipboardHelper.save(requireContext(), "text", stats.videoId)
        }
        binding.videoInfo.setText(stats.videoInfo)
        binding.audioInfo.setText(stats.audioInfo)
        binding.videoQuality.setText(stats.videoQuality)
    }
}
