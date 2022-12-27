package vn.com.vti.common.adapter.tree

import vn.com.vti.common.adapter.binding.BindingAdapter

@Suppress("MemberVisibilityCanBePrivate")
abstract class ExpandableMenuAdapter<T> : BindingAdapter<Node<T>>() {

    private var flattenNode: MutableList<Node<T>> = mutableListOf()

    override fun getItemCount() = flattenNode.size

    override fun getItem(position: Int) = flattenNode[position]

    protected fun expand(index: Int) {
        val node = flattenNode[index]
        if (node.getType() != Node.GROUP) {
            return
        }
        node.run {
            getFlatChildNode()?.let {
                flattenNode.addAll(index + 1, it)
                notifyItemRangeInserted(index + 1, it.size)
            }
            setFlatChildNode(null)
            notifyItemChanged(index)
        }
    }

    protected fun collapse(index: Int) {
        val node = flattenNode[index]
        if (node.getType() != Node.GROUP || index == itemCount - 1) {
            return
        }
        val level = node.getLevel()
        val start = index + 1
        var end = itemCount
        for (i in start until itemCount) {
            if (level >= flattenNode[i].getLevel()) {
                end = i
                break
            }
        }
        flattenNode.subList(start, end).let {
            if (it.isNotEmpty()) {
                node.setFlatChildNode(it.toMutableList())
                val count = it.size
                it.clear()
                notifyItemRangeRemoved(start, count)
                notifyItemChanged(index)
            }
        }
    }

    protected fun toggle(index: Int) {
        flattenNode[index].let {
            if (it.getType() != Node.GROUP) {
                return
            }
            if (it.isCollapsed()) {
                expand(index)
            } else {
                collapse(index)
            }
        }
    }

    fun setNodes(menu: List<Node<T>>) {
        if (itemCount > 0) notifyItemRangeRemoved(0, itemCount)
        flattenNode = menu.toMutableList()
        if (itemCount > 0) notifyItemRangeInserted(0, itemCount)
    }

    fun getData() = flattenNode

    fun findParentNodePosition(position: Int, nodeLevel: Int): Int {
        return (position downTo 0).firstOrNull {
            getItem(position).getLevel() == nodeLevel
        } ?: -1
    }

    fun findParentNode(position: Int, nodeLevel: Int): Node<T>? {
        return (position downTo 0).firstNotNullOfOrNull {
            getItem(it).let {
                if (it.getLevel() == nodeLevel) it
                else null
            }
        }
    }

    override fun clear() {
        super.clear()
        val size = flattenNode.size
        flattenNode.clear()
        if (size > 0)
            notifyItemRangeRemoved(0, size)
    }
}