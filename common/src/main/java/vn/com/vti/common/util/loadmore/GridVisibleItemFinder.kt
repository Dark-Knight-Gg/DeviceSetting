package vn.com.vti.common.util.loadmore

import androidx.recyclerview.widget.GridLayoutManager
import vn.com.vti.common.util.loadmore.HeadItemListener.IFirstVisibleItemFinder
import vn.com.vti.common.util.loadmore.LastItemListener.ILastVisibleItemFinder

internal class GridVisibleItemFinder(private val gridLayoutManager: GridLayoutManager) :
    ILastVisibleItemFinder, IFirstVisibleItemFinder {
    override fun findLastVisibleItemPosition(): Int {
        return gridLayoutManager.findLastVisibleItemPosition()
    }

    override fun findFirstVisibleItemPosition(): Int {
        return gridLayoutManager.findFirstVisibleItemPosition()
    }

}