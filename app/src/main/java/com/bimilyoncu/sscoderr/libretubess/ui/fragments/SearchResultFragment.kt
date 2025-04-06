package com.bimilyoncu.sscoderr.libretubess.ui.fragments

import android.content.res.Configuration
import android.os.Bundle
import android.os.Parcelable
import android.view.View
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.paging.LoadState
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bimilyoncu.sscoderr.libretubess.R
import com.bimilyoncu.sscoderr.libretubess.constants.PreferenceKeys
import com.bimilyoncu.sscoderr.libretubess.databinding.FragmentSearchResultBinding
import com.bimilyoncu.sscoderr.libretubess.db.DatabaseHelper
import com.bimilyoncu.sscoderr.libretubess.db.obj.SearchHistoryItem
import com.bimilyoncu.sscoderr.libretubess.extensions.ceilHalf
import com.bimilyoncu.sscoderr.libretubess.helpers.PreferenceHelper
import com.bimilyoncu.sscoderr.libretubess.ui.activities.MainActivity
import com.bimilyoncu.sscoderr.libretubess.ui.adapters.SearchResultsAdapter
import com.bimilyoncu.sscoderr.libretubess.ui.base.DynamicLayoutManagerFragment
import com.bimilyoncu.sscoderr.libretubess.ui.extensions.setupFragmentAnimation
import com.bimilyoncu.sscoderr.libretubess.ui.models.SearchResultViewModel
import com.bimilyoncu.sscoderr.libretubess.util.TextUtils.toTimeInSeconds
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull

class SearchResultFragment : DynamicLayoutManagerFragment(R.layout.fragment_search_result) {
    private var _binding: FragmentSearchResultBinding? = null
    private val binding get() = _binding!!
    private val args by navArgs<SearchResultFragmentArgs>()
    private val viewModel by viewModels<SearchResultViewModel>()

    private var recyclerViewState: Parcelable? = null

    override fun setLayoutManagers(gridItems: Int) {
        _binding?.searchRecycler?.layoutManager = GridLayoutManager(context, gridItems.ceilHalf())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        _binding = FragmentSearchResultBinding.bind(view)
        super.onViewCreated(view, savedInstanceState)

        // fixes a bug that the search query will stay the old one when searching for multiple
        // different queries in a row and navigating to the previous ones through back presses
        (context as MainActivity).setQuerySilent(args.query)

        // add the query to the history
        addToHistory(args.query)

        // filter options
        binding.filterChipGroup.setOnCheckedStateChangeListener { _, _ ->
            viewModel.setFilter(
                when (
                    binding.filterChipGroup.checkedChipId
                ) {
                    R.id.chip_all -> "all"
                    R.id.chip_videos -> "videos"
                    R.id.chip_channels -> "channels"
                    R.id.chip_playlists -> "playlists"
                    R.id.chip_music_songs -> "music_songs"
                    R.id.chip_music_videos -> "music_videos"
                    R.id.chip_music_albums -> "music_albums"
                    R.id.chip_music_playlists -> "music_playlists"
                    R.id.chip_music_artists -> "music_artists"
                    else -> throw IllegalArgumentException("Filter out of range")
                }
            )
        }

        val timeStamp = args.query.toHttpUrlOrNull()?.queryParameter("t")?.toTimeInSeconds()
        val searchResultsAdapter = SearchResultsAdapter(timeStamp ?: 0)
        binding.searchRecycler.adapter = searchResultsAdapter

        binding.searchRecycler.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                recyclerViewState = binding.searchRecycler.layoutManager?.onSaveInstanceState()
            }
        })

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                searchResultsAdapter.loadStateFlow.collect {
                    val isLoading = it.source.refresh is LoadState.Loading
                    binding.progress.isVisible = isLoading
                    binding.searchResultsLayout.isGone = isLoading
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.searchResultsFlow.collectLatest {
                    searchResultsAdapter.submitData(it)
                }
            }
        }

        setupFragmentAnimation(binding.root) {
            findNavController().popBackStack(R.id.searchFragment, true) ||
                    findNavController().popBackStack()
        }
    }

    private fun addToHistory(query: String) {
        val searchHistoryEnabled =
            PreferenceHelper.getBoolean(PreferenceKeys.SEARCH_HISTORY_TOGGLE, true)
        if (searchHistoryEnabled && query.isNotEmpty()) {
            lifecycleScope.launch(Dispatchers.IO) {
                DatabaseHelper.addToSearchHistory(SearchHistoryItem(query.trim()))
            }
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        // manually restore the recyclerview state due to https://github.com/material-components/material-components-android/issues/3473
        binding.searchRecycler.layoutManager?.onRestoreInstanceState(recyclerViewState)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
