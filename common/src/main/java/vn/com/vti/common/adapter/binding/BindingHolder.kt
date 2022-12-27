package vn.com.vti.common.adapter.binding

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import vn.com.vti.common.adapter.Holder

@Suppress("unused")
abstract class BindingHolder<BINDER : ViewDataBinding, MODEL> : Holder<MODEL> {
    protected var binder: BINDER
        private set

    constructor(binder: BINDER) : super(binder.root) {
        this.binder = binder
    }

    constructor(parent: ViewGroup, @LayoutRes layoutResId: Int) : super(
        inflateLayout(
            parent,
            layoutResId
        )
    ) {
        binder = DataBindingUtil.bind(itemView)!!
    }

    companion object {
        fun <BINDER : ViewDataBinding> inflateBinding(
            parent: ViewGroup,
            @LayoutRes layoutRes: Int
        ): BINDER {
            return DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                layoutRes,
                parent,
                false
            )
        }

        fun inflateLayout(parent: ViewGroup, @LayoutRes layoutRes: Int): View {
            return LayoutInflater.from(parent.context).inflate(layoutRes, parent, false)
        }
    }
}

val BindingHolder<*, *>.viewContext: Context get() = itemView.context
val ViewDataBinding.viewContext: Context get() = root.context