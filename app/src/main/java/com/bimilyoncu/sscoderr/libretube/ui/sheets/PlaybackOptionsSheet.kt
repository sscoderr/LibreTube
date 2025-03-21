package com.bimilyoncu.sscoderr.libretube.ui.sheets

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.media3.common.PlaybackParameters
import androidx.media3.session.MediaController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bimilyoncu.sscoderr.libretube.R
import com.bimilyoncu.sscoderr.libretube.constants.PreferenceKeys
import com.bimilyoncu.sscoderr.libretube.databinding.PlaybackBottomSheetBinding
import com.bimilyoncu.sscoderr.libretube.enums.PlayerCommand
import com.bimilyoncu.sscoderr.libretube.extensions.round
import com.bimilyoncu.sscoderr.libretube.helpers.PreferenceHelper
import com.bimilyoncu.sscoderr.libretube.services.AbstractPlayerService
import com.bimilyoncu.sscoderr.libretube.ui.adapters.SliderLabelsAdapter

class PlaybackOptionsSheet(
    private val player: MediaController
) : ExpandedBottomSheet(R.layout.playback_bottom_sheet) {
    private var _binding: PlaybackBottomSheetBinding? = null
    private val binding get() = _binding!!

    @androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        _binding = PlaybackBottomSheetBinding.bind(view)

        binding.speedShortcuts.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        binding.pitchShortcuts.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)

        binding.speedShortcuts.adapter = SliderLabelsAdapter(SUGGESTED_SPEEDS) {
            binding.speed.value = it
        }
        binding.pitchShortcuts.adapter = SliderLabelsAdapter(SUGGESTED_PITCHES) {
            binding.pitch.value = it
        }

        binding.speed.value = player.playbackParameters.speed
        binding.pitch.value = player.playbackParameters.pitch
        PreferenceHelper.getBoolean(PreferenceKeys.SKIP_SILENCE, false).let {
            binding.skipSilence.isChecked = it
        }

        binding.speed.addOnChangeListener { _, _, _ ->
            onChange()
        }

        binding.pitch.addOnChangeListener { _, _, _ ->
            onChange()
        }

        binding.skipSilence.setOnCheckedChangeListener { _, isChecked ->
            player.sendCustomCommand(
                AbstractPlayerService.runPlayerActionCommand,
                bundleOf(PlayerCommand.SKIP_SILENCE.name to isChecked)
            )
            PreferenceHelper.putBoolean(PreferenceKeys.SKIP_SILENCE, isChecked)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun onChange() {
        player.playbackParameters = PlaybackParameters(
            binding.speed.value.round(2),
            binding.pitch.value.round(2)
        )

        if (PreferenceHelper.getBoolean(PreferenceKeys.REMEMBER_PLAYBACK_SPEED, true)) {
            val currentSpeed = player.playbackParameters.speed.toString()
            PreferenceHelper.putString(PreferenceKeys.PLAYBACK_SPEED, currentSpeed)
        }
    }

    companion object {
        private val SUGGESTED_SPEEDS = listOf(0.5f, 1f, 1.25f, 1.5f, 1.75f, 2f)
        private val SUGGESTED_PITCHES = listOf(0.5f, 1f, 1.5f, 2f)
    }
}
