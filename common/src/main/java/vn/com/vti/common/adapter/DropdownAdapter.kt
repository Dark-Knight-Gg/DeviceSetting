package vn.com.vti.common.adapter

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.AutoCompleteTextView
import android.widget.Filter
import android.widget.Filterable
import androidx.databinding.BindingAdapter
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.flow.MutableStateFlow
import vn.com.vti.common.R
import vn.com.vti.common.adapter.binding.BindingHolder
import vn.com.vti.common.databinding.ItemSimpleDropdownBinding
import vn.com.vti.common.model.UiText
import vn.com.vti.common.viewmodel.collectLatestOnLifecycle
import vn.com.vti.common.viewmodel.repeatOnLifecycleScope

@Suppress("unused")
abstract class DropdownAdapter<MODEL> : android.widget.BaseAdapter() {

    final override fun getItem(position: Int) = getLabel(position)

    override fun getItemId(position: Int) = position.toLong()

    @Suppress("UNCHECKED_CAST")
    final override fun getDropDownView(
        position: Int,
        _convertView: View?,
        parent: ViewGroup,
    ): View {
        val holder: BindingHolder<*, MODEL?>
        var convertView = _convertView
        if (convertView == null) {
            holder = onCreateDropdownViewBinding(parent)
            convertView = holder.itemView
            convertView.tag = holder
        } else {
            holder = convertView.tag as BindingHolder<*, MODEL?>
        }
        holder.onBind(position, getModel(position))
        return convertView
    }

    @Suppress("UNCHECKED_CAST")
    final override fun getView(
        position: Int,
        _convertView: View?,
        parent: ViewGroup,
    ): View {
        val holder: BindingHolder<*, MODEL?>
        var convertView = _convertView
        if (convertView == null) {
            holder = onCreateViewBinding(parent)
            convertView = holder.itemView
            convertView.tag = holder
        } else {
            holder = convertView.tag as BindingHolder<*, MODEL?>
        }
        holder.onBind(position, getModel(position))
        return convertView
    }

    protected open fun onCreateDropdownViewBinding(parent: ViewGroup): BindingHolder<out ViewDataBinding, MODEL?> =
        onCreateViewBinding(parent)

    protected abstract fun onCreateViewBinding(parent: ViewGroup): BindingHolder<out ViewDataBinding, MODEL?>

    protected abstract fun getModel(position: Int): MODEL

    protected open fun getLabel(position: Int): String? = getModel(position).toString()
}

abstract class AutoCompleteTextAdapter<T>(
    data: List<T> = listOf(),
    filterPredicate: ((T, CharSequence) -> Boolean) = { _, _ -> true },
    private val onItemClick: ((position: Int, item: T) -> Unit)? = null,
) : DropdownAdapter<T>(), Filterable, AdapterView.OnItemClickListener {

    private val mFilter = ArrayFilter(predicate = filterPredicate, onFiltered = {
        if (count == 0) notifyDataSetInvalidated()
        else notifyDataSetChanged()
    }).apply {
        original = data
    }

    open fun setData(data: List<T>?) {
        mFilter.apply {
            original = data ?: emptyList()
        }
    }

    override fun getCount(): Int = mFilter.filteredSize

    override fun getModel(position: Int): T = mFilter[position]

    override fun getFilter(): Filter = mFilter

    override fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        onItemClick?.invoke(position, getModel(position))
    }
}

class ArrayFilter<T>(
    private val predicate: (T, CharSequence) -> Boolean,
    private val onFiltered: () -> Unit = {},
) : Filter() {

    private var lastConstraints: CharSequence? = null

    var original: List<T> = listOf()
        set(value) {
            field = value
            filter(lastConstraints)
        }

    private var filtered: List<T> = listOf()

    val filteredSize: Int
        get() = filtered.size

    val originalSize: Int
        get() = original.size

    operator fun get(index: Int): T = filtered[index]

    override fun performFiltering(constraint: CharSequence?): FilterResults {
        val result = FilterResults()
        lastConstraints = constraint
        if (constraint.isNullOrEmpty()) {
            result.count = original.size
            result.values = original.toList()
        } else {
            original.filter {
                predicate(it, constraint)
            }.let {
                result.count = it.size
                result.values = it
            }
        }
        return result
    }

    override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
        filtered = (results?.values?.let {
            @Suppress("UNCHECKED_CAST") (it as? List<T>)
        }) ?: listOf()
        onFiltered()
    }
}

open class SimpleDropdownAdapter<T>(
    data: List<T> = emptyList(),
    filterPredicate: ((T, CharSequence) -> Boolean) = { _, _ -> true },
    onItemClick: ((position: Int, item: T) -> Unit)? = null,
    private val holderSupplier: (parent: ViewGroup) -> BindingHolder<out ViewDataBinding, T?> = { parent ->
        DefaultHolder(
            parent
        ) { it?.toString() }
    },
    private val labelSupplier: (item: T?) -> String? = { it?.toString() },
) : AutoCompleteTextAdapter<T>(data, filterPredicate, onItemClick) {

    override fun onCreateViewBinding(parent: ViewGroup): BindingHolder<out ViewDataBinding, T?> =
        holderSupplier(parent)

    override fun getLabel(position: Int): String? = labelSupplier(getModel(position))
}

class DefaultHolder<T>(
    parent: ViewGroup,
    private val selectionPredicate: (T) -> Boolean = { false },
    private val writer: (T) -> String?,
) : BindingHolder<ItemSimpleDropdownBinding, T?>(parent, R.layout.item_simple_dropdown) {

    override fun onBind(position: Int, model: T?) {
        super.onBind(position, model)
        binder.apply {
            if (model != null) {
                selected = selectionPredicate(model)
                text = writer(model)
            } else {
                selected = false
                text = null
            }
            executePendingBindings()
        }
    }
}

object DropdownBindingAdapter {

    @JvmStatic
    @BindingAdapter("autoCompleteTextValue")
    fun AutoCompleteTextView.bindAutoCompleteTextValue(
        text: CharSequence?,
    ) {
        setText(text, false)
        if (isFocused) {
            setSelection(text?.length ?: 0)
        }
    }

    @BindingAdapter(
        value = ["autoCompleteTextAdapter", "autoCompleteTextListener"], requireAll = true
    )
    @JvmStatic
    fun AutoCompleteTextView.bindAutoCompleteText(
        adapter: AutoCompleteTextAdapter<*>?,
        listener: AdapterView.OnItemClickListener?,
    ) {
        setAdapter(adapter)
        onItemClickListener = listener
    }
}

interface ISingleChoiceContainer {

    fun getContext(): Context

    fun onAttachAdapter(
        adapter: AutoCompleteTextAdapter<*>,
        callback: AdapterView.OnItemClickListener,
    )

    fun onUpdateContent(value: String?)

}

class DropdownBindingFlow<T>(
    private val binding: ISingleChoiceContainer,
    owner: LifecycleOwner,
    data: List<T> = emptyList(),
    filter: ((T, CharSequence) -> Boolean) = { _, _ -> true },
    private val defaultValue: T,
    private val emiter: MutableStateFlow<T>,
    private val writer: ((T?) -> String?) = { it?.toString() },
    private val selectionPredicate: (T) -> Boolean = { emiter.value === it },
    holderSupplier: (parent: ViewGroup) -> BindingHolder<out ViewDataBinding, T?> = { parent ->
        DefaultHolder(
            parent = parent, selectionPredicate = selectionPredicate, writer = writer
        )
    }
) {

    private val adapter: SimpleDropdownAdapter<T> = SimpleDropdownAdapter(data = data,
        filterPredicate = filter,
        holderSupplier = holderSupplier,
        labelSupplier = writer,
        onItemClick = { _, item ->
            emiter.value = item
        })

    init {
        owner.repeatOnLifecycleScope {
            binding.onAttachAdapter(adapter, adapter)
            emiter.collectLatestOnLifecycle(owner) {
                binding.onUpdateContent(writer(it))
            }
        }
    }

    fun setData(data: List<T>?) {
        if (data.isNullOrEmpty()) {
            adapter.setData(emptyList())
            emiter.value = defaultValue
        } else {
            val old = emiter.value
            adapter.setData(data)
            if (old != null) {
                emiter.value = data.firstOrNull { it == old } ?: defaultValue
            }
        }
    }
}

class AutoCompleteBindingFlow<T>(
    private val binding: ISingleChoiceContainer,
    owner: LifecycleOwner,
    data: List<T> = emptyList(),
    filter: ((T, CharSequence) -> Boolean) = { _, _ -> true },
    private val emiter: MutableStateFlow<T>,
    private val originalContent: MutableStateFlow<String>,
    private val writer: ((T?) -> String?) = { it?.toString() },
    private val selectionPredicate: (T) -> Boolean = { emiter.value === it },
    holderSupplier: (parent: ViewGroup) -> BindingHolder<out ViewDataBinding, T?> = { parent ->
        DefaultHolder(
            parent = parent, selectionPredicate = selectionPredicate, writer = writer
        )
    }
) {

    private val adapter: SimpleDropdownAdapter<T> = SimpleDropdownAdapter(data = data,
        filterPredicate = filter,
        holderSupplier = holderSupplier,
        labelSupplier = writer,
        onItemClick = { _, item ->
            emiter.value = item
            (writer(item) ?: "").let {
                originalContent.value = it
                binding.onUpdateContent(it)
            }
        })

    init {
        owner.repeatOnLifecycleScope {
            binding.onAttachAdapter(adapter, adapter)
            binding.onUpdateContent(writer(emiter.value))
        }
    }

    fun setData(data: List<T>?) {
        adapter.setData(data)
    }
}

interface IStringResEnum {

    val label: UiText

}