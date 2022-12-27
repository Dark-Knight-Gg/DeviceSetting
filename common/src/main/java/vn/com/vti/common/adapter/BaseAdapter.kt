package vn.com.vti.common.adapter

import android.view.ViewGroup
import androidx.annotation.CallSuper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.AdapterDataObserver
import vn.com.vti.common.adapter.itf.OnDataChangedListener
import vn.com.vti.common.util.weak

abstract class BaseAdapter<HOLDER : Holder<*>> :
    RecyclerView.Adapter<HOLDER>() {
    private var dataChangedListener: OnDataChangedListener? = null
    private var dataObserver: AdapterDataObserver? = null
    protected var recyclerViewWeakRef: RecyclerView? by weak()

    fun setDataChangedListener(listener: OnDataChangedListener?) {
        this.dataChangedListener = listener
        if (listener == null) {
            dataObserver?.let {
                unregisterAdapterDataObserver(it)
                dataObserver = null
            }
        } else if (dataObserver == null) {
            initDataObserver()
        }
    }

    private fun initDataObserver() {
        dataObserver = object : AdapterDataObserver() {
            override fun onChanged() {
                super.onChanged()
                dataChangedListener?.run {
                    if (itemCount == 0) {
                        onDataSetEmpty()
                    } else {
                        onDataSetFilled()
                    }
                }
            }

            override fun onItemRangeInserted(
                positionStart: Int,
                itemCount: Int,
            ) {
                super.onItemRangeInserted(positionStart, itemCount)
                dataChangedListener?.run {
                    if (itemCount > 0) {
                        onDataSetFilled()
                    } else if (getContentItemCount() == 0) {
                        onDataSetEmpty()
                    }
                }
            }

            override fun onItemRangeRemoved(
                positionStart: Int,
                itemCount: Int,
            ) {
                super.onItemRangeRemoved(positionStart, itemCount)
                dataChangedListener?.run {
                    if (getContentItemCount() == 0) {
                        onDataSetEmpty()
                    }
                }
            }
        }.also {
            registerAdapterDataObserver(it)
        }
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        recyclerViewWeakRef = recyclerView
        if (dataChangedListener != null) {
            if (dataObserver == null) {
                initDataObserver()
            }
        }
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        recyclerViewWeakRef = null
        dataObserver?.let {
            unregisterAdapterDataObserver(it)
            dataObserver = null
        }
    }

    final override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HOLDER {
        return onCreateHolder(parent, viewType).also {
            it.onCreate()
        }
    }

    override fun onViewAttachedToWindow(holder: HOLDER) {
        super.onViewAttachedToWindow(holder)
        holder.onAttach()
    }

    @CallSuper
    override fun onViewDetachedFromWindow(holder: HOLDER) {
        super.onViewDetachedFromWindow(holder)
        holder.onDetach()
    }

    override fun onViewRecycled(holder: HOLDER) {
        super.onViewRecycled(holder)
        holder.onClear()
    }

    fun post(action: Runnable) {
        recyclerViewWeakRef?.post(action)
    }

    abstract fun onCreateHolder(parent: ViewGroup, viewType: Int): HOLDER

    /**
     * Clear adapter data
     */
    open fun clear() {
        //Derived classes implements to clear adapter data
    }

    open fun isSupportedLoadmoreHolder() = false

    open fun getContentItemCount(): Int = itemCount

    protected fun Holder<*>.selfNotifyChanged() {
        bindingAdapterPosition.takeIf { it > RecyclerView.NO_POSITION }
            ?.let { notifyItemChanged(it) }
    }

    fun notifyAllItemChanged() {
        if (itemCount == 0) return
        else notifyItemRangeChanged(0, itemCount)
    }
}