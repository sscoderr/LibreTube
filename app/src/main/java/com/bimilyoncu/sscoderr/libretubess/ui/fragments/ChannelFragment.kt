package com.bimilyoncu.sscoderr.libretubess.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.os.bundleOf
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.bimilyoncu.sscoderr.libretubess.R
import com.bimilyoncu.sscoderr.libretubess.api.MediaServiceRepository
import com.bimilyoncu.sscoderr.libretubess.api.obj.ChannelTab
import com.bimilyoncu.sscoderr.libretubess.api.obj.StreamItem
import com.bimilyoncu.sscoderr.libretubess.constants.IntentData
import com.bimilyoncu.sscoderr.libretubess.databinding.FragmentChannelBinding
import com.bimilyoncu.sscoderr.libretubess.enums.ShareObjectType
import com.bimilyoncu.sscoderr.libretubess.extensions.TAG
import com.bimilyoncu.sscoderr.libretubess.extensions.formatShort
import com.bimilyoncu.sscoderr.libretubess.extensions.toID
import com.bimilyoncu.sscoderr.libretubess.helpers.ClipboardHelper
import com.bimilyoncu.sscoderr.libretubess.helpers.ImageHelper
import com.bimilyoncu.sscoderr.libretubess.helpers.NavigationHelper
import com.bimilyoncu.sscoderr.libretubess.helpers.VersionControlHelper
import com.bimilyoncu.sscoderr.libretubess.obj.ShareData
import com.bimilyoncu.sscoderr.libretubess.ui.adapters.VideosAdapter
import com.bimilyoncu.sscoderr.libretubess.ui.base.DynamicLayoutManagerFragment
import com.bimilyoncu.sscoderr.libretubess.ui.dialogs.ShareDialog
import com.bimilyoncu.sscoderr.libretubess.ui.extensions.setupFragmentAnimation
import com.bimilyoncu.sscoderr.libretubess.ui.extensions.setupSubscriptionButton
import com.bimilyoncu.sscoderr.libretubess.ui.sheets.AddChannelToGroupSheet
import com.bimilyoncu.sscoderr.libretubess.util.deArrow
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException

class ChannelFragment : DynamicLayoutManagerFragment(R.layout.fragment_channel) {
    private var _binding: FragmentChannelBinding? = null
    private val binding get() = _binding!!
    private val args by navArgs<ChannelFragmentArgs>()

    private var channelId: String? = null
    private var channelName: String? = null
    private var channelAdapter: VideosAdapter? = null
    private var isLoading = true

    private lateinit var channelContentAdapter: ChannelContentAdapter

    private var nextPages = Array<String?>(5) { null }
    private var isAppBarFullyExpanded: Boolean = true
    private val tabList = mutableListOf<ChannelTab>()

    private val tabNamesMap = mapOf(
        VIDEOS_TAB_KEY to R.string.videos,
        "shorts" to R.string.yt_shorts,
        "livestreams" to R.string.livestreams,
        "playlists" to R.string.playlists,
        "albums" to R.string.albums
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        channelName = args.channelName
            ?.replace("/c/", "")
            ?.replace("/user/", "")
        channelId = args.channelId
    }

    override fun setLayoutManagers(gridItems: Int) {}

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        _binding = FragmentChannelBinding.bind(view)
        super.onViewCreated(view, savedInstanceState)
        // Check if the AppBarLayout is fully expanded
        binding.channelAppBar.addOnOffsetChangedListener { _, verticalOffset ->
            isAppBarFullyExpanded = verticalOffset == 0
        }

        binding.pager.reduceDragSensitivity()

        // Determine if the child can scroll up
        binding.channelRefresh.setOnChildScrollUpCallback { _, _ ->
            !isAppBarFullyExpanded
        }

        binding.channelRefresh.setOnRefreshListener {
            fetchChannel()
        }

        fetchChannel()

        setupFragmentAnimation(binding.root)
    }

    // adjust sensitivity due to the issue of viewpager2 with SwipeToRefresh https://issuetracker.google.com/issues/138314213
    private fun ViewPager2.reduceDragSensitivity() {
        val recyclerViewField = ViewPager2::class.java.getDeclaredField("mRecyclerView")
        recyclerViewField.isAccessible = true
        val recyclerView = recyclerViewField.get(this) as RecyclerView

        val touchSlopField = RecyclerView::class.java.getDeclaredField("mTouchSlop")
        touchSlopField.isAccessible = true
        val touchSlop = touchSlopField.get(recyclerView) as Int
        touchSlopField.set(recyclerView, touchSlop * 3)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun fetchChannel() = lifecycleScope.launch {
        isLoading = true
        _binding?.channelRefresh?.isRefreshing = true

        val response = try {
            withContext(Dispatchers.IO) {
                if (channelId != null) {
                    MediaServiceRepository.instance.getChannel(channelId!!)
                } else {
                    MediaServiceRepository.instance.getChannelByName(channelName!!)
                }.apply {
                    relatedStreams = relatedStreams.deArrow()
                }
            }
        } catch (e: IOException) {
            Log.e(TAG(), "IOException, you might not have internet connection")
            return@launch
        } catch (e: HttpException) {
            Log.e(TAG(), "HttpException, unexpected response")
            return@launch
        } finally {
            _binding?.channelRefresh?.isRefreshing = false
            isLoading = false
        }
        val binding = _binding ?: return@launch

        // needed if the channel gets loaded by the ID
        channelId = response.id
        channelName = response.name
        val shareData = ShareData(currentChannel = response.name)

        val channelId = channelId ?: return@launch

        binding.channelSubscribe.setupSubscriptionButton(
            channelId,
            response.name.orEmpty(),
            response.avatarUrl,
            response.verified,
            binding.notificationBell
        ) { isSubscribed ->
            _binding?.addToGroup?.isVisible = isSubscribed
        }

        // Apply version control to share button visibility
        val cachedStatus = VersionControlHelper.getCachedControlFeaturesStatus()
        binding.channelShare.isVisible = cachedStatus == null || cachedStatus
        
        // Only setup the click listener if the button is visible
        if (binding.channelShare.isVisible) {
            binding.channelShare.setOnClickListener {
                // Create channel share URL
                val channelUrl = "${ShareDialog.YOUTUBE_FRONTEND_URL}/channel/${channelId.toID()}"
                
                // Create and launch system share intent directly
                val intent = Intent(Intent.ACTION_SEND)
                    .putExtra(Intent.EXTRA_TEXT, channelUrl)
                    .putExtra(Intent.EXTRA_SUBJECT, response.name)
                    .setType("text/plain")
                val shareIntent = Intent.createChooser(intent, getString(R.string.shareTo))
                requireContext().startActivity(shareIntent)
            }
        }

        // If we haven't determined control features status yet, check asynchronously
        if (cachedStatus == null) {
            viewLifecycleOwner.lifecycleScope.launch {
                val shouldShowControls = VersionControlHelper.shouldShowControls(requireContext())
                Log.d(TAG(), "Async shouldShowControls result for share button: $shouldShowControls")
                if (isAdded && _binding != null) {
                    // Update visibility with the fresh result
                    binding.channelShare.isVisible = shouldShowControls
                }
            }
        }

        binding.addToGroup.setOnClickListener {
            AddChannelToGroupSheet().apply {
                arguments = bundleOf(IntentData.channelId to channelId)
            }.show(childFragmentManager)
        }

        binding.playAll.setOnClickListener {
            val firstVideoId =
                response.relatedStreams.firstOrNull()?.url?.toID() ?: return@setOnClickListener

            NavigationHelper.navigateVideo(requireContext(), firstVideoId, channelId = channelId)
        }

        nextPages[0] = response.nextpage
        isLoading = false
        binding.channelRefresh.isRefreshing = false

        binding.channelCoordinator.isVisible = true

        binding.channelName.text = response.name
        binding.channelName.setOnLongClickListener {
            ClipboardHelper.save(requireContext(), text = response.name.orEmpty())
            true
        }

        if (response.verified) {
            binding.channelName
                .setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_verified, 0)
        }
        binding.channelSubs.text = resources.getString(
            R.string.subscribers,
            response.subscriberCount.formatShort()
        )
        if (response.description.orEmpty().isBlank()) {
            binding.channelDescription.isGone = true
        } else {
            binding.channelDescription.text = response.description.orEmpty().trim()
        }

        ImageHelper.loadImage(response.bannerUrl, binding.channelBanner)
        ImageHelper.loadImage(response.avatarUrl, binding.channelImage, true)

        binding.channelImage.setOnClickListener {
            NavigationHelper.openImagePreview(
                requireContext(),
                response.avatarUrl ?: return@setOnClickListener
            )
        }

        binding.channelBanner.setOnClickListener {
            NavigationHelper.openImagePreview(
                requireContext(),
                response.bannerUrl ?: return@setOnClickListener
            )
        }

        channelContentAdapter = ChannelContentAdapter(
            tabList,
            response.relatedStreams,
            response.nextpage,
            channelId,
            this@ChannelFragment
        )
        binding.pager.adapter = channelContentAdapter
        TabLayoutMediator(binding.tabParent, binding.pager) { tab, position ->
            tab.text = tabList[position].name
        }.attach()

        channelAdapter = VideosAdapter(
            forceMode = VideosAdapter.Companion.LayoutMode.CHANNEL_ROW
        ).also {
            it.submitList(response.relatedStreams)
        }
        tabList.clear()

        val tabs = listOf(ChannelTab(VIDEOS_TAB_KEY, "")) + response.tabs
        for (channelTab in tabs) {
            val tabName = tabNamesMap[channelTab.name]?.let { getString(it) }
                ?: channelTab.name.replaceFirstChar(Char::titlecase)
            tabList.add(ChannelTab(tabName, channelTab.data))
        }
        channelContentAdapter.notifyItemRangeChanged(0, tabList.size - 1)
    }

    companion object {
        private const val VIDEOS_TAB_KEY = "videos"
    }
}

class ChannelContentAdapter(
    private val list: List<ChannelTab>,
    private val videos: List<StreamItem>,
    private val nextPage: String?,
    private val channelId: String?,
    fragment: Fragment
) : FragmentStateAdapter(fragment) {
    override fun getItemCount() = list.size

    override fun createFragment(position: Int) = ChannelContentFragment().apply {
        arguments = bundleOf(
            IntentData.tabData to list[position],
            IntentData.videoList to videos.toMutableList(),
            IntentData.channelId to channelId,
            IntentData.nextPage to nextPage
        )
    }
}
