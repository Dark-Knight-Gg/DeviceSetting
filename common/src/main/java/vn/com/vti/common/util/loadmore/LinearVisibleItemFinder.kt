package vn.com.vti.common.util.loadmore

import androidx.recyclerview.widget.LinearLayoutManager
import vn.com.vti.common.util.loadmore.HeadItemListener.IFirstVisibleItemFinder
import vn.com.vti.common.util.loadmore.LastItemListener.ILastVisibleItemFinder

internal class LinearVisibleItemFinder(private val linearLayoutManager: LinearLayoutManager) :
    ILastVisibleItemFinder, IFirstVisibleItemFinder {
    override fun findLastVisibleItemPosition(): Int {
        return linearLayoutManager.findLastVisibleItemPosition()
    }

    override fun findFirstVisibleItemPosition(): Int {
        return linearLayoutManager.findFirstVisibleItemPosition()
    }

}