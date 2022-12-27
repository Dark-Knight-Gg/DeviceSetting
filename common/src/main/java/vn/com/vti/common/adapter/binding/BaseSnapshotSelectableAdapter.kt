package vn.com.vti.common.adapter.binding

import vn.com.vti.common.adapter.OnItemClick

abstract class BaseSnapshotSelectableAdapter<M>(
    protected val multiSelection: Boolean = false,
) : SnapshotBindingArrayAdapter<M>() {

    var allItemSelectedListener: (Boolean) -> Unit = {}

    private val selectedItems: MutableSet<M> = mutableSetOf()
    fun getSelectedItems(): Set<M> = selectedItems

    fun notifySelectAllClicked(selectAll: Boolean) {
        if (selectAll && selectedItems.size != dataList.size) { //check select all and there be unselected item(s)
            selectedItems.addAll(dataList)
            notifyAllItemChanged()
        } else if (!selectAll && selectedItems.size == dataList.size) { //uncheck select all when all items are selected
            selectedItems.clear()
            notifyAllItemChanged()
        }
        //otherwise do nothing
    }

    protected val onItemSelected: OnItemClick<M> = OnItemClick { position, _, model ->
        model ?: return@OnItemClick
        if (!selectedItems.remove(model)) {
            selectedItems.add(model)
        }
        notifyItemChanged(position)
        allItemSelectedListener(selectedItems.size == dataList.size)
    }

    fun selectedItem(position: Int, model: M) {
        if (!selectedItems.contains(model)) {
            selectedItems.add(model)
            notifyItemChanged(position)
        }
    }

    fun updateSelectItem(position: Int, model: M) {
        if (selectedItems.contains(model)) {
            selectedItems.remove(model)
        } else {
            selectedItems.add(model)
        }
        notifyItemChanged(position)
    }

    fun clearSelected() {
        selectedItems.clear()
        notifyAllItemChanged()
    }
}