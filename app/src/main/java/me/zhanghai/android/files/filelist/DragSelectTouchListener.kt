/*
 * Copyright (c) 2026 Material Files contributors
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package me.zhanghai.android.files.filelist

import android.view.MotionEvent
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.roundToInt

/**
 * Selects a range of items while the finger is dragged after a long press, scrolling the list
 * automatically when the finger is near the top or bottom edge.
 *
 * The drag is started with [startDrag] (from a long click) and lasts until the finger is lifted.
 * Every time the item under the finger changes, [Listener.onDragSelectionUpdate] is called with
 * its adapter position.
 */
class DragSelectTouchListener(private val listener: Listener) : RecyclerView.OnItemTouchListener {
    private var recyclerView: RecyclerView? = null

    private var isActive = false
    private var lastX = 0f
    private var lastY = 0f
    private var hasLastPosition = false
    private var lastReportedPosition = RecyclerView.NO_POSITION

    private var scrollSpeed = 0
    private var isAutoScrolling = false

    private val autoScrollRunnable = object : Runnable {
        override fun run() {
            val recyclerView = recyclerView
            if (recyclerView == null || !isActive || scrollSpeed == 0) {
                isAutoScrolling = false
                return
            }
            recyclerView.scrollBy(0, scrollSpeed)
            reportPositionUnderFinger(recyclerView)
            ViewCompat.postOnAnimation(recyclerView, this)
        }
    }

    fun attach(recyclerView: RecyclerView) {
        if (this.recyclerView === recyclerView) {
            return
        }
        detach()
        this.recyclerView = recyclerView
        recyclerView.addOnItemTouchListener(this)
    }

    fun detach() {
        val recyclerView = recyclerView ?: return
        stop()
        recyclerView.removeOnItemTouchListener(this)
        this.recyclerView = null
    }

    fun startDrag(position: Int) {
        val recyclerView = recyclerView ?: return
        isActive = true
        hasLastPosition = false
        lastReportedPosition = position
        // Keep parents (e.g. the drawer layout) from stealing the gesture.
        recyclerView.parent?.requestDisallowInterceptTouchEvent(true)
    }

    override fun onInterceptTouchEvent(recyclerView: RecyclerView, event: MotionEvent): Boolean {
        if (!isActive) {
            return false
        }
        return when (event.actionMasked) {
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                stop()
                false
            }
            // Take over the gesture, so that the item view gets a cancel event and the list
            // doesn't scroll by itself.
            else -> true
        }
    }

    override fun onTouchEvent(recyclerView: RecyclerView, event: MotionEvent) {
        if (!isActive) {
            return
        }
        when (event.actionMasked) {
            MotionEvent.ACTION_MOVE -> {
                lastX = event.x.coerceIn(0f, (recyclerView.width - 1).coerceAtLeast(0).toFloat())
                lastY = event.y
                hasLastPosition = true
                updateAutoScroll(recyclerView)
                reportPositionUnderFinger(recyclerView)
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> stop()
        }
    }

    override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {}

    private fun reportPositionUnderFinger(recyclerView: RecyclerView) {
        if (!hasLastPosition) {
            return
        }
        val y = lastY.coerceIn(0f, (recyclerView.height - 1).coerceAtLeast(0).toFloat())
        val child = recyclerView.findChildViewUnder(lastX, y) ?: return
        val position = recyclerView.getChildAdapterPosition(child)
        if (position == RecyclerView.NO_POSITION || position == lastReportedPosition) {
            return
        }
        lastReportedPosition = position
        listener.onDragSelectionUpdate(position)
    }

    private fun updateAutoScroll(recyclerView: RecyclerView) {
        val height = recyclerView.height
        if (height <= 0) {
            scrollSpeed = 0
            return
        }
        val density = recyclerView.resources.displayMetrics.density
        val hotspotHeight = (AUTO_SCROLL_HOTSPOT_DP * density).coerceAtMost(height / 4f)
        val maxSpeed = AUTO_SCROLL_MAX_SPEED_DP * density
        scrollSpeed = when {
            lastY < hotspotHeight ->
                -(maxSpeed * ((hotspotHeight - lastY) / hotspotHeight).coerceAtMost(1f))
                    .roundToInt().coerceAtLeast(1)
            lastY > height - hotspotHeight ->
                (maxSpeed * ((lastY - (height - hotspotHeight)) / hotspotHeight)
                    .coerceAtMost(1f)).roundToInt().coerceAtLeast(1)
            else -> 0
        }
        if (scrollSpeed != 0 && !isAutoScrolling) {
            isAutoScrolling = true
            ViewCompat.postOnAnimation(recyclerView, autoScrollRunnable)
        }
    }

    private fun stop() {
        val wasActive = isActive
        isActive = false
        hasLastPosition = false
        scrollSpeed = 0
        lastReportedPosition = RecyclerView.NO_POSITION
        recyclerView?.removeCallbacks(autoScrollRunnable)
        isAutoScrolling = false
        if (wasActive) {
            listener.onDragSelectionEnd()
        }
    }

    interface Listener {
        fun onDragSelectionUpdate(position: Int)

        fun onDragSelectionEnd()
    }

    companion object {
        private const val AUTO_SCROLL_HOTSPOT_DP = 64f
        private const val AUTO_SCROLL_MAX_SPEED_DP = 16f
    }
}
