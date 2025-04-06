package com.bimilyoncu.sscoderr.libretubess.ui.sheets

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import com.bimilyoncu.sscoderr.libretubess.constants.IntentData
import com.bimilyoncu.sscoderr.libretubess.databinding.BottomSheetBinding
import com.bimilyoncu.sscoderr.libretubess.helpers.IntentHelper
import com.bimilyoncu.sscoderr.libretubess.ui.adapters.IntentChooserAdapter

class IntentChooserSheet : BaseBottomSheet() {
    private lateinit var url: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        url = arguments?.getString(IntentData.url)!!
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val binding = BottomSheetBinding.bind(view)
        val packages = IntentHelper.getResolveInfo(requireContext(), url)
        binding.optionsRecycler.layoutManager = GridLayoutManager(context, 3)
        binding.optionsRecycler.adapter = IntentChooserAdapter(packages, url)
    }
}
