package com.bimilyoncu.sscoderr.libretubess.ui.models

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.bimilyoncu.sscoderr.libretubess.extensions.updateIfChanged

class CommonPlayerViewModel : ViewModel() {
    val isMiniPlayerVisible = MutableLiveData(false)
    val isFullscreen = MutableLiveData(false)
    var maxSheetHeightPx = 0

    val sheetExpand = MutableLiveData<Boolean?>()

    fun setSheetExpand(state: Boolean?) {
        sheetExpand.updateIfChanged(state)
    }
}
