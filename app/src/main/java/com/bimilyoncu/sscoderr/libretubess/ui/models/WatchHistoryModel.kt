package com.bimilyoncu.sscoderr.libretubess.ui.models

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.bimilyoncu.sscoderr.libretubess.constants.PreferenceKeys
import com.bimilyoncu.sscoderr.libretubess.db.DatabaseHelper
import com.bimilyoncu.sscoderr.libretubess.db.DatabaseHolder
import com.bimilyoncu.sscoderr.libretubess.db.obj.WatchHistoryItem
import com.bimilyoncu.sscoderr.libretubess.helpers.PreferenceHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class WatchHistoryModel : ViewModel() {
    private val watchHistory = MutableLiveData<List<WatchHistoryItem>>()

    private var currentPage = 1
    private var isLoading = false

    private val selectedStatus = MutableStateFlow(
        PreferenceHelper.getInt(PreferenceKeys.SELECTED_HISTORY_STATUS_FILTER, 0)
    )
    private val selectedType = MutableStateFlow(
        PreferenceHelper.getInt(PreferenceKeys.SELECTED_HISTORY_TYPE_FILTER, 0)
    )

    val filteredWatchHistory =
        combine(watchHistory.asFlow(), selectedStatus, selectedType) { history, _, _ -> history }
            .flowOn(Dispatchers.IO).map { history -> history.filter { it.shouldIncludeByFilters() } }
            .asLiveData()

    var selectedStatusFilter
        get() = selectedStatus.value
        set(value) {
            PreferenceHelper.putInt(PreferenceKeys.SELECTED_HISTORY_STATUS_FILTER, value)
            selectedStatus.value = value
        }

    var selectedTypeFilter
        get() = selectedType.value
        set(value) {
            PreferenceHelper.putInt(PreferenceKeys.SELECTED_HISTORY_TYPE_FILTER, value)
            selectedType.value = value
        }

    private suspend fun WatchHistoryItem.shouldIncludeByFilters(): Boolean {
        val isLive = (duration ?: -1L) < 0L
        val matchesFilter = when (selectedTypeFilter) {
            0 -> true
            1 -> !isShort && !isLive
            2 -> isShort // where is the StreamItem converted to watchHistoryItem?
            3 -> isLive
            else -> throw IllegalArgumentException()
        }

        if (!matchesFilter) return false

        // no watch position filter
        if (selectedStatusFilter == 0) return true

        return when (selectedStatusFilter) {
            1 -> com.bimilyoncu.sscoderr.libretubess.db.DatabaseHelper.filterByWatchStatus(this)
            2 -> com.bimilyoncu.sscoderr.libretubess.db.DatabaseHelper.filterByWatchStatus(this, false)
            else -> throw IllegalArgumentException()
        }
    }

    fun fetchNextPage() = viewModelScope.launch(Dispatchers.IO) {
        if (isLoading) return@launch
        isLoading = true

        val newHistory = withContext(Dispatchers.IO) {
            DatabaseHelper.getWatchHistoryPage(currentPage, HISTORY_PAGE_SIZE)
        }

        isLoading = false
        currentPage++

        watchHistory.postValue(
            watchHistory.value.orEmpty().toMutableList().apply {
                addAll(newHistory)
            }
        )
    }

    fun removeFromHistory(watchHistoryItem: WatchHistoryItem) =
        viewModelScope.launch(Dispatchers.IO) {
            DatabaseHolder.Database.watchHistoryDao().delete(watchHistoryItem)

            watchHistory.postValue(
                watchHistory.value.orEmpty().filter { it != watchHistoryItem }
            )
        }

    companion object {
        private const val HISTORY_PAGE_SIZE = 10
    }
}