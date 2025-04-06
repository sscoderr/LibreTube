package com.bimilyoncu.sscoderr.libretubess.ui.models

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SearchViewModel : ViewModel() {
    val searchQuery = MutableLiveData<String>()
    fun setQuery(query: String?) {
        this.searchQuery.value = query
    }
}
