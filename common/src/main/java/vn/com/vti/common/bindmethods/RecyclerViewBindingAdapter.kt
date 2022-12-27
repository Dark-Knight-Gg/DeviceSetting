package vn.com.vti.common.bindmethods

import android.view.View
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration
import androidx.recyclerview.widget.SnapHelper
import vn.com.vti.common.adapter.BaseAdapter
import vn.com.vti.common.adapter.itf.OnDataChangedListener
import vn.com.vti.common.util.loadmore.ScrollLoadmoreHandler

object RecyclerViewBindingAdapter {
    @BindingAdapter("rvSnapHelper")
    @JvmStatic
    fun setRecyclerViewAdapter(recyclerView: RecyclerView, helper: SnapHelper?) {
        helper?.attachToRecyclerView(recyclerView)
    }

    @BindingAdapter("rvAdapter")
    @JvmStatic
    fun setRecyclerViewAdapter(
        recyclerView: RecyclerView,
        adapter: RecyclerView.Adapter<*>?
    ) {
        recyclerView.adapter = adapter
    }

    @BindingAdapter(value = ["rvLayoutManager", "rvLoadmoreListener"])
    @JvmStatic
    fun setRecyclerViewLayoutManager(
        recyclerView: RecyclerView,
        layoutManager: RecyclerView.LayoutManager?,
        onLoadmoreListener: ScrollLoadmoreHandler.OnLoadmoreListener?
    ) {
        recyclerView.apply {
            setLayoutManager(layoutManager)
            clearOnScrollListeners()
            if (layoutManager != null && onLoadmoreListener != null) {
                addOnScrollListener(
                    ScrollLoadmoreHandler(
                        layoutManager,
                        onLoadmoreListener
                    )
                )
            }
        }
    }

    @BindingAdapter(value = ["rvDecoration"])
    @JvmStatic
    fun setRecyclerViewDecoration(
        recyclerView: RecyclerView,
        decoration: ItemDecoration?
    ) {
        decoration?.let { itemDeco ->
            recyclerView.run {
                this.adapter?.let {
                    if (it.itemCount <= 2) removeItemDecoration(itemDeco)
                    else {
                        removeItemDecoration(itemDeco)
                        addItemDecoration(itemDeco)
                    }
                }
            }
        }
    }

    @BindingAdapter("rvItemTouchHelper")
    @JvmStatic
    fun setItemTouchHelper(
        recyclerView: RecyclerView,
        itemTouchHelper: ItemTouchHelper?
    ) {
        itemTouchHelper?.attachToRecyclerView(recyclerView)
    }

    @BindingAdapter("rvEmptyLabel")
    @JvmStatic
    fun setVisibilityByRecycler(
        view: View,
        adapter: BaseAdapter<*>?
    ) {
        if (adapter == null) {
            view.visibility = View.GONE
        } else {
            adapter.setDataChangedListener(object : OnDataChangedListener {

                override fun onDataSetEmpty() {
                    view.visibility = View.VISIBLE
                }

                override fun onDataSetFilled() {
                    view.visibility = View.GONE
                }

            })
        }
    }
}