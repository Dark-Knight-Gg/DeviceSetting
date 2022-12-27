package vn.com.vti.common.ui.decorator

import android.graphics.Rect
import android.view.View
import androidx.annotation.IntRange
import androidx.recyclerview.widget.RecyclerView

class HorizontalLinearMarginDecorator(
    @IntRange(from = 0) verticalMargin: Int,
    @IntRange(from = 0) horizontalMargin: Int,
    @IntRange(from = 0) firstItemLeftExtraSpace: Int = 0,
    @IntRange(from = 0) lastItemRightExtraSpace: Int = 0
) : RecyclerView.ItemDecoration() {

    private val outRectApplier: Rect.(view: View, parent: RecyclerView, state: RecyclerView.State) -> Unit

    init {
        if (firstItemLeftExtraSpace != 0 && lastItemRightExtraSpace != 0) {
            outRectApplier = { view, parent, state ->
                left = 0
                right = horizontalMargin
                top = verticalMargin
                bottom = verticalMargin
                parent.getChildAdapterPosition(view).let {
                    if (it == 0) {
                        left = firstItemLeftExtraSpace
                    }
                    if (it == state.itemCount - 1) {
                        bottom = lastItemRightExtraSpace + horizontalMargin
                    }
                }
            }
        } else if (firstItemLeftExtraSpace != 0) {
            outRectApplier = { view, parent, _ ->
                right = horizontalMargin
                top = verticalMargin
                bottom = verticalMargin
                parent.getChildAdapterPosition(view).let {
                    left = if (it == 0) {
                        firstItemLeftExtraSpace
                    } else 0
                }
            }
        } else if (lastItemRightExtraSpace != 0) {
            outRectApplier = { view, parent, state ->
                left = 0
                top = verticalMargin
                bottom = verticalMargin
                parent.getChildAdapterPosition(view).let {
                    right = if (it == state.itemCount - 1) {
                        lastItemRightExtraSpace + horizontalMargin
                    } else {
                        horizontalMargin
                    }
                }
            }
        } else {
            outRectApplier = { _, _, _ ->
                left = 0
                right = horizontalMargin
                top = verticalMargin
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