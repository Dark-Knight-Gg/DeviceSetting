package vn.com.vti.common.ui.decorator

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration

class MarginGridDecorator(
    private val verticalMargin: Int,
    private val horizontalMargin: Int,
    private val firstItemOffset: Int,
    private val lastItemOffset: Int,
) : ItemDecoration() {

    private var spanCount = 0
    private var lastItemCount = 0
    private var lastRowStartIndex = 0
    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State,
    ) {
        super.getItemOffsets(outRect, view, parent, state)
        syncState(parent, state)
        outRect.apply {
            parent.getChildAdapterPosition(view).let {
                when {
                    isFirstRow(it) -> {
                        top = firstItemOffset
                        bottom = verticalMargin
                    }
                    isLastRow(it) -> {
                        top = verticalMargin
                        bottom = lastItemOffset
                    }
                    else -> {
                        top = verticalMargin
                        bottom = verticalMargin
                    }
                }
            }
            left = horizontalMargin
            right = horizontalMargin
        }
    }

    private fun syncState(parent: RecyclerView, state: RecyclerView.State) {
        if (lastItemCount != state.itemCount) {
            lastItemCount = state.itemCount
            if (lastItemCount == 0) {
                lastRowStartIndex = -1
                spanCount = 0
            } else (parent.layoutManager as? GridLayoutManager)?.let {
                spanCount = it.spanCount
                lastRowStartIndex = if (state.itemCount % spanCount == 0) {
                    state.itemCount - spanCount
                } else {
                    state.itemCount / spanCount * spanCount
                }
            } ?: apply {
                spanCount = 0
                lastRowStartIndex = -1
            }
        }
    }

    private fun isFirstRow(position: Int): Boolean {
        return position < spanCount
    }

    private fun isLastRow(position: Int): Boolean {
        return position >= lastRowStartIndex
    }
}