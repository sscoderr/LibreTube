package com.bimilyoncu.sscoderr.libretubess.ui.fragments

import android.content.res.Configuration
import android.os.Bundle
import android.os.Parcelable
import android.view.View
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.withTransaction
import com.bimilyoncu.sscoderr.libretubess.R
import com.bimilyoncu.sscoderr.libretubess.databinding.FragmentWatchHistoryBinding
import com.bimilyoncu.sscoderr.libretubess.db.DatabaseHolder.Database
import com.bimilyoncu.sscoderr.libretubess.db.obj.WatchHistoryItem
import com.bimilyoncu.sscoderr.libretubess.extensions.ceilHalf
import com.bimilyoncu.sscoderr.libretubess.extensions.dpToPx
import com.bimilyoncu.sscoderr.libretubess.extensions.setOnDismissListener
import com.bimilyoncu.sscoderr.libretubess.helpers.NavBarHelper
import com.bimilyoncu.sscoderr.libretubess.helpers.NavigationHelper
import com.bimilyoncu.sscoderr.libretubess.ui.adapters.WatchHistoryAdapter
import com.bimilyoncu.sscoderr.libretubess.ui.base.DynamicLayoutManagerFragment
import com.bimilyoncu.sscoderr.libretubess.ui.extensions.addOnBottomReachedListener
import com.bimilyoncu.sscoderr.libretubess.ui.extensions.setupFragmentAnimation
import com.bimilyoncu.sscoderr.libretubess.ui.models.CommonPlayerViewModel
import com.bimilyoncu.sscoderr.libretubess.ui.models.WatchHistoryModel
import com.bimilyoncu.sscoderr.libretubess.ui.sheets.BaseBottomSheet
import com.bimilyoncu.sscoderr.libretubess.util.PlayingQueue
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class WatchHistoryFragment : DynamicLayoutManagerFragment(R.layout.fragment_watch_history) {
    private var _binding: FragmentWatchHistoryBinding? = null
    private val binding get() = _binding!!

    private val commonPlayerViewModel: CommonPlayerViewModel by activityViewModels()
    private var recyclerViewState: Parcelable? = null

    private val viewModel: WatchHistoryModel by viewModels()
    private val watchHistoryAdapter = WatchHistoryAdapter()

    override fun setLayoutManagers(gridItems: Int) {
        _binding?.watchHistoryRecView?.layoutManager =
            GridLayoutManager(context, gridItems.ceilHalf())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        _binding = FragmentWatchHistoryBinding.bind(view)
        super.onViewCreated(view, savedInstanceState)

        commonPlayerViewModel.isMiniPlayerVisible.observe(viewLifecycleOwner) {
            _binding?.watchHistoryRecView?.updatePadding(bottom = if (it) 64f.dpToPx() else 0)
        }

        binding.watchHistoryRecView.setOnDismissListener { position ->
            val item = viewModel.filteredWatchHistory.value?.getOrNull(position) ?: return@setOnDismissListener
            viewModel.removeFromHistory(item)
        }

        // observe changes to indicate if the history is empty
        watchHistoryAdapter.registerAdapterDataObserver(object :
            RecyclerView.AdapterDataObserver() {
            override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
                if (watchHistoryAdapter.itemCount == 0) {
                    binding.historyContainer.isGone = true
                    binding.historyEmpty.isVisible = true
                }
            }
        })

        binding.watchHistoryRecView.adapter = watchHistoryAdapter

        // manually restore the recyclerview state due to https://github.com/material-components/material-components-android/issues/3473
        binding.watchHistoryRecView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                recyclerViewState = binding.watchHistoryRecView.layoutManager?.onSaveInstanceState()
            }
        })

        binding.filterTypeTV.text =
            resources.getStringArray(R.array.filterOptions)[viewModel.selectedTypeFilter]
        binding.filterStatusTV.text =
            resources.getStringArray(R.array.filterStatusOptions)[viewModel.selectedStatusFilter]

        val watchPositionItem = arrayOf(getString(R.string.also_clear_watch_positions))
        val selected = booleanArrayOf(false)

        binding.clear.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.clear_history)
                .setMultiChoiceItems(watchPositionItem, selected) { _, index, newValue ->
                    selected[index] = newValue
                }
                .setPositiveButton(R.string.okay) { _, _ ->
                    binding.historyContainer.isGone = true
                    binding.historyEmpty.isVisible = true
                    lifecycleScope.launch(Dispatchers.IO) {
                        Database.withTransaction {
                            Database.watchHistoryDao().deleteAll()
                            if (selected[0]) Database.watchPositionDao().deleteAll()
                        }
                    }
                }
                .setNegativeButton(R.string.cancel, null)
                .show()
        }

        binding.filterTypeTV.setOnClickListener {
            val filterOptions = resources.getStringArray(R.array.filterOptions)

            BaseBottomSheet().apply {
                setSimpleItems(filterOptions.toList()) { index ->
                    binding.filterTypeTV.text = filterOptions[index]
                    viewModel.selectedTypeFilter = index
                }
            }.show(childFragmentManager)
        }

        binding.filterStatusTV.setOnClickListener {
            val filterOptions = resources.getStringArray(R.array.filterStatusOptions)

            BaseBottomSheet().apply {
                setSimpleItems(filterOptions.toList()) { index ->
                    binding.filterStatusTV.text = filterOptions[index]
                    viewModel.selectedStatusFilter = index
                }
            }.show(childFragmentManager)
        }

        binding.playAll.setOnClickListener {
            val history = viewModel.filteredWatchHistory.value.orEmpty()
            if (history.isEmpty()) return@setOnClickListener

            PlayingQueue.add(
                *history.reversed().map(WatchHistoryItem::toStreamItem).toTypedArray()
            )
            NavigationHelper.navigateVideo(
                requireContext(),
                history.last().videoId,
                keepQueue = true,
                resumeFromSavedPosition = false
            )
        }

        viewModel.filteredWatchHistory.observe(viewLifecycleOwner) { history ->
            binding.historyEmpty.isGone = history.isNotEmpty()
            binding.historyContainer.isVisible = history.isNotEmpty()

            watchHistoryAdapter.submitList(history)
        }

        viewModel.fetchNextPage()

        binding.watchHistoryRecView.addOnBottomReachedListener {
            viewModel.fetchNextPage()
        }

        if (NavBarHelper.getStartFragmentId(requireContext()) != R.id.watchHistoryFragment) {
            setupFragmentAnimation(binding.root)
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        // manually restore the recyclerview state due to https://github.com/material-components/material-components-android/issues/3473
        binding.watchHistoryRecView.layoutManager?.onRestoreInstanceState(recyclerViewState)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
