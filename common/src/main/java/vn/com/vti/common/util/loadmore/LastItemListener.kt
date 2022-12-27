package vn.com.vti.common.util.loadmore

import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager

abstract class LastItemListener(layoutManager: RecyclerView.LayoutManager) :
    RecyclerView.OnScrollListener() {
    private val lastVisibleItemFinder: ILastVisibleItemFinder = when (layoutManager) {
        is GridLayoutManager -> GridVisibleItemFinder(layoutManager)
        is LinearLayoutManager -> LinearVisibleItemFinder(layoutManager)
        is StaggeredGridLayoutManager -> StaggedVisibleItemFinder(layoutManager)
        else -> throw UnsupportedOperationException(layoutManager.javaClass.toString() + " is not supported yet")
    }
    private var lastVisibleItem = 0
    private var tempVisibleItem = 0
    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        super.onScrolled(recyclerView, dx, dy)
        tempVisibleItem = lastVisibleItemFinder.findLastVisibleItemPosition()
        if (tempVisibleItem != lastVisibleItem) {
            onLastItemIndexChanged(tempVisibleItem)
            lastVisibleItem = tempVisibleItem
        }
    }

    protected abstract fun onLastItemIndexChanged(newIndex: Int)
    interface ILastVisibleItemFinder {
        fun findLastVisibleItemPosition(): Int
    }

}