package vn.com.vti.common.ui.decorator

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration
import kotlin.math.max

class DotPagerIndicatorDecoration(
    private val colorActive: Int = Color.BLUE,
    private val colorInactive: Int = Color.GRAY,
    private val indicatorHeight: Int = 20,
    private val indicatorItemRadius: Int = 10,
    private val indicatorItemPadding: Int = 8
) : ItemDecoration() {

    private val mPaint = Paint()

    override fun onDrawOver(
        c: Canvas,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        super.onDrawOver(c, parent, state)
        val itemCount = state.itemCount

        if (itemCount > 2) {
            // center horizontally, calculate width and subtract half from center
            val totalLength = indicatorItemRadius * 2 * itemCount
            val paddingBetweenItems =
                max(0, itemCount - 1) * indicatorItemPadding
            val indicatorTotalWidth = totalLength + paddingBetweenItems
            val indicatorX = (parent.width - indicatorTotalWidth) / 2f

            // center vertically in the allotted space
            val indicatorY = parent.height - indicatorHeight / 2f
            drawInactiveIndicators(c, indicatorX, indicatorY, itemCount)

            // find active page (which should be highlighted)
            val layoutManager = parent.layoutManager as? LinearLayoutManager ?: return
            val activePosition =
                if (layoutManager.findFirstCompletelyVisibleItemPosition() == 0) 0 else layoutManager.findLastCompletelyVisibleItemPosition()
            if (activePosition == RecyclerView.NO_POSITION) {
                return
            }
            drawHighlights(c, indicatorX, indicatorY, activePosition)
        }

    }

    private fun drawInactiveIndicators(
        c: Canvas,
        indicatorX: Float,
        indicatorY: Float,
        itemCount: Int
    ) {
        mPaint.color = colorInactive

        val itemWidth = indicatorItemRadius * 2 + indicatorItemPadding
        var cx = indicatorX
        repeat(itemCount) {
            c.drawCircle(cx, indicatorY, indicatorItemRadius.toFloat(), mPaint)
            cx += itemWidth
        }
    }

    private fun drawHighlights(
        c: Canvas, indicatorX: Float, indicatorY: Float,
        highlightPosition: Int
    ) {
        mPaint.color = colorActive

        val itemWidth = indicatorItemRadius * 2 + indicatorItemPadding

        val highlightCx = indicatorX + itemWidth * highlightPosition

        c.drawCircle(
            highlightCx, indicatorY,
            indicatorItemRadius.toFloat(), mPaint
        )
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

    init {
        mPaint.apply {
            strokeCap = Paint.Cap.ROUND
            strokeWidth = (indicatorItemRadius * 2).toFloat()
            style = Paint.Style.FILL
            isAntiAlias = true
        }
    }
}
