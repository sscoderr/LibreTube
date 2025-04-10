package com.bimilyoncu.sscoderr.libretubess.ui.fragments

import android.annotation.SuppressLint
import android.content.res.Configuration
import android.os.Bundle
import android.os.Parcelable
import android.view.View
import android.view.ViewGroup.MarginLayoutParams
import androidx.core.os.bundleOf
import androidx.core.view.children
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bimilyoncu.sscoderr.libretubess.R
import com.bimilyoncu.sscoderr.libretubess.api.obj.StreamItem
import com.bimilyoncu.sscoderr.libretubess.api.obj.Subscription
import com.bimilyoncu.sscoderr.libretubess.constants.IntentData
import com.bimilyoncu.sscoderr.libretubess.constants.PreferenceKeys
import com.bimilyoncu.sscoderr.libretubess.databinding.FragmentSubscriptionsBinding
import com.bimilyoncu.sscoderr.libretubess.db.DatabaseHelper
import com.bimilyoncu.sscoderr.libretubess.db.DatabaseHolder
import com.bimilyoncu.sscoderr.libretubess.extensions.dpToPx
import com.bimilyoncu.sscoderr.libretubess.extensions.formatShort
import com.bimilyoncu.sscoderr.libretubess.extensions.toID
import com.bimilyoncu.sscoderr.libretubess.helpers.NavBarHelper
import com.bimilyoncu.sscoderr.libretubess.helpers.NavigationHelper
import com.bimilyoncu.sscoderr.libretubess.helpers.PreferenceHelper
import com.bimilyoncu.sscoderr.libretubess.obj.SelectableOption
import com.bimilyoncu.sscoderr.libretubess.ui.adapters.LegacySubscriptionAdapter
import com.bimilyoncu.sscoderr.libretubess.ui.adapters.SubscriptionChannelAdapter
import com.bimilyoncu.sscoderr.libretubess.ui.adapters.VideosAdapter
import com.bimilyoncu.sscoderr.libretubess.ui.base.DynamicLayoutManagerFragment
import com.bimilyoncu.sscoderr.libretubess.ui.extensions.addOnBottomReachedListener
import com.bimilyoncu.sscoderr.libretubess.ui.extensions.setupFragmentAnimation
import com.bimilyoncu.sscoderr.libretubess.ui.models.CommonPlayerViewModel
import com.bimilyoncu.sscoderr.libretubess.ui.models.EditChannelGroupsModel
import com.bimilyoncu.sscoderr.libretubess.ui.models.SubscriptionsViewModel
import com.bimilyoncu.sscoderr.libretubess.ui.sheets.ChannelGroupsSheet
import com.bimilyoncu.sscoderr.libretubess.ui.sheets.FilterSortBottomSheet
import com.bimilyoncu.sscoderr.libretubess.ui.sheets.FilterSortBottomSheet.Companion.FILTER_SORT_REQUEST_KEY
import com.bimilyoncu.sscoderr.libretubess.util.PlayingQueue
import com.bimilyoncu.sscoderr.libretubess.util.deArrow
import com.google.android.material.chip.Chip
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SubscriptionsFragment : DynamicLayoutManagerFragment(R.layout.fragment_subscriptions) {
    private var _binding: FragmentSubscriptionsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SubscriptionsViewModel by activityViewModels()
    private val playerModel: CommonPlayerViewModel by activityViewModels()
    private val channelGroupsModel: EditChannelGroupsModel by activityViewModels()
    private var selectedFilterGroup
        set(value) = PreferenceHelper.putInt(PreferenceKeys.SELECTED_CHANNEL_GROUP, value)
        get() = PreferenceHelper.getInt(PreferenceKeys.SELECTED_CHANNEL_GROUP, 0)

    private var isCurrentTabSubChannels = false
    private var isAppBarFullyExpanded = true

    private var feedAdapter = VideosAdapter()
    private var selectedSortOrder = PreferenceHelper.getInt(PreferenceKeys.FEED_SORT_ORDER, 0)
        set(value) {
            PreferenceHelper.putInt(PreferenceKeys.FEED_SORT_ORDER, value)
            field = value
        }

    private var hideWatched =
        PreferenceHelper.getBoolean(PreferenceKeys.HIDE_WATCHED_FROM_FEED, false)
        set(value) {
            PreferenceHelper.putBoolean(PreferenceKeys.HIDE_WATCHED_FROM_FEED, value)
            field = value
        }

    private var showUpcoming =
        PreferenceHelper.getBoolean(PreferenceKeys.SHOW_UPCOMING_IN_FEED, true)
        set(value) {
            PreferenceHelper.putBoolean(PreferenceKeys.SHOW_UPCOMING_IN_FEED, value)
            field = value
        }

    private var subChannelsRecyclerViewState: Parcelable? = null
    private var subFeedRecyclerViewState: Parcelable? = null

    private val legacySubscriptionsAdapter = LegacySubscriptionAdapter()
    private val channelsAdapter = SubscriptionChannelAdapter()

    override fun setLayoutManagers(gridItems: Int) {
        _binding?.subFeed?.layoutManager = VideosAdapter.getLayout(requireContext(), gridItems)
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        _binding = FragmentSubscriptionsBinding.bind(view)
        super.onViewCreated(view, savedInstanceState)

        setupSortAndFilter()

        binding.subFeed.adapter = feedAdapter

        val legacySubscriptions = PreferenceHelper.getBoolean(
            PreferenceKeys.LEGACY_SUBSCRIPTIONS,
            false
        )

        if (legacySubscriptions) {
            binding.subChannels.layoutManager = GridLayoutManager(
                context,
                PreferenceHelper.getString(
                    PreferenceKeys.LEGACY_SUBSCRIPTIONS_COLUMNS,
                    "4"
                ).toInt()
            )
            binding.subChannels.adapter = legacySubscriptionsAdapter
        } else {
            binding.subChannels.layoutManager = LinearLayoutManager(context)
            binding.subChannels.adapter = channelsAdapter
        }

        // Check if the AppBarLayout is fully expanded
        binding.subscriptionsAppBar.addOnOffsetChangedListener { _, verticalOffset ->
            isAppBarFullyExpanded = verticalOffset == 0
        }

        // Determine if the child can scroll up
        binding.subRefresh.setOnChildScrollUpCallback { _, _ ->
            !isAppBarFullyExpanded
        }

        binding.subRefresh.isEnabled = true
        binding.subProgress.isVisible = true

        if (viewModel.videoFeed.value == null) {
            viewModel.fetchFeed(requireContext(), forceRefresh = false)
        }
        if (viewModel.subscriptions.value == null) {
            viewModel.fetchSubscriptions(requireContext())
        }

        viewModel.videoFeed.observe(viewLifecycleOwner) {
            if (!isCurrentTabSubChannels && it != null) showFeed()
        }

        viewModel.subscriptions.observe(viewLifecycleOwner) {
            if (isCurrentTabSubChannels && it != null) showSubscriptions()
        }

        viewModel.feedProgress.observe(viewLifecycleOwner) { progress ->
            if (progress == null || progress.currentProgress == progress.total) {
                binding.feedProgressContainer.isGone = true
            } else {
                binding.feedProgressContainer.isVisible = true
                binding.feedProgressText.text = "${progress.currentProgress}/${progress.total}"
                binding.feedProgressBar.max = progress.total
                binding.feedProgressBar.progress = progress.currentProgress
            }
        }

        binding.subRefresh.setOnRefreshListener {
            viewModel.fetchSubscriptions(requireContext())
            viewModel.fetchFeed(requireContext(), forceRefresh = true)
        }

        binding.toggleSubs.isVisible = true

        binding.toggleSubs.setOnClickListener {
            binding.subProgress.isVisible = true
            binding.subRefresh.isRefreshing = true
            isCurrentTabSubChannels = !isCurrentTabSubChannels

            if (isCurrentTabSubChannels) showSubscriptions() else showFeed()

            binding.subChannels.isVisible = isCurrentTabSubChannels
            binding.subFeed.isGone = isCurrentTabSubChannels
        }

        binding.subChannels.addOnBottomReachedListener {
            val binding = _binding ?: return@addOnBottomReachedListener

            if (viewModel.subscriptions.value != null && isCurrentTabSubChannels) {
                binding.subRefresh.isRefreshing = true
                binding.subRefresh.isRefreshing = false
            }
        }

        // add some extra margin to the subscribed channels while the mini player is visible
        // otherwise the last channel would be invisible
        playerModel.isMiniPlayerVisible.observe(viewLifecycleOwner) {
            binding.subChannels.updateLayoutParams<MarginLayoutParams> {
                bottomMargin = (if (it) 64f else 0f).dpToPx()
            }
        }

        binding.channelGroups.setOnCheckedStateChangeListener { group, _ ->
            selectedFilterGroup = group.children.indexOfFirst { it.id == group.checkedChipId }
            if (isCurrentTabSubChannels) showSubscriptions() else showFeed()
        }

        channelGroupsModel.groups.observe(viewLifecycleOwner) {
            lifecycleScope.launch { initChannelGroups() }
        }

        binding.editGroups.setOnClickListener {
            ChannelGroupsSheet().show(childFragmentManager, null)
        }

        // manually restore the recyclerview state due to https://github.com/material-components/material-components-android/issues/3473
        binding.subChannels.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                subChannelsRecyclerViewState = binding.subChannels.layoutManager?.onSaveInstanceState()
            }
        })

        binding.subFeed.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                subFeedRecyclerViewState = binding.subFeed.layoutManager?.onSaveInstanceState()
            }
        })

        lifecycleScope.launch(Dispatchers.IO) {
            val groups = DatabaseHolder.Database.subscriptionGroupsDao().getAll()
                .sortedBy { it.index }
            channelGroupsModel.groups.postValue(groups)
        }

        if (NavBarHelper.getStartFragmentId(requireContext()) != R.id.subscriptionsFragment) {
            setupFragmentAnimation(binding.root)
        }
    }

    private fun loadFeedItems(sortedFeed: List<StreamItem>) {
        val binding = _binding ?: return

        if (viewModel.videoFeed.value != null && !isCurrentTabSubChannels && !binding.subRefresh.isRefreshing) {
            binding.subRefresh.isRefreshing = true

            lifecycleScope.launch {
                val streamItemsToInsert = sortedFeed.let {
                    withContext(Dispatchers.IO) {
                        runCatching { it.deArrow() }.getOrDefault(it)
                    }
                }

                feedAdapter.submitList(streamItemsToInsert) {
                    binding.subFeed.scrollToPosition(0)
                }
                binding.subRefresh.isRefreshing = false
            }
        }
    }

    private fun setupSortAndFilter() {
        binding.filterSort.setOnClickListener {
            childFragmentManager.setFragmentResultListener(
                FILTER_SORT_REQUEST_KEY,
                viewLifecycleOwner
            ) { _, resultBundle ->
                selectedSortOrder = resultBundle.getInt(IntentData.sortOptions)
                hideWatched = resultBundle.getBoolean(IntentData.hideWatched)
                showUpcoming = resultBundle.getBoolean(IntentData.showUpcoming)
                showFeed()
            }

            FilterSortBottomSheet()
                .apply {
                    arguments = bundleOf(
                        IntentData.sortOptions to fetchSortOptions(),
                        IntentData.hideWatched to hideWatched,
                        IntentData.showUpcoming to showUpcoming,
                    )
                }
                .show(childFragmentManager)
        }
    }

    private fun fetchSortOptions(): List<SelectableOption> {
        return resources.getStringArray(R.array.sortOptions)
            .mapIndexed { index, option ->
                SelectableOption(isSelected = index == selectedSortOrder, name = option)
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun playByGroup(groupIndex: Int) {
        val streams = viewModel.videoFeed.value.orEmpty()
            .filterByGroup(groupIndex)
            .let { DatabaseHelper.filterByStatusAndWatchPosition(it, hideWatched) }
            .sortedBySelectedOrder()

        if (streams.isEmpty()) return

        PlayingQueue.clear()
        PlayingQueue.add(*streams.toTypedArray())

        NavigationHelper.navigateVideo(
            requireContext(),
            videoUrlOrId = streams.first().url,
            keepQueue = true,
            resumeFromSavedPosition = false
        )
    }

    @SuppressLint("InflateParams")
    private fun initChannelGroups() {
        val binding = _binding ?: return

        binding.chipAll.isChecked = selectedFilterGroup == 0
        binding.chipAll.setOnLongClickListener {
            playByGroup(0)
            true
        }

        binding.channelGroups.removeAllViews()
        binding.channelGroups.addView(binding.chipAll)

        channelGroupsModel.groups.value?.forEachIndexed { index, group ->
            val chip = layoutInflater.inflate(R.layout.filter_chip, null) as Chip
            chip.apply {
                id = View.generateViewId()
                isCheckable = true
                text = group.name
                setOnLongClickListener {
                    // the index must be increased by one to skip the "all channels" group button
                    playByGroup(index + 1)
                    true
                }
            }

            binding.channelGroups.addView(chip)

            if (index + 1 == selectedFilterGroup) binding.channelGroups.check(chip.id)
        }
    }

    private fun List<StreamItem>.filterByGroup(groupIndex: Int): List<StreamItem> {
        if (groupIndex == 0) return this

        val group = channelGroupsModel.groups.value?.getOrNull(groupIndex - 1)
        return filter {
            val channelId = it.uploaderUrl.orEmpty().toID()
            group?.channels?.contains(channelId) != false
        }
    }

    @JvmName("filterSubsByGroup")
    private fun List<Subscription>.filterByGroup(groupIndex: Int): List<Subscription> {
        if (groupIndex == 0) return this

        val group = channelGroupsModel.groups.value?.getOrNull(groupIndex - 1)
        return filter { group?.channels?.contains(it.url.toID()) != false }
    }

    private fun List<StreamItem>.sortedBySelectedOrder() = when (selectedSortOrder) {
        0 -> this
        1 -> this.reversed()
        2 -> this.sortedBy { it.views }.reversed()
        3 -> this.sortedBy { it.views }
        4 -> this.sortedBy { it.uploaderName }
        5 -> this.sortedBy { it.uploaderName }.reversed()
        else -> this
    }

    private fun showFeed() {
        val videoFeed = viewModel.videoFeed.value ?: return

        binding.subRefresh.isRefreshing = false
        val feed = videoFeed
            .filterByGroup(selectedFilterGroup)
            .filter { showUpcoming || !it.isUpcoming }
            .let {
                DatabaseHelper.filterByStatusAndWatchPosition(it, hideWatched)
            }

        val sorted = feed
            .sortedBySelectedOrder()
            .toMutableList()

        // add an "all caught up item"
        if (selectedSortOrder == 0) {
            val lastCheckedFeedTime = PreferenceHelper.getLastCheckedFeedTime()
            val caughtUpIndex = feed.indexOfFirst { it.uploaded <= lastCheckedFeedTime && !it.isUpcoming }
            if (caughtUpIndex > 0 && !feed[caughtUpIndex-1].isUpcoming) {
                sorted.add(
                    caughtUpIndex,
                    StreamItem(type = VideosAdapter.CAUGHT_UP_STREAM_TYPE)
                )
            }
        }

        binding.subChannels.isGone = true
        binding.subProgress.isGone = true

        val notLoaded = viewModel.videoFeed.value.isNullOrEmpty()
        binding.subFeed.isGone = notLoaded
        binding.emptyFeed.isVisible = notLoaded
        loadFeedItems(sorted)

        binding.toggleSubs.text = getString(R.string.subscriptions)

        feed.firstOrNull { !it.isUpcoming }?.uploaded?.let {
            PreferenceHelper.setLastFeedWatchedTime(it)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun showSubscriptions() {
        val subscriptions = viewModel.subscriptions.value?.filterByGroup(selectedFilterGroup) ?: return

        val legacySubscriptions = PreferenceHelper.getBoolean(
            PreferenceKeys.LEGACY_SUBSCRIPTIONS,
            false
        )

        if (legacySubscriptions) {
            legacySubscriptionsAdapter.submitList(subscriptions) {
                binding.subFeed.scrollToPosition(0)
            }
        } else {
            channelsAdapter.submitList(subscriptions) {
                binding.subFeed.scrollToPosition(0)
            }
        }

        binding.subRefresh.isRefreshing = false
        binding.subProgress.isGone = true
        binding.subFeed.isGone = true

        val notLoaded = viewModel.subscriptions.value.isNullOrEmpty()
        binding.subChannels.isGone = notLoaded
        binding.emptyFeed.isVisible = notLoaded

        val subCount = subscriptions.size.toLong().formatShort()
        binding.toggleSubs.text = "${getString(R.string.subscriptions)} ($subCount)"
    }

    fun removeItem(videoId: String) {
        feedAdapter.removeItemById(videoId)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        // manually restore the recyclerview state due to https://github.com/material-components/material-components-android/issues/3473
        binding.subChannels.layoutManager?.onRestoreInstanceState(subChannelsRecyclerViewState)
        binding.subFeed.layoutManager?.onRestoreInstanceState(subFeedRecyclerViewState)
    }
}
