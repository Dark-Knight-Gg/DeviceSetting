package vn.com.vti.common.adapter.flex

import android.view.View
import android.view.ViewGroup
import androidx.annotation.CallSuper
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import vn.com.vti.common.adapter.Holder
import vn.com.vti.common.util.extension.inflateBinding

open class FlexibleHolder<VIEW : ViewDataBinding, MODEL>(
    parent: ViewGroup,
    @LayoutRes layoutResId: Int,
    protected val binder: Binder<VIEW, MODEL>,
) : Holder<MODEL>(
    (parent.inflateBinding<ViewDataBinding>(layoutResId).root)
) {

    protected lateinit var view: VIEW

    @CallSuper
    override fun onCreate() {
        super.onCreate()
        view = onInflateViewDataBinding(itemView)
        binder.onCreate(this, view)
    }

    override fun onBind(position: Int, model: MODEL?) {
        super.onBind(position, model)
        binder.onBind(this, view, model)
    }

    open fun onInflateViewDataBinding(root: View): VIEW {
        return DataBindingUtil.findBinding<VIEW>(root)!!
    }
}