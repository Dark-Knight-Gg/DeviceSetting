@file:Suppress("unused")

package vn.com.vti.common.adapter.binding

import vn.com.vti.common.util.notNullOrEmptyLet
import kotlin.math.min

abstract class BindingArrayAdapter<M> @JvmOverloads constructor(data: List<M>? = null) :
    BindingAdapter<M>() {

    var dataList: MutableList<M> = mutableListOf()
        protected set

    init {
        data?.let {
            dataList.addAll(it)
        }
    }

    override fun getItem(position: Int): M? = dataList[position]

    override fun getItemCount() = dataList.size

    open fun addData(data: Collection<M>) {
        val startOffset = itemCount
        if (data.isEmpty()) {
            notifyItemRangeInserted(startOffset, 0)
        } else {
            if (startOffset > 0) {
                notifyItemChanged(startOffset - 1)
            }
            dataList.addAll(data)
            notifyItemRangeInserted(startOffset, data.size)
        }
    }

    open fun addData(start: Int, data: Collection<M>) {
        val startOffset = min(start, dataList.size)
        if (data.isEmpty()) {
            notifyItemRangeInserted(startOffset, 0)
        } else {
            dataList.addAll(start, data)
            notifyItemRangeInserted(startOffset, data.size)
        }
    }

    open fun addData(data: M) {
        dataList.add(data)
        notifyItemInserted(itemCount - 1)
    }

    open fun addData(position: Int, data: M) {
        dataList.add(position, data)
        notifyItemInserted(position)
    }

    open fun update(index: Int, newItem: M) {
        if (index >= 0 && index < dataList.size) {
            dataList[index] = newItem
            notifyItemChanged(index)
        }
    }

    open fun remove(index: Int) {
        if (index >= 0 && index < dataList.size) {
            dataList.removeAt(index)
            notifyItemRemoved(index)
        }
    }

    open fun remove(item: M) {
        val index = dataList.indexOf(item)
        if (index >= 0) {
            dataList.removeAt(index)
            notifyItemRemoved(index)
        }
    }

    inline fun removeFirst(crossinline predicate: (M) -> Boolean) {
        val index = dataList.indexOfFirst(predicate)
        if (index >= 0) remove(index)
    }

    open fun remove(start: Int, end: Int) {
        if (start in 0..end && end < dataList.size) {
            dataList.subList(start, end + 1).clear()
            notifyItemRangeRemoved(start, end - start + 1)
        }
    }

    override fun clear() {
        val size = itemCount
        dataList.clear()
        notifyItemRangeRemoved(0, size)
    }

    fun getData(): List<M> = dataList

    open fun setData(data: Collection<M>?) {
        dataList.apply {
            clear()
            data.notNullOrEmptyLet {
                this.addAll(it)
            }
        }
    }

    open fun setDataWithNotify(data: Collection<M>?) {
        dataList.apply {
            val count = size
            if (size > 0) {
                clear()
                notifyItemRangeRemoved(0, count)
            }
            data.notNullOrEmptyLet {
                this.addAll(it)
                if (size > 0)
                    notifyItemRangeInserted(0, size)
            }
        }
    }
}