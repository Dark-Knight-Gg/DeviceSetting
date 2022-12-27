package vn.com.vti.common.adapter.segment

import androidx.annotation.IntRange

interface Segment<E> {
    @get:IntRange(from = 0)
    val count: Int

    fun getItem(position: Int): E?
}