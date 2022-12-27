package vn.com.vti.common.adapter.flex

import androidx.databinding.ViewDataBinding
import vn.com.vti.common.adapter.Holder

fun interface Binder<VIEW : ViewDataBinding, MODEL> {

    fun onCreate(holder: Holder<MODEL>, view: VIEW) {}

    fun onBind(holder: Holder<MODEL>, view: VIEW, model: MODEL?)
}