package vn.com.vti.common.adapter.binding

import android.view.View
import androidx.databinding.ViewDataBinding
import vn.com.vti.common.adapter.BaseAdapter
import vn.com.vti.common.adapter.OnItemClick

/**
 * Created by VTI Android Team on 3/29/2018.
 * Copyright © 2018 VTI Inc. All rights reserved.
 */
abstract class BindingAdapter<M> :
    BaseAdapter<BindingHolder<out ViewDataBinding, M>>() {
    protected var mItemClickListener: OnItemClick<M>? = null

    override fun onBindViewHolder(
        holder: BindingHolder<out ViewDataBinding, M>,
        position: Int
    ) = holder.onBind(position, getItem(position))

    protected fun setRootViewItemClick(target: BindingHolder<out ViewDataBinding, M>) {
        target.registerRootViewItemClickEvent(mItemClickListener)
    }

    fun setItemClickListener(itemClick: OnItemClick<M>?) {
        mItemClickListener = itemClick
    }

    fun setItemClickListener(listener: (position: Int, view: View?, model: M?) -> Unit) {
        mItemClickListener = OnItemClick(listener)
    }

    abstract fun getItem(position: Int): M?
}