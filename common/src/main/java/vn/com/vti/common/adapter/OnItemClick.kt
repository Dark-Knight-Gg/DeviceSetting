package vn.com.vti.common.adapter

import android.view.View

fun interface OnItemClick<T> {
    fun onItemClick(position: Int, view: View?, t: T?)
}