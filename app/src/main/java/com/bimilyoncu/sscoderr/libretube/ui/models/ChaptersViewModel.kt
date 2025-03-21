package com.bimilyoncu.sscoderr.libretube.ui.models

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.bimilyoncu.sscoderr.libretube.api.obj.ChapterSegment

class ChaptersViewModel: ViewModel() {
    val chaptersLiveData = MutableLiveData<List<ChapterSegment>>()
    val chapters get() = chaptersLiveData.value.orEmpty()
    val currentChapterIndex = MutableLiveData<Int>()
    var maxSheetHeightPx = 0
}