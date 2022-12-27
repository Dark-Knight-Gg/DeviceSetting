package vn.com.vti.common.adapter

import android.view.LayoutInflater
import android.view.View
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.annotation.IdRes
import androidx.core.view.children
import androidx.databinding.BindingAdapter
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.flow.MutableStateFlow
import vn.com.vti.common.viewmodel.collectLatestOnLifecycle

abstract class RadioAdapter {

    private var onCheckedChangedListener: OnRadioCheckedChangeListener? = null

    private var childIds = ArrayList<Int>()

    var selectedPosition: Int? = null

    abstract fun getItemCount(): Int

    abstract fun onCreateRadioButton(
        parent: RadioGroup,
        inflater: LayoutInflater,
        position: Int
    ): View

    fun attachToRadioGroup(radioGroup: RadioGroup) {
        childIds.clear()
        radioGroup.run {
            clearCheck()
            removeAllViews()
            val inflater = LayoutInflater.from(context)
            (0 until getItemCount()).forEach {
                val button = onCreateRadioButton(this, inflater, it)
                View.generateViewId().let { id ->
                    button.id = id
                    childIds.add(id)
                }
                addView(button)
            }
            setOnCheckedChangeListener { group, checkedId ->
                onCheckedChangedListener?.run {
                    childIds.indexOf(checkedId).let {
                        if (it >= 0) {
                            onRadioCheckedChanged(group, checkedId, it)
                        }
                    }
                }
            }
            selectedPosition?.let {
                if (it >= 0 && it < getItemCount())
                    radioGroup.check(childIds[it])
            }
        }
    }

    fun setOnCheckedChangedListener(listener: OnRadioCheckedChangeListener?) {
        onCheckedChangedListener = listener
    }

    fun interface OnRadioCheckedChangeListener {

        fun onRadioCheckedChanged(group: RadioGroup, @IdRes checkedId: Int, position: Int)
    }
}

object RadioBindingAdapter {

    @BindingAdapter("radioGroupAdapter")
    @JvmStatic
    fun setRadioGroupAdapter(radioGroup: RadioGroup, radioAdapter: RadioAdapter?) {
        radioAdapter?.attachToRadioGroup(radioGroup) ?: radioGroup.run {
            clearCheck()
            removeAllViews()
        }
    }
}

fun <T> RadioGroup.bind(
    owner: LifecycleOwner,
    emiter: MutableStateFlow<T>,
    mapper: Map<Int, T>
) {
    emiter.collectLatestOnLifecycle(owner) { t ->
        val selectedId =
            mapper.entries.find { t == it.value }?.key ?: return@collectLatestOnLifecycle
        if (checkedRadioButtonId == selectedId) return@collectLatestOnLifecycle
        check(selectedId)
    }
    setOnCheckedChangeListener { _, checkedId ->
        val value = mapper[checkedId] ?: return@setOnCheckedChangeListener
        if (value == emiter.value) return@setOnCheckedChangeListener
        emiter.value = value
    }
}

fun RadioGroup.buttonClickable(enable: Boolean) {
    children.forEach {
        if (it is RadioButton)
            it.isClickable = enable
    }
}