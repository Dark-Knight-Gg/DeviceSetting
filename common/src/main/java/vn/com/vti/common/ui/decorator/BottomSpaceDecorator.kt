package vn.com.vti.common.ui.decorator

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class BottomSpaceDecorator(private val space: Int) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(
        outRect: Rect, view: View,
        parent: RecyclerView,
        state: RecyclerView.State,
    ) {
        super.getItemOffsets(outRect, view, parent, state)
        outRect.apply {
            val bottom =
                if (parent.getChildLayoutPosition(view) == state.itemCount - 1) space else 0
            set(0, 0, 0, bottom)
        }
    }
}