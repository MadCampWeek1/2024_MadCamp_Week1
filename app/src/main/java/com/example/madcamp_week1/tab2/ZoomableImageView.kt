package com.example.madcamp_week1

import android.content.Context
import android.graphics.Matrix
import android.graphics.PointF
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import androidx.appcompat.widget.AppCompatImageView
import kotlin.math.sqrt

class ZoomableImageView(context: Context, attrs: AttributeSet?) :
    AppCompatImageView(context, attrs) {

    private val scaleGestureDetector = ScaleGestureDetector(context, ScaleListener())
    private val gestureDetector = GestureDetector(context, GestureListener())
    private val matrix = Matrix()
    private val savedMatrix = Matrix()
    private var mode = NONE

    // States
    private val start = PointF()
    private val mid = PointF()
    private var oldDist = 1f

    // Scaling limits
    private var scaleFactor = 1.0f
    private var initialScale = 1.0f
    private val minScaleFactor = 0.5f
    private val maxScaleFactor = 70.0f

    init {
        imageMatrix = matrix
        scaleType = ScaleType.MATRIX
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        scaleGestureDetector.onTouchEvent(event)
        gestureDetector.onTouchEvent(event)

        when (event.action and MotionEvent.ACTION_MASK) {
            MotionEvent.ACTION_DOWN -> {
                savedMatrix.set(matrix)
                start.set(event.x, event.y)
                mode = DRAG
            }
            MotionEvent.ACTION_POINTER_DOWN -> {
                oldDist = spacing(event)
                if (oldDist > 10f) {
                    savedMatrix.set(matrix)
                    midPoint(mid, event)
                    mode = ZOOM
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP -> {
                mode = NONE
            }
            MotionEvent.ACTION_MOVE -> {
                if (mode == DRAG) {
                    matrix.set(savedMatrix)
                    val dx = event.x - start.x
                    val dy = event.y - start.y
                    matrix.postTranslate(dx, dy)
                } else if (mode == ZOOM) {
                    val newDist = spacing(event)
                    if (newDist > 10f) {
                        val scale = newDist / oldDist
                        val newScaleFactor = scaleFactor * scale
                        if (newScaleFactor in initialScale * minScaleFactor..initialScale * maxScaleFactor) {
                            matrix.set(savedMatrix)
                            matrix.postScale(scale, scale, mid.x, mid.y)
                            scaleFactor = newScaleFactor
                        }
                    }
                }
            }
        }

        imageMatrix = matrix
        return true
    }

    override fun setImageDrawable(drawable: Drawable?) {
        super.setImageDrawable(drawable)
        fitImageToScreen()
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        if (changed) {
            fitImageToScreen()
        }
    }

    private fun fitImageToScreen() {
        val drawable = drawable ?: return

        val imageWidth = drawable.intrinsicWidth
        val imageHeight = drawable.intrinsicHeight

        val viewWidth = width
        val viewHeight = height

        if (viewWidth == 0 || viewHeight == 0) return

        val widthScale = viewWidth.toFloat() / imageWidth.toFloat()
        val heightScale = viewHeight.toFloat() / imageHeight.toFloat()
        val scale = widthScale.coerceAtMost(heightScale)

        val dx = (viewWidth - imageWidth * scale) / 2
        val dy = (viewHeight - imageHeight * scale) / 2

        matrix.setScale(scale, scale)
        matrix.postTranslate(dx, dy)

        imageMatrix = matrix
        initialScale = scale
        scaleFactor = scale
    }

    fun resetZoom() {
        scaleFactor = initialScale
        fitImageToScreen()
    }

    private inner class ScaleListener : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            val scale = detector.scaleFactor
            val newScaleFactor = scaleFactor * scale
            if (newScaleFactor in initialScale * minScaleFactor..initialScale * maxScaleFactor) {
                scaleFactor = newScaleFactor
                matrix.postScale(scale, scale, detector.focusX, detector.focusY)
                imageMatrix = matrix
            }
            return true
        }
    }

    private inner class GestureListener : GestureDetector.SimpleOnGestureListener() {
        override fun onDoubleTap(e: MotionEvent): Boolean {
            resetZoom()
            return true
        }
    }

    private fun spacing(event: MotionEvent): Float {
        val x = event.getX(0) - event.getX(1)
        val y = event.getY(0) - event.getY(1)
        return sqrt((x * x + y * y).toDouble()).toFloat()
    }

    private fun midPoint(point: PointF, event: MotionEvent) {
        val x = event.getX(0) + event.getX(1)
        val y = event.getY(0) + event.getY(1)
        point.set(x / 2, y / 2)
    }

    companion object {
        private const val NONE = 0
        private const val DRAG = 1
        private const val ZOOM = 2
    }
}
