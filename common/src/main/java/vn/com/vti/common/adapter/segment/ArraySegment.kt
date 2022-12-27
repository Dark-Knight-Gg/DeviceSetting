@file:Suppress("unused")

package vn.com.vti.common.adapter.segment

class ArraySegment<E> @JvmOverloads constructor(private var items: MutableList<E> = mutableListOf()) :
    Segment<E> {

    override val count: Int
        get() = items.size

    override fun getItem(position: Int): E = items[position]

    fun setData(items: MutableList<E>?) {
        this.items = items ?: mutableListOf()
    }

    fun remove(position: Int) {
        items.removeAt(position)
    }

    fun remove(item: E): Int {
        val position = items.indexOf(item)
        if (position > 0) {
            items.remove(item)
        }
        return position
    }
}