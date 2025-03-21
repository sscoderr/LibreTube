package com.bimilyoncu.sscoderr.libretube.ui.sheets

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import com.bimilyoncu.sscoderr.libretube.R
import com.bimilyoncu.sscoderr.libretube.databinding.BottomSheetBinding
import com.bimilyoncu.sscoderr.libretube.ui.adapters.IconsSheetAdapter

class IconsBottomSheet : ExpandedBottomSheet(R.layout.bottom_sheet) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val binding = BottomSheetBinding.bind(view)
        binding.optionsRecycler.layoutManager = GridLayoutManager(context, 3)
        binding.optionsRecycler.adapter = IconsSheetAdapter()
    }
}
