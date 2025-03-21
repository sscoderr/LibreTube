package com.github.libretube.ui.views

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.constraintlayout.motion.widget.TransitionAdapter
import com.github.libretube.R

class SingleViewTouchableMotionLayout(context: Context, attributeSet: AttributeSet? = null) :
    MotionLayout(context, attributeSet) {

    private val viewToDetectTouch by lazy {
        findViewById<View>(R.id.main_container) ?: findViewById(R.id.audio_player_container)
    }
    private val viewRect = Rect()
    private var touchStarted = false
    private val transitionListenerList = mutableListOf<TransitionListener?>()
    private val swipeUpListener = mutableListOf<() -> Unit>()
    private val swipeDownListener = mutableListOf<() -> Unit>()

    private var startedMinimized = false
    private var isStrictlyDownSwipe = false
    
    // Track initial touch location
    private var initialTouchY = 0f
    
    // Flag to prevent handling redundant gestures
    private var isAnimatingFromGesture = false

    init {
        addTransitionListener(object : TransitionAdapter() {
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
            // For audio player, we want to allow clicks inside
            if (viewToDetectTouch.id == R.id.audio_player_container) return false
            
            setTransitionDuration(200)
            transitionToStart()
            return true
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
            if (isStrictlyDownSwipe && startedMinimized && distanceY < -15F) {
                if (!isAnimatingFromGesture) {
                    isAnimatingFromGesture = true
                    swipeDownListener.forEach { it.invoke() }
                }
                return false  // Let MotionLayout also handle it
            }

            if (progress == 0F && distanceY > 30F) {
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
                
                // Check if touch is inside our detection view
                viewToDetectTouch.getHitRect(viewRect)
                touchStarted = viewRect.contains(event.x.toInt(), event.y.toInt())
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                // Reset state
                touchStarted = false
                isAnimatingFromGesture = false
            }
        }
        
        // Always let MotionLayout handle the touch event if it started in our view
        // This is key for smooth drag animation
        return if (touchStarted) super.onTouchEvent(event) else false
    }
}
