package vn.com.vti.common.adapter.binding

import android.view.ViewGroup
import androidx.databinding.ViewDataBinding

@Suppress("unused")
abstract class BindingVariableHolder<BINDER : ViewDataBinding, MODEL> :
    BindingHolder<BINDER, MODEL?> {
    constructor(binder: BINDER) : super(binder)
    constructor(parent: ViewGroup, layoutResId: Int) : super(parent, layoutResId)

    protected abstract val bindingVariable: Int
    override fun onBind(position: Int, model: MODEL?) {
        super.onBind(position, model)
        binder.run {
            setVariable(bindingVariable, model)
            executePendingBindings()
        }
    }
}