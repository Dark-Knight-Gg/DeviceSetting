package vn.com.vti.common.adapter.tree

import androidx.annotation.IntDef
import androidx.annotation.IntRange

class Node<T>(
    private val type: Int = GROUP,
    private val level: Int = 0,
    private val data: T,
    private var childNode: List<Node<T>>? = null,
    private var isLoaded: Boolean = false,
    private var isWarning: Boolean = false,
) {

    @Type
    fun getType() = type

    @IntRange(from = 0)
    fun getLevel() = level

    fun getFlatChildNode(): List<Node<T>>? = childNode

    fun setFlatChildNode(nodes: List<Node<T>>?) {
        this.childNode = nodes
    }

    fun setLoaded(state: Boolean) {
        this.isLoaded = state
    }

    fun isLoaded() = this.isLoaded

    fun isWarning() = isWarning

    fun isCollapsed() = getFlatChildNode()?.isNotEmpty() ?: false

    fun getData(): T = data

    @Target(AnnotationTarget.FUNCTION, AnnotationTarget.VALUE_PARAMETER)
    @IntDef(GROUP, ITEM)
    @Retention(AnnotationRetention.SOURCE)
    annotation class Type

    companion object {
        const val GROUP = 0
        const val ITEM = 1
    }

}