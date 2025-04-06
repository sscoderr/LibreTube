package com.bimilyoncu.sscoderr.libretubess.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.bimilyoncu.sscoderr.libretubess.R
import com.bimilyoncu.sscoderr.libretubess.api.PlaylistsHelper
import com.bimilyoncu.sscoderr.libretubess.api.obj.Playlists
import com.bimilyoncu.sscoderr.libretubess.api.obj.StreamItem
import com.bimilyoncu.sscoderr.libretubess.constants.PreferenceKeys
import com.bimilyoncu.sscoderr.libretubess.constants.PreferenceKeys.HOME_TAB_CONTENT
import com.bimilyoncu.sscoderr.libretubess.databinding.FragmentHomeBinding
import com.bimilyoncu.sscoderr.libretubess.db.DatabaseHelper
import com.bimilyoncu.sscoderr.libretubess.db.obj.PlaylistBookmark
import com.bimilyoncu.sscoderr.libretubess.helpers.NavBarHelper
import com.bimilyoncu.sscoderr.libretubess.helpers.PreferenceHelper
import com.bimilyoncu.sscoderr.libretubess.helpers.VersionControlHelper
import com.bimilyoncu.sscoderr.libretubess.ui.activities.SettingsActivity
import com.bimilyoncu.sscoderr.libretubess.ui.adapters.PlaylistBookmarkAdapter
import com.bimilyoncu.sscoderr.libretubess.ui.adapters.PlaylistsAdapter
import com.bimilyoncu.sscoderr.libretubess.ui.adapters.VideosAdapter
import com.bimilyoncu.sscoderr.libretubess.ui.adapters.VideosAdapter.Companion.LayoutMode
import com.bimilyoncu.sscoderr.libretubess.ui.extensions.setupFragmentAnimation
import com.bimilyoncu.sscoderr.libretubess.ui.models.HomeViewModel
import com.bimilyoncu.sscoderr.libretubess.ui.models.SubscriptionsViewModel
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import androidx.lifecycle.lifecycleScope


class HomeFragment : Fragment(R.layout.fragment_home) {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val subscriptionsViewModel: SubscriptionsViewModel by activityViewModels()
    private val homeViewModel: HomeViewModel by activityViewModels()

    private val trendingAdapter = VideosAdapter(forceMode = LayoutMode.TRENDING_ROW)
    private val feedAdapter = VideosAdapter(forceMode = LayoutMode.RELATED_COLUMN)
    private val watchingAdapter = VideosAdapter(forceMode = LayoutMode.RELATED_COLUMN, isContinueWatchingSection = true)
    private val bookmarkAdapter = PlaylistBookmarkAdapter(PlaylistBookmarkAdapter.Companion.BookmarkMode.HOME)
    private val playlistAdapter = PlaylistsAdapter(playlistType = PlaylistsHelper.getPrivatePlaylistType())

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        _binding = FragmentHomeBinding.bind(view)
        super.onViewCreated(view, savedInstanceState)

        binding.trendingRV.adapter = trendingAdapter
        binding.featuredRV.adapter = feedAdapter
        binding.bookmarksRV.adapter = bookmarkAdapter
        binding.playlistsRV.adapter = playlistAdapter
        binding.playlistsRV.adapter?.registerAdapterDataObserver(object :
            RecyclerView.AdapterDataObserver() {
            override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
                super.onItemRangeRemoved(positionStart, itemCount)
                if (itemCount == 0) {
                    binding.playlistsRV.isGone = true
                    binding.playlistsTV.isGone = true
                }
            }
        })
        binding.watchingRV.adapter = watchingAdapter

        // Setup audio-only mode toggle
        setupAudioOnlyModeToggle()

        with(homeViewModel) {
            trending.observe(viewLifecycleOwner, ::showTrending)
            feed.observe(viewLifecycleOwner, ::showFeed)
            bookmarks.observe(viewLifecycleOwner, ::showBookmarks)
            playlists.observe(viewLifecycleOwner, ::showPlaylists)
            continueWatching.observe(viewLifecycleOwner, ::showContinueWatching)
            isLoading.observe(viewLifecycleOwner, ::updateLoading)
        }

        binding.featuredTV.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_subscriptionsFragment)
        }

        binding.watchingTV.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_watchHistoryFragment)
        }

        binding.trendingTV.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_trendsFragment)
        }

        binding.playlistsTV.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_libraryFragment)
        }

        binding.bookmarksTV.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_libraryFragment)
        }

        binding.refresh.setOnRefreshListener {
            binding.refresh.isRefreshing = true
            fetchHomeFeed()
        }

        binding.refreshButton.setOnClickListener {
            fetchHomeFeed()
        }

        binding.changeInstance.setOnClickListener {
            redirectToIntentSettings()
        }

        if (NavBarHelper.getStartFragmentId(requireContext()) != R.id.homeFragment) {
            setupFragmentAnimation(binding.root)
        }
    }

    /**
     * Set up the audio-only mode toggle switch
     */
    private fun setupAudioOnlyModeToggle() {
        // First update the visibility according to version control
        updateAudioModeVisibility()
        
        // Then set up the switch functionality if we're visible
        val cachedStatus = VersionControlHelper.getCachedControlFeaturesStatus()
        if (cachedStatus != false && binding.audioModeContainer.isVisible) {
            setupAudioModeSwitch()
            
            // If we initially had null status, check asynchronously and update
            if (cachedStatus == null) {
                viewLifecycleOwner.lifecycleScope.launch {
                    val shouldShowControls = VersionControlHelper.shouldShowControls(requireContext())
                    Log.d("HomeFragment", "Async shouldShowControls result: $shouldShowControls")
                    if (isAdded && _binding != null) {
                        // Update visibility again with the fresh result
                        updateAudioModeVisibility()
                    }
                }
            }
        }
    }

    /**
     * Set up the switch functionality, separate from visibility control
     */
    private fun setupAudioModeSwitch() {
        // Get the current preference state
        val isAudioOnlyMode = PreferenceHelper.getBoolean(PreferenceKeys.AUDIO_ONLY_MODE, false)
        
        // Set the switch state based on the preference
        binding.audioOnlyModeSwitch.isChecked = isAudioOnlyMode
        
        // Make the whole container clickable to toggle the switch
        binding.audioModeContainer.setOnClickListener {
            binding.audioOnlyModeSwitch.toggle()
        }
        
        // Set up the change listener for the switch
        binding.audioOnlyModeSwitch.setOnCheckedChangeListener { _, isChecked ->
            // Save the preference
            PreferenceHelper.putBoolean(PreferenceKeys.AUDIO_ONLY_MODE, isChecked)
            
            // Show feedback to the user
            val message = if (isChecked) {
                getString(R.string.audio_only_mode_enabled)
            } else {
                getString(R.string.audio_only_mode_disabled)
            }
            
            Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
        }
    }

    override fun onResume() {
        super.onResume()

        // Update audio mode visibility
        updateAudioModeVisibility()
        
        // Only update the switch if the feature is visible
        val cachedStatus = VersionControlHelper.getCachedControlFeaturesStatus()
        if (cachedStatus != false && binding.audioModeContainer.isVisible) {
            setupAudioModeSwitch()
        }

        // Avoid re-fetching when re-entering the screen if it was loaded successfully
        if (homeViewModel.loadedSuccessfully.value == false) {
            fetchHomeFeed()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun fetchHomeFeed() {
        binding.nothingHere.isGone = true
        val defaultItems = resources.getStringArray(R.array.homeTabItemsValues)
        val visibleItems = PreferenceHelper.getStringSet(HOME_TAB_CONTENT, defaultItems.toSet())

        homeViewModel.loadHomeFeed(
            context = requireContext(),
            subscriptionsViewModel = subscriptionsViewModel,
            visibleItems = visibleItems,
            onUnusualLoadTime = ::showChangeInstanceSnackBar
        )
    }

    private fun showTrending(streamItems: List<StreamItem>?) {
        if (streamItems == null) return

        makeVisible(binding.trendingRV, binding.trendingTV)
        trendingAdapter.submitList(streamItems)
    }

    private fun showFeed(streamItems: List<StreamItem>?) {
        if (streamItems == null) return

        makeVisible(binding.featuredRV, binding.featuredTV)
        val hideWatched = PreferenceHelper.getBoolean(PreferenceKeys.HIDE_WATCHED_FROM_FEED, false)
        val feedVideos = streamItems
            .let { DatabaseHelper.filterByStatusAndWatchPosition(it, hideWatched) }
            .take(20)

        feedAdapter.submitList(feedVideos)
    }

    private fun showBookmarks(bookmarks: List<PlaylistBookmark>?) {
        if (bookmarks == null) return

        makeVisible(binding.bookmarksTV, binding.bookmarksRV)
        bookmarkAdapter.submitList(bookmarks)
    }

    private fun showPlaylists(playlists: List<Playlists>?) {
        if (playlists == null) return

        makeVisible(binding.playlistsRV, binding.playlistsTV)
        playlistAdapter.submitList(playlists)
    }

    private fun showContinueWatching(unwatchedVideos: List<StreamItem>?) {
        if (unwatchedVideos == null) return

        makeVisible(binding.watchingRV, binding.watchingTV)
        watchingAdapter.submitList(unwatchedVideos)
    }

    private fun updateLoading(isLoading: Boolean) {
        if (isLoading) {
            showLoading()
        } else {
            hideLoading()
        }
    }

    private fun showLoading() {
        binding.progress.isVisible = !binding.refresh.isRefreshing
        binding.nothingHere.isVisible = false
    }

    private fun hideLoading() {
        binding.progress.isVisible = false
        binding.refresh.isRefreshing = false

        val hasContent = homeViewModel.loadedSuccessfully.value == true
        if (hasContent) {
            showContent()
        } else {
            showNothingHere()
        }
    }

    private fun showNothingHere() {
        binding.nothingHere.isVisible = true
        binding.scroll.isVisible = false
    }

    // Function to make sure both the scroll view and audio container have correct visibility
    private fun updateAudioModeVisibility() {
        // Check cached version control status
        val cachedStatus = VersionControlHelper.getCachedControlFeaturesStatus()
        Log.d("HomeFragment", "Updating audio mode visibility with status: $cachedStatus")
        
        // The scroll view's visibility is managed elsewhere, we don't want to interfere with that
        // But we can check if the parent is visible and then conditionally make our container visible
        if (binding.scroll.isVisible) {
            // If scroll is visible, conditionally show/hide audio based on version control
            binding.audioModeContainer.isVisible = cachedStatus == null || cachedStatus
            Log.d("HomeFragment", "Parent visible, setting audio container visibility: ${binding.audioModeContainer.isVisible}")
        } else {
            Log.d("HomeFragment", "Parent not visible, audio container will remain hidden")
        }
    }
    
    // Called when the home content has loaded successfully - ensure we update our container visibility
    private fun showContent() {
        binding.nothingHere.isVisible = false
        binding.scroll.isVisible = true
        
        // Now that scroll is visible, update audio mode container visibility
        updateAudioModeVisibility()
    }

    private fun showChangeInstanceSnackBar() {
    }

    private fun redirectToIntentSettings() {
        val settingsIntent = Intent(context, SettingsActivity::class.java).apply {
            putExtra(SettingsActivity.REDIRECT_KEY, SettingsActivity.REDIRECT_TO_INTENT_SETTINGS)
        }
        startActivity(settingsIntent)
    }

    private fun makeVisible(vararg views: View) {
        views.forEach { it.isVisible = true }
    }
}
