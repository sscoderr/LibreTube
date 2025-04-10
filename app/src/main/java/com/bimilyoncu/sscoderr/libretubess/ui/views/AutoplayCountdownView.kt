package com.bimilyoncu.sscoderr.libretubess.ui.views

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.core.os.postDelayed
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.get
import com.bimilyoncu.sscoderr.libretubess.R
import com.bimilyoncu.sscoderr.libretubess.compat.PictureInPictureCompat
import com.bimilyoncu.sscoderr.libretubess.databinding.AutoplayCountdownBinding
import com.bimilyoncu.sscoderr.libretubess.helpers.ContextHelper
import com.bimilyoncu.sscoderr.libretubess.ui.activities.MainActivity
import com.bimilyoncu.sscoderr.libretubess.ui.models.CommonPlayerViewModel

class AutoplayCountdownView(
    context: Context,
    attributeSet: AttributeSet?
) : FrameLayout(context, attributeSet) {
    private val layoutInflater = LayoutInflater.from(context)
    private val commonPlayerViewModel: CommonPlayerViewModel
        get() = ViewModelProvider(
        context as MainActivity
    ).get()

    val binding = AutoplayCountdownBinding.inflate(layoutInflater, this, true)
    private var onCountdownEnd: () -> Unit = {}
    private var hideSelf: () -> Unit = {}
    private val handler = Handler(Looper.getMainLooper())
    private var currentTimerState = COUNTDOWN_SECONDS

    init {
        binding.cancel.setOnClickListener {
            cancelAndHideCountdown()
        }
    }

    fun setHideSelfListener(listener: () -> Unit) {
        hideSelf = listener
    }

    fun startCountdown(onEnd: () -> Unit) {
        onCountdownEnd = {
            hideSelf.invoke()
            onEnd.invoke()
        }
        currentTimerState = COUNTDOWN_SECONDS
        binding.playNext.setOnClickListener {
            handler.removeCallbacksAndMessages(TIMER_RUNNABLE_TOKEN)
            onCountdownEnd.invoke()
        }
        updateCountdown()
    }

    private fun updateCountdown() {
        if (currentTimerState == 0) {
            onCountdownEnd.invoke()
            return
        }

        // don't show cancel and play next buttons in PiP mode
        val context = ContextHelper.unwrapActivity<MainActivity>(context)
        val isInPipMode = PictureInPictureCompat.isInPictureInPictureMode(context)
        binding.cancel.isVisible = !isInPipMode
        binding.playNext.isVisible = !isInPipMode

        // only show countdown when mini player not visible
        this.isVisible = commonPlayerViewModel.isMiniPlayerVisible.value == false
        binding.currentState.text = context.getString(
            R.string.playing_next,
            currentTimerState.toString()
        )
        currentTimerState--
        handler.postDelayed(1000, TIMER_RUNNABLE_TOKEN, this::updateCountdown)
    }

    fun cancelAndHideCountdown() {
        handler.removeCallbacksAndMessages(TIMER_RUNNABLE_TOKEN)
        hideSelf.invoke()
    }

    companion object {
        private const val COUNTDOWN_SECONDS = 5
        private const val TIMER_RUNNABLE_TOKEN = "timer_runnable"
    }
}
