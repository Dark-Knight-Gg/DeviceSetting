package vn.com.vti.common.ui.decorator

import android.graphics.Rect
import android.view.View
import androidx.annotation.IntRange
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration

class MarginDecorator(
    @IntRange(from = 0) verticalMargin: Int,
    @IntRange(from = 0) horizontalMargin: Int,
    @IntRange(from = 0) firstItemTopExtraSpace: Int = 0,
    @IntRange(from = 0) lastItemBottomExtraSpace: Int = 0
) : ItemDecoration() {

    private val outRectApplier: Rect.(view: View, parent: RecyclerView, state: RecyclerView.State) -> Unit

    init {
        if (firstItemTopExtraSpace != 0 && lastItemBottomExtraSpace != 0) {
            outRectApplier = { view, parent, state ->
                left = horizontalMargin
                right = horizontalMargin
                top = 0
                bottom = verticalMargin
                parent.getChildAdapterPosition(view).let {
                    if (it == 0) {
                        top = firstItemTopExtraSpace
                    }
                    if (it == state.itemCount - 1) {
                        bottom = lastItemBottomExtraSpace + verticalMargin
                    }
                }
            }
        } else if (firstItemTopExtraSpace != 0) {
            outRectApplier = { view, parent, _ ->
                left = horizontalMargin
                right = horizontalMargin
                top = 0
                bottom = verticalMargin
                parent.getChildAdapterPosition(view).let {
                    if (it == 0) {
                        top = firstItemTopExtraSpace
                    }
                }
            }
        } else if (lastItemBottomExtraSpace != 0) {
            outRectApplier = { view, parent, state ->
                left = horizontalMargin
                right = horizontalMargin
                top = 0
                parent.getChildAdapterPosition(view).let {
                    if (it == state.itemCount - 1) {
                        bottom = lastItemBottomExtraSpace + verticalMargin
                    } else {
                        bottom = verticalMargin
                    }
                }
            }
        } else {
            outRectApplier = { _, _, _ ->
                left = horizontalMargin
                right = horizontalMargin
                top = 0
                bottom = verticalMargin
            }
        }
    }

    override fun getItemOffsets(
        outRect: Rect, view: View,
        parent: RecyclerView,
        state: RecyclerView.State,
    ) {
        super.getItemOffsets(outRect, view, parent, state)
        outRect.outRectApplier(view, parent, state)
    }
}