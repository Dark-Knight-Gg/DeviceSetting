package vn.com.vti.common.bindmethods

import androidx.databinding.BindingAdapter
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout

object SwipeRefreshBindingAdapter {
    @BindingAdapter("srRefreshing")
    @JvmStatic
    fun setRefreshLayoutRefreshing(
        swipeRefreshLayout: SwipeRefreshLayout,
        isRefreshing: Boolean
    ) {
        swipeRefreshLayout.isRefreshing = isRefreshing
    }

    @BindingAdapter("srRefreshListener")
    @JvmStatic
    fun setRefreshLayoutListener(
        swipeRefreshLayout: SwipeRefreshLayout,
        listener: SwipeRefreshLayout.OnRefreshListener?
    ) {
        swipeRefreshLayout.setOnRefreshListener(listener)
    }
}