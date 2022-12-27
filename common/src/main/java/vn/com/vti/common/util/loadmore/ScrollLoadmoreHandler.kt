package vn.com.vti.common.util.loadmore

import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import vn.com.vti.common.util.loadmore.LastItemListener.ILastVisibleItemFinder

class ScrollLoadmoreHandler(
    layoutManager: RecyclerView.LayoutManager,
    private val onLoadmoreListener: OnLoadmoreListener,
) : RecyclerView.OnScrollListener() {
    // The minimum amount of items to have below your current scroll position
    // before loading more.
    private var visibleThreshold = 2

    // The total number of items in the dataset after the last load
    private var previousTotalItemCount = 0

    // True if we are still waiting for the last set of data to load.
    private var loading = true

    private val mLayoutManager: RecyclerView.LayoutManager = layoutManager
    private val lastVisibleItemFinder: ILastVisibleItemFinder = when (layoutManager) {
        is GridLayoutManager -> {
            visibleThreshold *= layoutManager.spanCount
            GridVisibleItemFinder(layoutManager)
        }
        is LinearLayoutManager -> LinearVisibleItemFinder(layoutManager)
        is StaggeredGridLayoutManager -> {
            visibleThreshold *= layoutManager.spanCount
            StaggedVisibleItemFinder(layoutManager)
        }
        else -> throw UnsupportedOperationException(layoutManager.javaClass.toString() + " is not supported yet")
    }

    // This happens many times a second during a scroll, so be wary of the code you place here.
    // We are given a few useful parameters to help us work out if we need to load some more data,
    // but first we check if we are waiting for the previous load to finish.
    override fun onScrolled(view: RecyclerView, dx: Int, dy: Int) {
        val totalItemCount = mLayoutManager.itemCount
        val lastVisibleItemPosition = lastVisibleItemFinder.findLastVisibleItemPosition()
        if (totalItemCount < previousTotalItemCount) {
            previousTotalItemCount = totalItemCount
            if (totalItemCount == 0) {
                loading = true
            }
        }
        if (loading && totalItemCount > previousTotalItemCount) {
            loading = false
            previousTotalItemCount = totalItemCount
        }
        if (!loading && lastVisibleItemPosition + visibleThreshold > totalItemCount) {
            onLoadmoreListener.onLoadmore(totalItemCount, view)
            loading = true
        }
    }

    @Suppress("unused")
    fun resetState() {
        previousTotalItemCount = 0
        loading = false
    }

    fun interface OnLoadmoreListener {
        fun onLoadmore(totalItemsCount: Int, view: RecyclerView)
    }

}