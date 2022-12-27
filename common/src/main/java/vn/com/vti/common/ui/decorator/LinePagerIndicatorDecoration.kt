package vn.com.vti.common.ui.decorator

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.Interpolator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration
import kotlin.math.max

class LinePagerIndicatorDecoration(
    private val colorActive: Int = Color.BLUE,
    private val colorInactive: Int = Color.GRAY,
    private val indicatorHeight: Int = 20,
    private val indicatorStrokeWidth: Int = 2,
    private val indicatorItemLength: Int = 20,
    private val indicatorItemPadding: Int = 8
) : ItemDecoration() {

    /**
     * Some more natural animation interpolation
     */
    private val mInterpolator: Interpolator =
        AccelerateDecelerateInterpolator()
    private val mPaint = Paint()
    override fun onDrawOver(
        c: Canvas,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        super.onDrawOver(c, parent, state)
        val itemCount = state.itemCount
        // center horizontally, calculate width and subtract half from center
        val totalLength = indicatorItemLength * itemCount
        val paddingBetweenItems =
            max(0, itemCount - 1) * indicatorItemPadding
        val indicatorTotalWidth = totalLength + paddingBetweenItems
        val indicatorStartX = (parent.width - indicatorTotalWidth) / 2f
        // center vertically in the allotted space
        val indicatorPosY = parent.height - indicatorHeight / 2f
        drawInactiveIndicators(c, indicatorStartX, indicatorPosY, itemCount)
        // find active page (which should be highlighted)
        val layoutManager = parent.layoutManager as LinearLayoutManager? ?: return
        val activePosition = layoutManager.findFirstVisibleItemPosition()
        if (activePosition == RecyclerView.NO_POSITION) {
            return
        }
        // find offset of active page (if the user is scrolling)
        val activeChild = layoutManager.findViewByPosition(activePosition) ?: return
        val left = activeChild.left
        val width = activeChild.width
        // on swipe the active item will be positioned from [-width, 0]
        // interpolate offset for smooth animation
        val progress =
            mInterpolator.getInterpolation(left * -1 / width.toFloat())
        drawHighlights(c, indicatorStartX, indicatorPosY, activePosition, progress, itemCount)
    }

    private fun drawInactiveIndicators(
        c: Canvas,
        indicatorStartX: Float,
        indicatorPosY: Float,
        itemCount: Int
    ) {
        mPaint.color = colorInactive
        // width of item indicator including padding
        val itemWidth = indicatorItemLength + indicatorItemPadding
        var start = indicatorStartX
        for (i in 0..itemCount) { // draw the line for every item
            c.drawLine(start, indicatorPosY, start + indicatorItemLength, indicatorPosY, mPaint)
            start += itemWidth
        }
    }

    private fun drawHighlights(
        c: Canvas, indicatorStartX: Float, indicatorPosY: Float,
        highlightPosition: Int, progress: Float, itemCount: Int
    ) {
        mPaint.color = colorActive
        // width of item indicator including padding
        val itemWidth = indicatorItemLength + indicatorItemPadding
        if (progress == 0f) { // no swipe, draw a normal indicator
            val highlightStart = indicatorStartX + itemWidth * highlightPosition
            c.drawLine(
                highlightStart, indicatorPosY,
                highlightStart + indicatorItemLength, indicatorPosY, mPaint
            )
        } else {
            var highlightStart = indicatorStartX + itemWidth * highlightPosition
            // calculate partial highlight
            val partialLength = indicatorItemLength * progress
            // draw the cut off highlight
            c.drawLine(
                highlightStart + partialLength, indicatorPosY,
                highlightStart + indicatorItemLength, indicatorPosY, mPaint
            )
            // draw the highlight overlapping to the next item as well
            if (highlightPosition < itemCount - 1) {
                highlightStart += itemWidth
                c.drawLine(
                    highlightStart, indicatorPosY,
                    highlightStart + partialLength, indicatorPosY, mPaint
                )
            }
        }
    }

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        super.getItemOffsets(outRect, view, parent, state)
        outRect.bottom = indicatorHeight
    }

    companion object {
        fun newInstance(): LinePagerIndicatorDecoration {
            return LinePagerIndicatorDecoration()
        }
    }

    init {
        mPaint.apply {
            strokeCap = Paint.Cap.ROUND
            strokeWidth = indicatorStrokeWidth.toFloat()
            style = Paint.Style.STROKE
            isAntiAlias = true
        }
    }
}