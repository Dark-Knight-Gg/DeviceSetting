package vn.com.vti.common.adapter.segment

class SingleSegment<E>(private var element: E? = null) : Segment<E> {

    override val count: Int
        get() = if (element != null) 1 else 0

    override fun getItem(position: Int): E? = element

}