package com.bimilyoncu.sscoderr.libretube.ui.fragments

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.distinctUntilChanged
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.map
import androidx.navigation.fragment.findNavController
import com.bimilyoncu.sscoderr.libretube.R
import com.bimilyoncu.sscoderr.libretube.api.MediaServiceRepository
import com.bimilyoncu.sscoderr.libretube.constants.IntentData
import com.bimilyoncu.sscoderr.libretube.constants.PreferenceKeys
import com.bimilyoncu.sscoderr.libretube.databinding.FragmentSearchSuggestionsBinding
import com.bimilyoncu.sscoderr.libretube.db.DatabaseHolder.Database
import com.bimilyoncu.sscoderr.libretube.extensions.TAG
import com.bimilyoncu.sscoderr.libretube.extensions.anyChildFocused
import com.bimilyoncu.sscoderr.libretube.helpers.PreferenceHelper
import com.bimilyoncu.sscoderr.libretube.ui.activities.MainActivity
import com.bimilyoncu.sscoderr.libretube.ui.adapters.SearchHistoryAdapter
import com.bimilyoncu.sscoderr.libretube.ui.adapters.SearchSuggestionsAdapter
import com.bimilyoncu.sscoderr.libretube.ui.extensions.setupFragmentAnimation
import com.bimilyoncu.sscoderr.libretube.ui.models.SearchViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SearchSuggestionsFragment : Fragment(R.layout.fragment_search_suggestions) {
    private var _binding: FragmentSearchSuggestionsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SearchViewModel by activityViewModels()
    private val mainActivity get() = activity as MainActivity

    private val historyAdapter = SearchHistoryAdapter(
        onRootClickListener = { historyQuery ->
            runCatching {
                (activity as MainActivity?)?.searchView
            }.getOrNull()?.setQuery(historyQuery, true)
        },
        onArrowClickListener = { historyQuery ->
            runCatching {
                (activity as MainActivity?)?.searchView
            }.getOrNull()?.setQuery(historyQuery, false)
        }
    )
    private val suggestionsAdapter = SearchSuggestionsAdapter(
        onRootClickListener = { suggestion ->
            (activity as MainActivity?)?.searchView?.setQuery(suggestion, true)
        },
        onArrowClickListener = { suggestion ->
            (activity as MainActivity?)?.searchView?.setQuery(suggestion, false)
        },
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.searchQuery.value = arguments?.getString(IntentData.query)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        _binding = FragmentSearchSuggestionsBinding.bind(view)
        super.onViewCreated(view, savedInstanceState)

        viewModel.searchQuery
            .map { it.isNullOrEmpty() }
            .distinctUntilChanged()
            .observe(viewLifecycleOwner) { isQueryEmpty ->
                if (isQueryEmpty) {
                    binding.suggestionsRecycler.adapter = historyAdapter
                } else if (PreferenceHelper.getBoolean(PreferenceKeys.SEARCH_SUGGESTIONS, true)) {
                    binding.suggestionsRecycler.adapter = suggestionsAdapter
                }
        }

        // waiting for the query to change
        viewModel.searchQuery.observe(viewLifecycleOwner) {
            showData(it)
        }

        setupFragmentAnimation(binding.root) {
            if (mainActivity.searchView.anyChildFocused()) mainActivity.searchView.clearFocus()
            else findNavController().popBackStack()
        }
    }

    private fun showData(query: String?) {
        // fetch search suggestions if enabled or show the search history
        binding.historyEmpty.isGone = true
        binding.suggestionsRecycler.isVisible = true
        if (query.isNullOrEmpty()) {
            showHistory()
        } else if (PreferenceHelper.getBoolean(PreferenceKeys.SEARCH_SUGGESTIONS, true)) {
            fetchSuggestions(query)
        }
    }

    private fun fetchSuggestions(query: String) {
        lifecycleScope.launch {
            val response = try {
                withContext(Dispatchers.IO) {
                    MediaServiceRepository.instance.getSuggestions(query)
                }
            } catch (e: Exception) {
                Log.e(TAG(), e.toString())
                return@launch
            }
            // only load the suggestions if the input field didn't get cleared yet
            if (!viewModel.searchQuery.value.isNullOrEmpty()) {
                suggestionsAdapter.submitList(response.reversed())
            }
        }
    }

    private fun showHistory() {
        lifecycleScope.launch {
            val historyList = withContext(Dispatchers.IO) {
                Database.searchHistoryDao().getAll().map { it.query }
            }
            if (historyList.isNotEmpty()) {
                historyAdapter.submitList(historyList)
            } else {
                binding.suggestionsRecycler.isGone = true
                binding.historyEmpty.isVisible = true
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        _binding = null
    }
}
