package vn.com.vti.common.adapter.segment

import android.view.ViewGroup
import androidx.collection.SparseArrayCompat
import vn.com.vti.common.adapter.BaseAdapter

class BindingSegmentAdapter : BaseAdapter<BindingSegmentHolder<*, *>>() {
    private val registeredWrapper: MutableList<Wrapper<*>> = mutableListOf()
    private val mActiveSegmentWrapper: SparseArrayCompat<Wrapper<*>> =
        SparseArrayCompat()
    private var itemCount = 0

    override fun getItemCount() = itemCount
    private var viewTypeFinder: ViewTypeFinder? = null
    override fun getItemViewType(position: Int): Int {
        return viewTypeFinder?.getItemViewType(position) ?: 0
    }

    override fun onCreateHolder(parent: ViewGroup, viewType: Int): BindingSegmentHolder<*, *> {
        val wrapper =
            mActiveSegmentWrapper.get(viewType)
                ?: throw IllegalArgumentException("No wrapper associated with this view type $viewType")
        return wrapper.createViewHolder(parent)
    }

    override fun onBindViewHolder(holder: BindingSegmentHolder<*, *>, position: Int) {
        holder.internalBind(position)
    }

    fun getWrapperOfPosition(position: Int): Wrapper<*>? {
        return viewTypeFinder?.getItemViewType(position)?.let {
            mActiveSegmentWrapper[it]
        }
    }

    fun <E> registerSegment(
        viewType: Int,
        segment: Segment<E>,
        createViewHolderFunction: (ViewGroup) -> BindingSegmentHolder<*, E>,
    ) {
        registeredWrapper.add(
            Wrapper(
                0,
                viewType,
                segment,
                createViewHolderFunction
            )
        )
    }

    fun invalidateSegments() {
        var index = 0
        var segment: Segment<*>
        var count: Int
        mActiveSegmentWrapper.clear()
        val activeWrapper: MutableList<Wrapper<*>> = mutableListOf()
        for (wrapper in registeredWrapper) {
            segment = wrapper.segment
            count = segment.count
            if (count > 0) {
                wrapper.startIndex = index
                mActiveSegmentWrapper.put(wrapper.viewType, wrapper)
                activeWrapper.add(wrapper)
                index += count
            }
        }
        itemCount = index
        val size = activeWrapper.size
        viewTypeFinder = when {
            size > 3 -> {
                ViewTypeFinderImpl(activeWrapper)
            }
            size > 2 -> {
                TripleViewTypeFinder(activeWrapper)
            }
            size > 1 -> {
                DoubleViewTypeFinder(activeWrapper)
            }
            else -> {
                SingleViewTypeFinder(activeWrapper)
            }
        }
    }

    class Wrapper<E>(
        var startIndex: Int, val viewType: Int, val segment: Segment<E>,
        private val createViewHolderFunction: (ViewGroup) -> BindingSegmentHolder<*, E>,
    ) {

        fun createViewHolder(viewGroup: ViewGroup): BindingSegmentHolder<*, E> =
            createViewHolderFunction.invoke(viewGroup).apply {
                attachSegmentWrapper(this@Wrapper)
            }
    }

    private interface ViewTypeFinder {
        fun getItemViewType(position: Int): Int
    }

    private class SingleViewTypeFinder(wrappers: MutableList<Wrapper<*>>) :
        ViewTypeFinder {

        private val viewType: Int = if (wrappers.size > 0) wrappers[0].viewType else 0

        override fun getItemViewType(position: Int): Int {
            return viewType
        }

    }

    private class DoubleViewTypeFinder(activeWrapper: MutableList<Wrapper<*>>) :
        ViewTypeFinder {
        private val firstViewType: Int = activeWrapper[0].viewType
        private val secondViewType: Int = activeWrapper[1].viewType
        private val firstIndex: Int = activeWrapper[1].startIndex
        override fun getItemViewType(position: Int): Int {
            return if (position < firstIndex) firstViewType else secondViewType
        }

    }

    private class TripleViewTypeFinder(activeWrapper: MutableList<Wrapper<*>>) :
        ViewTypeFinder {
        private val firstViewType: Int = activeWrapper[0].viewType
        private val secondViewType: Int = activeWrapper[1].viewType
        private val thirdViewType: Int = activeWrapper[2].viewType
        private val firstIndex: Int = activeWrapper[1].startIndex
        private val secondIndex: Int = activeWrapper[2].startIndex
        override fun getItemViewType(position: Int): Int {
            return if (position < firstIndex) firstViewType else if (position >= secondIndex) thirdViewType else secondViewType
        }

    }

    class ViewTypeFinderImpl : ViewTypeFinder {
        private var index: IntArray
        private var viewTypes: IntArray

        internal constructor(activeWrapper: MutableList<Wrapper<*>>) {
            index = IntArray(activeWrapper.size)
            viewTypes = IntArray(activeWrapper.size)
            for (i in activeWrapper.indices) {
                index[i] = activeWrapper[i].startIndex
                viewTypes[i] = activeWrapper[i].viewType
            }
        }

        constructor(index: IntArray, viewTypes: IntArray) {
            this.index = index
            this.viewTypes = viewTypes
            require(!(index.size != viewTypes.size || index.size <= 3)) { "must equals size and larger than 3" }
        }

        override fun getItemViewType(position: Int): Int {
            var start: Int
            var end: Int
            if (position == 0) {
                return viewTypes[0]
            }
            if (position >= index[index.lastIndex]) {
                return viewTypes.last()
            }
            start = 0
            end = index.lastIndex
            var temp: Int
            while (end - start > 1) {
                temp = start + end shr 1
                index[temp].let {
                    when {
                        position > it -> start = temp
                        position < it -> end = temp
                        else -> return@getItemViewType viewTypes[temp]
                    }
                }

            }
            return start
        }
    }
}