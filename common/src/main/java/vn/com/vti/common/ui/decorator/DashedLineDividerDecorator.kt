package vn.com.vti.common.ui.decorator

import android.graphics.*
import android.view.View
import androidx.annotation.ColorInt
import androidx.annotation.Px
import androidx.recyclerview.widget.RecyclerView

class DashedLineDividerDecorator(
    @Px private val thickness: Int = 2,
    @ColorInt color: Int = Color.GRAY,
    private val drawHeader: Boolean = false,
    private val drawFooter: Boolean = false
) :
    RecyclerView.ItemDecoration() {
    private val mPaint: Paint = Paint()
    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        outRect.bottom = thickness
    }

    override fun onDrawOver(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        val left = parent.paddingLeft
        val right = parent.width - parent.paddingRight
        val childCount = parent.childCount
        val path = Path()
        if (drawHeader) {
            path.moveTo(left.toFloat(), 0f)
            path.lineTo(right.toFloat(), 0f)
        }
        val end = if (drawFooter) childCount else childCount - 1
        for (i in 0 until end) {
            val child = parent.getChildAt(i)
            val params = child.layoutParams as RecyclerView.LayoutParams
            val top = child.bottom + params.bottomMargin + (thickness shr 1)
            path.moveTo(left.toFloat(), top.toFloat())
            path.lineTo(right.toFloat(), top.toFloat())
        }
        c.drawPath(path, mPaint)
    }

    init {
        mPaint.color = color
        mPaint.style = Paint.Style.STROKE
        mPaint.strokeWidth = thickness.toFloat()
        mPaint.pathEffect = DashPathEffect(floatArrayOf(10f, 10f), 0f)
    }
}