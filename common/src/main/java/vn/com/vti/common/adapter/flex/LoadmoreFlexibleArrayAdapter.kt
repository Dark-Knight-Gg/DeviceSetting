package vn.com.vti.common.adapter.flex

import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import vn.com.vti.common.util.livedata.NonNullMutableLiveData

abstract class LoadmoreFlexibleArrayAdapter<M> : FlexibleArrayAdapter<M> {

    private val liveLoading = NonNullMutableLiveData(false)

    constructor()
    constructor(data: MutableList<out M>?) : super(data)

    fun setLoading(loading: Boolean) {
        if (liveLoading.value != loading && itemCount > 0) {
            liveLoading.value = loading
            val position = itemCount - 1
            post {
                notifyItemChanged(position)
            }
        }
    }

    final override fun getItemViewType(position: Int): Int {
        return if (liveLoading.value && position == itemCount - 1) {
            VIEWTYPE_LOADING
        } else getContentItemViewType(position)
    }

    override fun onCreateHolder(
        parent: ViewGroup,
        viewType: Int,
    ): FlexibleHolder<out ViewDataBinding, M> {
        val (layoutRes, binder) = onCreateBinder(viewType)
        return when (viewType) {
            VIEWTYPE_LOADING -> LoadmoreFlexibleHolder(parent, layoutRes, binder, liveLoading)
            else -> FlexibleHolder(parent, layoutRes, binder)
        }
    }

    override fun isSupportedLoadmoreHolder() = true

    open fun getContentItemViewType(position: Int) = super.getItemViewType(position)

    companion object {
        private const val VIEWTYPE_LOADING = 1000
    }
}