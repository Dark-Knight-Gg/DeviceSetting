package vn.com.vti.common.adapter.segment

import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import vn.com.vti.common.adapter.Holder
import vn.com.vti.common.adapter.OnItemClick
import vn.com.vti.common.adapter.binding.BindingHolder

abstract class BindingSegmentHolder<BINDER : ViewDataBinding, MODEL> : Holder<MODEL> {
    private lateinit var segmentWrapper: BindingSegmentAdapter.Wrapper<MODEL>
    protected var binder: BINDER
        private set

    protected var segmentModel: MODEL? = null
        private set

    constructor(binder: BINDER) : super(binder.root) {
        this.binder = binder
    }

    constructor(parent: ViewGroup, @LayoutRes layoutResId: Int) : super(
        BindingHolder.inflateLayout(parent, layoutResId)
    ) {
        binder = DataBindingUtil.bind(itemView)!!
    }

    fun attachSegmentWrapper(wrapper: BindingSegmentAdapter.Wrapper<MODEL>) {
        segmentWrapper = wrapper
    }

    fun internalBind(position: Int) {
        val segmentPosition = position - segmentWrapper.startIndex
        segmentModel = segmentWrapper.segment.getItem(segmentPosition)
        onBind(position, segmentPosition, segmentModel)
    }

    abstract fun onBind(layoutPosition: Int, segmentPosition: Int, model: MODEL?)

    protected inner class DelegateOnClick(private val delegate: OnItemClick<MODEL>) :
        View.OnClickListener {
        override fun onClick(v: View?) {
            delegate.onItemClick(layoutPosition, v, segmentModel)
        }
    }
}