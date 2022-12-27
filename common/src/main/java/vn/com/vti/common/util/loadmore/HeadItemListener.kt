package vn.com.vti.common.util.loadmore

import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager

abstract class HeadItemListener(layoutManager: RecyclerView.LayoutManager) :
    RecyclerView.OnScrollListener() {
    private val firstVisibleItemFinder: IFirstVisibleItemFinder = when (layoutManager) {
        is GridLayoutManager -> GridVisibleItemFinder(layoutManager)
        is LinearLayoutManager -> LinearVisibleItemFinder(layoutManager)
        is StaggeredGridLayoutManager -> StaggedVisibleItemFinder(layoutManager)
        else -> throw UnsupportedOperationException(layoutManager.javaClass.toString() + " is not supported yet")
    }
    private var firstVisibleItem = 0
    private var tempVisibleItem = 0
    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        super.onScrolled(recyclerView, dx, dy)
        tempVisibleItem = firstVisibleItemFinder.findFirstVisibleItemPosition()
        if (tempVisibleItem != firstVisibleItem) {
            onHeadItemIndexChanged(tempVisibleItem)
            firstVisibleItem = tempVisibleItem
        }
    }

    protected abstract fun onHeadItemIndexChanged(newIndex: Int)
    interface IFirstVisibleItemFinder {
        fun findFirstVisibleItemPosition(): Int
    }

}