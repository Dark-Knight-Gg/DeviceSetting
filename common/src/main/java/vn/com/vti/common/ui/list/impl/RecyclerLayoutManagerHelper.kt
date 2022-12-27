package vn.com.vti.common.ui.list.impl

import android.content.Context
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import org.jetbrains.annotations.Contract

object RecyclerLayoutManagerHelper {

    const val VERT = RecyclerView.VERTICAL
    const val HORZ = RecyclerView.HORIZONTAL

    @Contract("!null, !null, !null -> new")
    @JvmStatic
    fun linear(
        context: Context,
        orientation: Int = VERT,
        reverse: Boolean = false
    ): RecyclerView.LayoutManager {
        return LinearLayoutManager(context, orientation, reverse)
    }

    @Contract("!null, !null, !null ,!null-> new")
    @JvmStatic
    fun grid(
        context: Context,
        spanCount: Int,
        orientation: Int = VERT,
        reverse: Boolean = false
    ): RecyclerView.LayoutManager = GridLayoutManager(context, spanCount, orientation, reverse)

    @Contract("!null, !null-> new")
    @JvmStatic
    fun staggedGrid(spanCount: Int, orientation: Int = VERT): RecyclerView.LayoutManager =
        StaggeredGridLayoutManager(spanCount, orientation)
}