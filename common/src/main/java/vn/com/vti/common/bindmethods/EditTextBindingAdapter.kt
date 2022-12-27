package vn.com.vti.common.bindmethods

import android.text.Editable
import android.text.InputFilter
import android.text.InputFilter.LengthFilter
import android.text.Spanned
import android.text.TextWatcher
import android.text.method.TransformationMethod
import android.view.KeyEvent
import android.view.View
import android.view.View.OnFocusChangeListener
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.Filterable
import android.widget.ListAdapter
import android.widget.TextView
import androidx.appcompat.widget.AppCompatAutoCompleteTextView
import androidx.appcompat.widget.AppCompatEditText
import androidx.databinding.BindingAdapter
import androidx.databinding.InverseBindingAdapter
import androidx.databinding.InverseBindingListener
import androidx.databinding.adapters.ListenerUtil
import androidx.databinding.library.baseAdapters.R
import vn.com.vti.common.util.livedata.TransformLiveData
import vn.com.vti.common.util.runSafety
import kotlin.math.max


@Suppress("unused")
object EditTextBindingAdapter {

    @BindingAdapter("edtBindTextOnInputComplete")
    @JvmStatic
    fun bindOnInputComplete(
        editText: AppCompatEditText,
        liveData: TransformLiveData<String?, String?>?
    ) = liveData?.let {
        editText.onFocusChangeListener =
            object : InputCompletedByFocusChangeListener(editText.hasFocus()) {
                override fun onInputComplete() {
                    it.setSourceValue(editText.text?.toString())
                }
            }
    }

    @BindingAdapter("edtAutoCompleteAdapter")
    @JvmStatic
    fun <T> bindAutoCompleteAdapter(
        view: AppCompatAutoCompleteTextView,
        adapter: T
    ) where T : ListAdapter, T : Filterable {
        if (view.adapter != adapter)
            view.setAdapter(adapter)
    }

    @BindingAdapter("edtTransformationMethod")
    @JvmStatic
    fun edtTransformationMethod(
        editText: AppCompatEditText,
        transformation: TransformationMethod?
    ) {
        editText.transformationMethod = transformation
    }

    @BindingAdapter("edtOnDoneClearFocus")
    @JvmStatic
    fun onDoneClearFocus(
        editText: AppCompatEditText,
        clear: Boolean
    ) {
        editText.apply {
            if (clear) {
                imeOptions = EditorInfo.IME_ACTION_DONE
                setOnEditorActionListener { v: TextView, actionId: Int, _: KeyEvent? ->
                    if (actionId == EditorInfo.IME_ACTION_DONE) {
                        v.clearFocus()
                    }
                    false
                }
            } else {
                setOnEditorActionListener(null)
            }
        }
    }

    @BindingAdapter("edtMaxLength")
    @JvmStatic
    fun setEdtLength(
        editText: AppCompatEditText,
        length: Int
    ) {
        editText.apply {
            if (length > 0) {
                editText.filters = arrayOf<InputFilter>(
                    LengthFilter(length)
                )
            }
        }
    }

    private abstract class InputCompletedByFocusChangeListener(private var focusState: Boolean) :
        OnFocusChangeListener {
        override fun onFocusChange(
            v: View,
            hasFocus: Boolean,
        ) {
            if (hasFocus) {
                focusState = true
            } else if (focusState) {
                focusState = false
                onInputComplete()
            }
        }

        abstract fun onInputComplete()

    }
}

object TextViewTextChangedBindingAdapter {

    @BindingAdapter("bindTextWithSelection")
    @JvmStatic
    fun EditText.setTextWithSelection(newText: CharSequence?) {
        val oldText = text
        if (newText === oldText || newText == null && oldText.isEmpty()) {
            return
        }
        if (newText is Spanned) {
            if (newText == oldText) {
                return
            }
        } else if (!haveContentsChanged(newText, oldText)) {
            return
        }
        if (newText.isNullOrEmpty()) {
            setText(newText)
        } else {
            val oldStart = selectionStart
            val oldEnd = selectionEnd
            setText(newText)
            runSafety {
                val currentText = text?.toString()
                if (currentText.isNullOrEmpty()) return
                val offset = currentText.length - oldText.length
                val startSelection = max(0, oldStart + offset)
                val endSelection = max(0, oldEnd + offset)
                setSelection(startSelection, endSelection)
            }
        }
    }

    @InverseBindingAdapter(
        attribute = "bindTextWithSelection",
        event = "bindTextWithSelectionAttrChanged"
    )
    @JvmStatic
    fun EditText.getTextWithSelection(): String? = text?.toString()

    @BindingAdapter(
        value = ["bindTextWithSelectionAttrChanged"],
        requireAll = false
    )
    @JvmStatic
    fun setTextWatcher(
        view: EditText,
        textAttrChanged: InverseBindingListener?
    ) {
        val newValue: TextWatcher? = if (textAttrChanged == null) {
            null
        } else {
            object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                    textAttrChanged.onChange()
                }

                override fun afterTextChanged(s: Editable) {
                }
            }
        }
        val oldValue = ListenerUtil.trackListener(view, newValue, R.id.textWatcher)
        if (oldValue != null) {
            view.removeTextChangedListener(oldValue)
        }
        if (newValue != null) {
            view.addTextChangedListener(newValue)
        }
    }

    private fun haveContentsChanged(str1: CharSequence?, str2: CharSequence?): Boolean {
        if (str1 == null != (str2 == null)) {
            return true
        } else if (str1 == null) {
            return false
        }
        val length = str1.length
        if (length != str2!!.length) {
            return true
        }
        for (i in 0 until length) {
            if (str1[i] != str2[i]) {
                return true
            }
        }
        return false
    }
}