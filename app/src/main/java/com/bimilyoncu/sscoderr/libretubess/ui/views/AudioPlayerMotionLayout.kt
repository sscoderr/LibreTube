package com.bimilyoncu.sscoderr.libretubess.ui.views

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.constraintlayout.motion.widget.TransitionAdapter
import com.bimilyoncu.sscoderr.libretubess.R

/**
 * Specialized MotionLayout for the audio player that implements auto-completion behavior
 * on touch release based on progress thresholds.
 */
class AudioPlayerMotionLayout(context: Context, attributeSet: AttributeSet? = null) :
    MotionLayout(context, attributeSet) {

    private val viewToDetectTouch by lazy {
        findViewById<View>(R.id.audio_player_container)
    }
    private val viewRect = Rect()
    private var touchStarted = false
    private val transitionListenerList = mutableListOf<TransitionListener?>()
    private val swipeUpListener = mutableListOf<() -> Unit>()
    private val swipeDownListener = mutableListOf<() -> Unit>()
    private val tapWhenMinimizedListener = mutableListOf<() -> Unit>()
    
    // Used to detect if the touch was a tap and not a swipe
    private var touchStartX = 0f
    private var touchStartY = 0f
    private var isMoveDetected = false

    private var startedMinimized = false
    private var isStrictlyDownSwipe = false
    
    // Track initial touch location
    private var initialTouchY = 0f
    
    // Flag to prevent handling redundant gestures
    private var isAnimatingFromGesture = false

    // Store the transition IDs
    private var transitionStartId = 0
    private var transitionEndId = 0

    init {
        addTransitionListener(object : TransitionAdapter() {
            override fun onTransitionChange(
                motionLayout: MotionLayout?,
                startId: Int,
                endId: Int, 
                progress: Float
            ) {
                transitionStartId = startId
                transitionEndId = endId
            }
            
            override fun onTransitionCompleted(motionLayout: MotionLayout?, currentId: Int) {
                touchStarted = false
                isAnimatingFromGesture = false
            }
        })

        super.setTransitionListener(object : TransitionAdapter() {
            override fun onTransitionChange(p0: MotionLayout?, p1: Int, p2: Int, p3: Float) {
                transitionListenerList.filterNotNull()
                    .forEach { it.onTransitionChange(p0, p1, p2, p3) }
            }

            override fun onTransitionCompleted(p0: MotionLayout?, p1: Int) {
                transitionListenerList.filterNotNull()
                    .forEach { it.onTransitionCompleted(p0, p1) }
            }
        })
    }

    override fun setTransitionListener(listener: TransitionListener?) {
        addTransitionListener(listener)
    }

    override fun addTransitionListener(listener: TransitionListener?) {
        transitionListenerList += listener
    }

    private inner class Listener : GestureDetector.SimpleOnGestureListener() {
        override fun onSingleTapUp(e: MotionEvent): Boolean {
            // If we're in the minimized state and detect a single tap
            // and we're not in the middle of a gesture
            if (startedMinimized && !isMoveDetected && currentState == transitionEndId) {
                tapWhenMinimizedListener.forEach { it.invoke() }
                return true
            }
            return false // Let other click listeners handle taps
        }

        override fun onScroll(
            e1: MotionEvent?,
            e2: MotionEvent,
            distanceX: Float,
            distanceY: Float
        ): Boolean {
            if (isStrictlyDownSwipe && distanceY > 0) {
                isStrictlyDownSwipe = false
            }

            // For special cases not handled by motion scene
            if (isStrictlyDownSwipe && startedMinimized && distanceY < -8F) {
                if (!isAnimatingFromGesture) {
                    isAnimatingFromGesture = true
                    swipeDownListener.forEach { it.invoke() }
                }
                return false  // Let MotionLayout also handle it
            }

            if (progress == 0F && distanceY > 5F) {
                if (!isAnimatingFromGesture) {
                    isAnimatingFromGesture = true
                    swipeUpListener.forEach { it.invoke() }
                }
                return false  // Let MotionLayout also handle it
            }

            return false
        }
    }

    fun addSwipeUpListener(listener: () -> Unit) = apply {
        swipeUpListener.add(listener)
    }

    fun addSwipeDownListener(listener: () -> Unit) = apply {
        swipeDownListener.add(listener)
    }
    
    fun addTapWhenMinimizedListener(listener: () -> Unit) = apply {
        tapWhenMinimizedListener.add(listener)
    }

    private val gestureDetector = GestureDetector(context, Listener())

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        // Process gesture detector without interfering with motion events
        gestureDetector.onTouchEvent(event)
        
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                isStrictlyDownSwipe = true
                startedMinimized = progress == 1F
                initialTouchY = event.y
                
                // Reset the move detection flag on new touch
                isMoveDetected = false
                touchStartX = event.x
                touchStartY = event.y
                
                // Check if touch is inside our detection view
                viewToDetectTouch.getHitRect(viewRect)
                touchStarted = viewRect.contains(event.x.toInt(), event.y.toInt())
            }
            MotionEvent.ACTION_MOVE -> {
                // Detect if this is a move or a tap
                val xDiff = Math.abs(event.x - touchStartX)
                val yDiff = Math.abs(event.y - touchStartY)
                
                // Set move detected if movement exceeds a small threshold
                if (xDiff > 15 || yDiff > 15) {
                    isMoveDetected = true
                }
                
                // If we're in the minimized state and detect any upward movement, help the transition along
                if (startedMinimized && touchStarted && event.y < initialTouchY) {
                    // Calculate how much upward movement we've had
                    val upwardDistance = initialTouchY - event.y
                    
                    // Apply a multiplier to make the transition respond faster to small movements
                    // For very small movements, give them a boost to overcome initial friction
                    if (upwardDistance > 5 && progress > 0.85) {
                        progress -= (upwardDistance / 100)  // Accelerate the transition
                    }
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                // Handle tap in minimized state
                if (touchStarted && !isMoveDetected && currentState == transitionEndId) {
                    tapWhenMinimizedListener.forEach { it.invoke() }
                } else if (touchStarted) {
                    handleTouchRelease()
                }
                
                // Reset state
                touchStarted = false
                isAnimatingFromGesture = false
            }
        }
        
        // Always let MotionLayout handle the touch event if it started in our view
        // This is key for smooth drag animation
        return if (touchStarted) super.onTouchEvent(event) else false
    }
    
    /**
     * Special handling for when touch is released - completes transition based on thresholds
     */
    private fun handleTouchRelease() {
        val currentProgress = progress
        
        // If the progress is not at start or end position when touch is released,
        // complete the transition
        if (currentProgress > 0 && currentProgress < 1) {
            // Different thresholds based on the player's initial state
            // For minimizing (starting from fully expanded): if progress > 0.3, minimize
            // For maximizing (starting from minimized): if progress < 0.7, maximize
            
            // Check if we're minimizing or maximizing based on which state the motion is closer to
            val wasMinimized = currentState == transitionEndId
            
            if (wasMinimized) {
                // We're trying to maximize from minimized state
                val shouldMaximize = currentProgress < 0.9
                if (shouldMaximize) {
                    transitionToStart()
                } else {
                    transitionToEnd()
                }
            } else {
                // We're trying to minimize from maximized state
                val shouldMinimize = currentProgress > 0.3
                if (shouldMinimize) {
                    transitionToEnd()
                } else {
                    transitionToStart()
                }
            }
        }
    }
} 