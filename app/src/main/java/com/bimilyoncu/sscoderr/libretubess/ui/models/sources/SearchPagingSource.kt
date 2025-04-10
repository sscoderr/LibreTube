package com.bimilyoncu.sscoderr.libretubess.ui.models.sources

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.bimilyoncu.sscoderr.libretubess.api.MediaServiceRepository
import com.bimilyoncu.sscoderr.libretubess.api.obj.ContentItem
import com.bimilyoncu.sscoderr.libretubess.util.deArrow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SearchPagingSource(
    private val searchQuery: String,
    private val searchFilter: String
) : PagingSource<String, ContentItem>() {
    override fun getRefreshKey(state: PagingState<String, ContentItem>) = null

    override suspend fun load(params: LoadParams<String>): LoadResult<String, ContentItem> {
        return try {
            val result = withContext(Dispatchers.IO) {
                params.key?.let {
                    MediaServiceRepository.instance.getSearchResultsNextPage(
                        searchQuery, searchFilter, it
                    )
                } ?: MediaServiceRepository.instance.getSearchResults(searchQuery, searchFilter)
            }

            LoadResult.Page(result.items.deArrow(), null, result.nextpage)
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }
}
