package vn.com.vti.common.adapter.binding

import android.widget.Filter
import android.widget.Filterable

abstract class FilterableBindingArrayAdapter<M> : BindingArrayAdapter<M>(), Filterable {

    var original: List<M> = listOf()
        set(value) {
            field = value
            lastFilteredPattern = null
            setDataWithNotify(value)
        }

    private var lastFilteredPattern: String? = null

    @Suppress("UNCHECKED_CAST")
    protected open inner class BaseFilterable(
        private val predicate: (models: List<M>, constraint: CharSequence) -> List<M>,
        private val onFilterCompleted: (Int) -> Unit = {},
    ) : Filter() {

        override fun performFiltering(constraint: CharSequence?): FilterResults =
            FilterResults().apply {
                count = original.size
                if (original.isEmpty()) {
                    values = original
                } else {
                    val pattern = constraint?.toString()?.trim() ?: ""
                    values = predicate(original, pattern)
                }
            }

        override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
            val filtered = results?.let {
                it.values as? List<M>
            } ?: emptyList()
            setDataWithNotify(filtered)
            onFilterCompleted(itemCount)
        }

    }

    @Suppress("UNCHECKED_CAST")
    protected inner class CompatFilterable(
        private val predicate: (model: M, constraint: CharSequence) -> Boolean,
        private val onFilterCompleted: (Int) -> Unit = {},
    ) : Filter() {

        override fun performFiltering(constraint: CharSequence?): FilterResults =
            FilterResults().apply {
                count = original.size
                if (original.isEmpty()) {
                    values = original
                } else {
                    val pattern =
                        constraint?.toString()?.trim() ?: ""
                    if (pattern.isEmpty()) {
                        values = original
                    } else {
                        lastFilteredPattern = pattern
                        values = original.filter {
                            predicate(it, pattern)
                        }
                    }
                }
            }

        override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
            val filtered = results?.let {
                it.values as? List<M>
            } ?: emptyList()
            setDataWithNotify(filtered)
            onFilterCompleted(itemCount)
        }

    }
}