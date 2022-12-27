package vn.com.vti.common.adapter.flex

import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import vn.com.vti.common.adapter.BaseAdapter
import vn.com.vti.common.adapter.OnItemClick

abstract class FlexibleAdapter<M> :
    BaseAdapter<FlexibleHolder<out ViewDataBinding, M>>() {

    protected var mItemClickListener: OnItemClick<M>? = null

    override fun onCreateHolder(
        parent: ViewGroup,
        viewType: Int,
    ): FlexibleHolder<out ViewDataBinding, M> {
        val (layoutRes, binder) = onCreateBinder(viewType)
        return FlexibleHolder(parent, layoutRes, binder)
    }

    override fun onBindViewHolder(
        holder: FlexibleHolder<out ViewDataBinding, M>,
        position: Int,
    ) = holder.onBind(position, getItem(position))

    fun setItemClickListener(itemClick: OnItemClick<M>?) {
        mItemClickListener = itemClick
    }

    override fun clear() {}

    abstract fun getItem(position: Int): M

    abstract fun onCreateBinder(viewType: Int): Pair<Int, Binder<*, M>>
}