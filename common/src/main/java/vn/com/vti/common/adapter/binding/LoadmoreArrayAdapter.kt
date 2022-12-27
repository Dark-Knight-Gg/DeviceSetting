package vn.com.vti.common.adapter.binding

import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import vn.com.vti.common.R
import vn.com.vti.common.databinding.ItemLoadingBinding

@Suppress("unused")
abstract class LoadmoreArrayAdapter<M> : BindingArrayAdapter<M> {
    private var isLoading = false

    constructor()
    constructor(data: MutableList<out M>?) : super(data)

    fun setLoading(loading: Boolean) {
        if (isLoading != loading) {
            if (!loading) {
                isLoading = false
                notifyItemChanged(itemCount - 1)
            } else if (getContentItemCount() > 0) {
                isLoading = true
                notifyItemChanged(itemCount - 1)
            }
        }
    }

    override fun getItemCount(): Int {
        return getContentItemCount() + 1
    }

    final override fun getItemViewType(position: Int): Int {
        return if (position == itemCount - 1) {
            VIEWTYPE_LOADING
        } else getContentItemViewType(position)
    }

    override fun getItem(position: Int): M? {
        return if (position == itemCount - 1) {
            null
        } else super.getItem(position)
    }

    final override fun onCreateHolder(
        parent: ViewGroup,
        viewType: Int,
    ): BindingHolder<out ViewDataBinding, M> {
        if (viewType == VIEWTYPE_LOADING) {
            return onCreateLoadingViewHolder(parent)
        }
        return onCreateContentViewHolder(parent, viewType)
    }

    abstract fun onCreateContentViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): BindingHolder<out ViewDataBinding, M>

    open fun getContentItemViewType(position: Int) = super.getItemViewType(position)

    open fun onCreateLoadingViewHolder(parent: ViewGroup): BindingHolder<out ViewDataBinding, M> {
        return Holder(parent)
    }

    override fun getContentItemCount(): Int {
        return super.getItemCount()
    }

    private inner class Holder(parent: ViewGroup) :
        BindingHolder<ItemLoadingBinding, M>(parent, R.layout.item_loading) {

        override fun onBind(position: Int, model: M?) {
            super.onBind(position, model)
            binder.apply {
                loading = isLoading
                executePendingBindings()
            }
        }
    }

    companion object {
        private const val VIEWTYPE_LOADING = 1000
    }
}