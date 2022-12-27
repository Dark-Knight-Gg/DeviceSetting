package vn.com.vti.common.adapter.flex

import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.LiveData
import vn.com.vti.common.R
import vn.com.vti.common.databinding.ItemLoadingWrapperBinding
import vn.com.vti.common.util.extension.inflateBinding
import vn.com.vti.common.util.extension.unbox

class LoadmoreFlexibleHolder<VIEW : ViewDataBinding, MODEL>(
    parent: ViewGroup,
    @LayoutRes contentLayoutResId: Int,
    binder: Binder<VIEW, MODEL>,
    @Suppress("MemberVisibilityCanBePrivate") val liveLoading: LiveData<Boolean>,
) : FlexibleHolder<VIEW, MODEL>(parent, R.layout.item_loading_wrapper, binder) {

    private val rootBinding: ItemLoadingWrapperBinding = DataBindingUtil.getBinding(itemView)!!

    init {
        rootBinding.container.inflateBinding<ViewDataBinding>(contentLayoutResId, true)
    }

    override fun onBind(position: Int, model: MODEL?) {
        super.onBind(position, model)
        rootBinding.loading.visibility = if (liveLoading.unbox()) View.VISIBLE else View.GONE
    }

    override fun onInflateViewDataBinding(root: View): VIEW {
        return DataBindingUtil.findBinding<VIEW>(rootBinding.container.getChildAt(0))!!
    }
}