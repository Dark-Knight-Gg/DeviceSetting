package vn.com.vti.common.bindmethods

import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.telephony.PhoneNumberUtils
import android.text.*
import android.text.style.ForegroundColorSpan
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.AutoCompleteTextView
import android.widget.TextView
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.SearchView
import androidx.core.text.PrecomputedTextCompat
import androidx.core.text.color
import androidx.core.widget.TextViewCompat
import androidx.databinding.BindingAdapter
import androidx.databinding.InverseBindingAdapter
import androidx.databinding.InverseBindingListener
import androidx.lifecycle.MutableLiveData
import com.google.android.material.textfield.TextInputLayout
import vn.com.vti.common.model.UiText
import vn.com.vti.common.util.extension.DateTimeXs.toTimeString
import vn.com.vti.common.util.extension.dismissKeyboard
import vn.com.vti.common.util.extension.distinctSetValue
import vn.com.vti.common.util.textrender.ITextRender
import vn.com.vti.common.util.textrender.TextRenderHelper
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.*


object TextBindingAdapter {

    @BindingAdapter("textStyleCompat")
    @JvmStatic
    fun TextView.setTextStyleCompat(textStyle: Int?) {
        typeface = Typeface.defaultFromStyle(textStyle ?: Typeface.NORMAL)
    }

    @BindingAdapter("textHtml")
    @JvmStatic
    fun textAsHtml(textView: TextView, text: String?) {
        TextRenderHelper.textAsHtml(textView, text)
    }

    @BindingAdapter("textCompat")
    @JvmStatic
    fun renderTextToTextView(
        textView: TextView, content: Int?
    ) {
        content?.let {
            if (it == 0x0) {
                textView.text = ""
            } else textView.setText(content)
        } ?: kotlin.run {
            textView.text == null
        }
    }

    @BindingAdapter("textRender", "android:text")
    @JvmStatic
    fun renderTextToTextView(
        textView: TextView, render: ITextRender, content: String?
    ) {
        render.apply(textView, content)
    }

    @BindingAdapter("textPhoneNumber")
    @JvmStatic
    fun TextView.textAsPhoneNumber(phoneNumber: String?) {
        text = if (phoneNumber.isNullOrEmpty()) null
        else {
            val locale = resources.configuration.locales.get(0)
            PhoneNumberUtils.formatNumber(
                phoneNumber, locale.country
            )?.takeIf { it.isNotEmpty() } ?: phoneNumber
        }
    }

    @BindingAdapter(value = ["fxStrikethrough", "fxUnderline"], requireAll = false)
    @JvmStatic
    fun textEffect(
        view: TextView, strikethrough: Boolean, underline: Boolean
    ) {
        view.paintFlags.let {
            if (strikethrough) it or Paint.STRIKE_THRU_TEXT_FLAG else it and Paint.STRIKE_THRU_TEXT_FLAG.inv()
        }.let {
            if (underline) it or Paint.UNDERLINE_TEXT_FLAG else it and Paint.UNDERLINE_TEXT_FLAG.inv()
        }.let {
            view.paintFlags = it
        }
    }

    @BindingAdapter(value = ["textNumberFormatter", "textNumberValue"])
    @JvmStatic
    fun textAsFormattedNumber(
        textView: TextView, numberFormat: NumberFormat?, number: Long
    ) {
        textView.text = numberFormat?.format(number)
    }

    @BindingAdapter(value = ["textNumberFormatter", "textNumberValue"])
    @JvmStatic
    fun textAsFormattedNumber(
        textView: TextView, numberFormat: NumberFormat?, number: Float
    ) {
        textView.text = numberFormat?.format(number.toDouble())
    }

    @BindingAdapter(value = ["textNumberFormatter", "textNumberValue"])
    @JvmStatic
    fun textAsFormattedNumber(
        textView: TextView, numberFormat: NumberFormat?, number: Int
    ) {
        textView.text = numberFormat?.format(number.toDouble())
    }

    @BindingAdapter("searchQueryAttrChanged")
    @JvmStatic
    fun SearchView.setSearchQueryListeners(attrChange: InverseBindingListener) {
        setOnQueryTextListener(object : SearchView.OnQueryTextListener {

            override fun onQueryTextSubmit(query: String?): Boolean {
                attrChange.onChange()
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                attrChange.onChange()
                return true
            }

        })
    }

    @BindingAdapter("textAsDateTime", "textAsDateTimePattern")
    @JvmStatic
    fun TextView.textAsDateTime(millis: Long?, pattern: String?) {
        text = if (millis == null || pattern.isNullOrEmpty()) {
            null
        } else {
            millis.toTimeString(pattern)
        }
    }

    @BindingAdapter("searchQuery")
    @JvmStatic
    fun SearchView.setSearchQuery(value: String?) {
        if (value != getSearchQuery()) {
            setQuery(value, false)
        }
    }

    @InverseBindingAdapter(attribute = "searchQuery", event = "searchQueryAttrChanged")
    @JvmStatic
    fun SearchView.getSearchQuery() = query.toString()

    @BindingAdapter(value = ["android:hint", "obligatory"], requireAll = true)
    @JvmStatic
    fun TextInputLayout.setTextObligatory(hintText: String?, obligatory: Boolean = false) {
        hint = if (obligatory && !hintText.isNullOrEmpty()) {
            val value = "$hintText *"
            val spannable: Spannable = SpannableString(value)
            spannable.setSpan(
                ForegroundColorSpan(Color.RED),
                value.lastIndex,
                value.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            spannable
        } else {
            hintText
        }
    }

    @BindingAdapter(value = ["android:text", "obligatory"], requireAll = true)
    @JvmStatic
    fun AppCompatTextView.setTextObligatory(hintText: String?, obligatory: Boolean = false) {
        text = if (obligatory && !hintText.isNullOrEmpty()) {
            SpannableStringBuilder().apply {
                append(hintText)
                append(" ")
                color(Color.RED) {
                    append("*")
                }
            }
        } else {
            hintText
        }
    }

    @BindingAdapter(value = ["errorText", "focusWhenError"], requireAll = false)
    @JvmStatic
    fun TextInputLayout.setTextError(errorText: String?, focusWhenError: Boolean?) {
        error = errorText
        if (!errorText.isNullOrEmpty() && focusWhenError == true) {
            requestFocus()
        }
    }

    @BindingAdapter(value = ["errorText", "focusWhenError"], requireAll = false)
    @JvmStatic
    fun TextInputLayout.setTextError(errorText: UiText?, focusWhenError: Boolean?) {
        error = errorText?.getBy(context)
        if (errorText != null && focusWhenError == true) {
            requestFocus()
        }
    }

    @BindingAdapter("compatMaxLength")
    @JvmStatic
    fun TextView.compatMaxLength(maxLength: Int?) {
        if (filters.isNullOrEmpty()) {
            if (maxLength != null && maxLength > 0) filters =
                arrayOf(InputFilter.LengthFilter(maxLength))
        } else {
            filters = mutableListOf<InputFilter>().apply {
                addAll(filters)
                removeIf {
                    it is InputFilter.LengthFilter
                }
                if (maxLength != null && maxLength > 0) add(InputFilter.LengthFilter(maxLength))
            }.toTypedArray()
        }
    }

    @BindingAdapter("bindImeOptions")
    @JvmStatic
    fun AutoCompleteTextView.bindImeOptions(imeOptions: Int?) {
        if (imeOptions == null) {
            setOnEditorActionListener(null)
            setImeOptions(EditorInfo.IME_ACTION_UNSPECIFIED)
        } else {
            setImeOptions(imeOptions)
            setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    dismissDropDown()
                }
                false
            }
        }
    }

    @BindingAdapter("bindImeOptions")
    @JvmStatic
    fun AppCompatEditText.bindImeOptions(imeOptions: Int?) {
        if (imeOptions == null) {
            setOnEditorActionListener(null)
            setImeOptions(EditorInfo.IME_ACTION_UNSPECIFIED)
        } else {
            setImeOptions(imeOptions)
            setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    dismissKeyboard()
                }
                false
            }
        }
    }

    @BindingAdapter(value = ["android:hint", "hintTextDisable", "hintTextByInputType"])
    @JvmStatic
    fun AppCompatEditText.compatHintText(
        hintContent: String?, disabled: Boolean?, inputType: Int?
    ) {
        hint = if (true == disabled || InputType.TYPE_NULL == inputType) {
            null
        } else hintContent
    }

    @BindingAdapter("bindInputType")
    @JvmStatic
    fun AppCompatEditText.bindInputType(type: Int?) {
        if (type == InputType.TYPE_NULL) {
            clearFocus()
            isFocusable = false
            isFocusableInTouchMode = false
            isClickable = false
        } else {
            isFocusable = true
            isFocusableInTouchMode = true
            isClickable = true
        }
        inputType = type ?: InputType.TYPE_CLASS_TEXT
    }

    @BindingAdapter("bindTextChangeListener")
    @JvmStatic
    fun AppCompatEditText.bindTextChangeListener(textWatcher: TextWatcher?) {
        textWatcher?.let { addTextChangedListener(it) }
    }

    @BindingAdapter("bindSingleChoiceAllowTyping")
    @JvmStatic
    fun AutoCompleteTextView.bindSingleChoiceAllowTyping(allow: Boolean?) {
        inputType = if (true == allow) {
            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
        } else InputType.TYPE_NULL
    }

    @BindingAdapter("bindOnClickClearText")
    @JvmStatic
    fun View.bindOnClickClearText(source: MutableLiveData<String>?) {
        if (source == null) {
            setOnClickListener(null)
        } else {
            setOnClickListener {
                source.distinctSetValue("")
            }
        }
    }

    @BindingAdapter(
        value = ["textFromDate", "textFromDatePattern", "textFromDateFormat"], requireAll = true
    )
    @JvmStatic
    fun TextView.setTextFromDate(millis: Long?, pattern: String?, prefix: String?) {
        text = when {
            (millis == null || pattern.isNullOrEmpty()) -> null
            else -> millis.toTimeString(pattern).let {
                if (prefix.isNullOrEmpty()) it
                else String.format(Locale.getDefault(), prefix, it)
            }
        }
    }

    @BindingAdapter(
        value = ["textAsTimeDuration"], requireAll = true
    )
    @JvmStatic
    fun TextView.textAsTimeDuration(millis: Long?) {
        text = millis?.let {
            val inSeconds = millis / 1000
            val hrs = inSeconds / 3600
            val mins = (inSeconds % 3600) / 60
            val seconds = inSeconds % 60
            String.format("%1\$d:%2\$02d:%3\$02d", hrs, mins, seconds)
        }
    }

    /**
     * Refer to https://developer.android.com/reference/androidx/core/text/PrecomputedTextCompat
     *
     * Utitlity method for set precomputed text to TextView using [AppCompatTextView.setTextFuture]
     *
     *  - When to use:
     *      - TextView has long text. Especially TextView in [androidx.recyclerview.widget.RecyclerView]
     *  (If your [RecyclerView] uses custom [RecyclerView.LayoutManager], ensure that the layoutmanager supports RecyclerView's Prefetch
     *  - Notice:
     *      - TextView styling (Fontsize, TextStyle) must be called before this function
     *
     */
    @BindingAdapter(
        value = ["precomputedText"]
    )
    @JvmStatic
    fun AppCompatTextView.setPrecomputedTextFuture(content: CharSequence?) {
        if (content.isNullOrEmpty()) text = content
        else setTextFuture(
            PrecomputedTextCompat.getTextFuture(
                content, TextViewCompat.getTextMetricsParams(this), null
            )
        )
    }

    @BindingAdapter("formatMoney")
    @JvmStatic
    fun formatMoney(
        lblValue: AppCompatEditText,
        highLightText: Boolean
    ) {
        lblValue.addTextChangedListener(onTextChangedListener(lblValue))
    }

    private fun onTextChangedListener(editText: AppCompatEditText): TextWatcher? {
        return object : TextWatcher {
            override fun beforeTextChanged(
                s: CharSequence,
                start: Int,
                count: Int,
                after: Int
            ) {
            }

            override fun onTextChanged(
                s: CharSequence,
                start: Int,
                before: Int,
                count: Int
            ) {
            }

            override fun afterTextChanged(s: Editable) {
                editText.removeTextChangedListener(this)
                try {
                    val originalString = s.toString().replace(",".toRegex(), "")
                    val longVal: Long = originalString.toLong()
                    val formatter: DecimalFormat =
                        NumberFormat.getInstance(Locale.US) as DecimalFormat
//                    formatter.applyPattern(FORMAT_MONEY_DEFAULT)
                    val formattedString: String = formatter.format(longVal)
                    //setting text after format to EditText
                    editText.setText(formattedString)
                    editText.text?.let {
                        editText.setSelection(it.length)
                    }
                } catch (nfe: NumberFormatException) {
                    nfe.printStackTrace()
                }
                editText.addTextChangedListener(this)
            }
        }
    }
}