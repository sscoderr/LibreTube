package com.bimilyoncu.sscoderr.libretubess.ui.viewholders

import androidx.recyclerview.widget.RecyclerView
import com.bimilyoncu.sscoderr.libretubess.databinding.AllCaughtUpRowBinding
import com.bimilyoncu.sscoderr.libretubess.databinding.TrendingRowBinding
import com.bimilyoncu.sscoderr.libretubess.databinding.VideoRowBinding

class VideosViewHolder : RecyclerView.ViewHolder {
    var trendingRowBinding: TrendingRowBinding? = null
    var videoRowBinding: VideoRowBinding? = null
    var allCaughtUpBinding: AllCaughtUpRowBinding? = null

    constructor(binding: TrendingRowBinding) : super(binding.root) {
        trendingRowBinding = binding
    }

    constructor(binding: VideoRowBinding) : super(binding.root) {
        videoRowBinding = binding
    }

    constructor(binding: AllCaughtUpRowBinding) : super(binding.root) {
        allCaughtUpBinding = binding
    }
}
