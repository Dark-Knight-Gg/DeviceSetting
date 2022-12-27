package vn.com.vti.common.util

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.view.MotionEvent
import android.view.View
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import vn.com.vti.common.util.AppResources.getResources
import java.util.*
import kotlin.math.abs
import kotlin.math.max


abstract class SwipeHelper(private val swipedListener: (Int) -> Unit) :
    ItemTouchHelper.SimpleCallback(
        ItemTouchHelper.ACTION_STATE_IDLE,
        ItemTouchHelper.LEFT
    ) {
    private var mRecyclerView: RecyclerView? = null
    private var swipedPosition = -1
    val buttonsBuffer: MutableMap<Int, List<UnderlayButton>> = mutableMapOf()
    private val recoverQueue = object : LinkedList<Int>() {
        override fun add(element: Int): Boolean {
            if (contains(element)) return false
            return super.add(element)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private val touchListener = object : RecyclerView.OnItemTouchListener {
        override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
            val item = rv.findChildViewUnder(e.x, e.y)
            if (item != null) {
                val position = rv.getChildLayoutPosition(item)
                swipedListener.invoke(position)
            }
            if (swipedPosition < 0) {
                return false
            }
            buttonsBuffer[swipedPosition]?.forEach { it.handle(e) }
            recoverQueue.add(swipedPosition)
            swipedPosition = -1
            recoverSwipedItem()
            return true
        }

        override fun onTouchEvent(rv: RecyclerView, e: MotionEvent) {
            //intentionally blank
            swipedListener.invoke(-1)
        }

        override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {
            //intentionally blank
        }
    }

    fun attachToRecyclerView(recyclerView: RecyclerView?) {
        if (mRecyclerView == recyclerView) return
        mRecyclerView?.let {
            mRecyclerView?.removeOnItemTouchListener(touchListener)
        }
        mRecyclerView = recyclerView?.also {
            it.addOnItemTouchListener(touchListener)
        }
    }

    private fun recoverSwipedItem() {
        while (!recoverQueue.isEmpty()) {
            recoverQueue.poll() ?: return
        }
    }

    private fun drawButtons(
        canvas: Canvas,
        buttons: List<UnderlayButton>,
        itemView: View,
        dX: Float
    ) {
        var right = itemView.right
        buttons.forEach { button ->
            val width = button.intrinsicWidth / buttons.intrinsicWidth() * abs(dX)
            val left = right - width
            button.draw(
                canvas,
                RectF(left, itemView.top.toFloat(), right.toFloat(), itemView.bottom.toFloat())
            )

            right = left.toInt()
        }
    }

    override fun onChildDraw(
        c: Canvas,
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean
    ) {
        val position = viewHolder.bindingAdapterPosition
        var maxDX = dX
        val itemView = viewHolder.itemView

        if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE && dX < 0) {
            if (!buttonsBuffer.containsKey(position)) {
                buttonsBuffer[position] =
                    instantiateUnderlayButton(recyclerView.context, position)
            }

            val buttons = buttonsBuffer[position] ?: return
            if (buttons.isEmpty()) return
            maxDX = max(-buttons.intrinsicWidth(), dX)
            drawButtons(c, buttons, itemView, maxDX)
        }

        super.onChildDraw(
            c,
            recyclerView,
            viewHolder,
            maxDX,
            dY,
            actionState,
            isCurrentlyActive
        )
    }

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        return false
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        val position = viewHolder.bindingAdapterPosition
        if (swipedPosition != position) recoverQueue.add(swipedPosition)
        swipedPosition = position
        recoverSwipedItem()
    }

    abstract fun instantiateUnderlayButton(context: Context, position: Int): List<UnderlayButton>

    interface UnderlayButtonClickListener {
        fun onClick()
    }

    class UnderlayButton(
        private val context: Context,
        private val title: String,
        textSize: Float,
        @DrawableRes private var drawRes: Int?,
        @ColorRes private val colorRes: Int,
        private val clickListener: UnderlayButtonClickListener
    ) {
        private var clickableRegion: RectF? = null
        private val textSizeInPixel: Float =
            textSize * context.resources.displayMetrics.density // dp to px
        private val iconSizeInPixel = 30f * context.resources.displayMetrics.density
        private val horizontalPadding = 50.0f
        val intrinsicWidth: Float

        init {
            val paint = Paint()
            paint.textSize = textSizeInPixel
            paint.typeface = Typeface.DEFAULT_BOLD
            paint.textAlign = Paint.Align.LEFT
            val titleBounds = Rect()
            paint.getTextBounds(title, 0, title.length, titleBounds)
            intrinsicWidth = titleBounds.width() + 2 * horizontalPadding
        }

        @SuppressLint("UseCompatLoadingForDrawables")
        fun draw(canvas: Canvas, rect: RectF) {
            val paint = Paint()

            // Draw background
            paint.color = ContextCompat.getColor(context, colorRes)
            canvas.drawRect(rect, paint)

            // Draw title
            paint.color = ContextCompat.getColor(context, android.R.color.white)
            paint.textSize = textSizeInPixel
            paint.typeface = Typeface.DEFAULT_BOLD
            paint.textAlign = Paint.Align.LEFT

            val titleBounds = Rect()
            paint.getTextBounds(title, 0, title.length, titleBounds)

            val icon = drawRes?.let {
                context.resources.getDrawable(it, getResources().newTheme())
            }
            icon?.setBounds(
                (rect.centerX() - iconSizeInPixel / 2).toInt(),
                (rect.centerY() - iconSizeInPixel / 2).toInt(),
                (rect.centerX() + iconSizeInPixel / 2).toInt(),
                (rect.centerY() + iconSizeInPixel / 2).toInt()
            )
            icon?.draw(canvas)

            val yTitle = icon?.let {
                (it.bounds.bottom + 30).toFloat()
            }
            yTitle?.let {
                canvas.drawText(title, rect.left + horizontalPadding, it, paint)
            }


            clickableRegion = rect
        }

        fun handle(event: MotionEvent) {
            clickableRegion?.let {
                if (it.contains(event.x, event.y)) {
                    clickListener.onClick()
                }
            }
        }
    }
}

private fun List<SwipeHelper.UnderlayButton>.intrinsicWidth(): Float {
    if (isEmpty()) return 0.0f
    return map { it.intrinsicWidth }.sum()
}