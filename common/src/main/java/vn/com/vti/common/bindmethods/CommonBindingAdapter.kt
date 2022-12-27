package vn.com.vti.common.bindmethods

import android.graphics.Paint
import android.text.InputType
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatSpinner
import androidx.databinding.BindingAdapter
import androidx.databinding.InverseBindingAdapter
import androidx.databinding.InverseBindingListener
import vn.com.vti.common.util.animation.IVisibilityAnimation

@Suppress("unused")
object CommonBindingAdapter {

    @BindingAdapter("viewCompatSelected")
    @JvmStatic
    fun View.viewCompatSelected(selected: Boolean) {
        isSelected = selected
    }

    @BindingAdapter("viewCompatVisibility")
    @JvmStatic
    fun View.viewCompatVisibility(isVisible: Boolean) {
        visibility = if (isVisible) View.VISIBLE else View.GONE
    }

    @BindingAdapter("viewCompatVisibilityInvisible")
    @JvmStatic
    fun View.viewCompatVisibilityInvisible(isVisible: Boolean) {
        visibility = if (isVisible) View.VISIBLE else View.INVISIBLE
    }

    @BindingAdapter("strikeThrough")
    @JvmStatic
    fun strikeThrough(textView: TextView, strikeThrough: Boolean) {
        if (strikeThrough) {
            textView.paintFlags = textView.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
        } else {
            textView.paintFlags = textView.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
        }
    }

    @BindingAdapter("viewCompatEnabled")
    @JvmStatic
    fun View.viewCompatEnabled(enabled: Boolean) {
        isEnabled = enabled
        isClickable = enabled
    }

    @BindingAdapter("viewCompatDisable")
    @JvmStatic
    fun viewCompatDisable(view: View, disable: Boolean) {
        view.apply {
            isEnabled = !disable
            isClickable = !disable
        }
    }

    @BindingAdapter("changeVisibilityAnimation", "android:visibility")
    @JvmStatic
    fun changeVisibilityWithAnimation(
        view: View,
        animation: IVisibilityAnimation,
        visibility: Int
    ) {
        if (view.visibility == visibility) {
            return
        }
        view.clearAnimation()
        if (visibility == View.VISIBLE) {
            animation.onAnimateShow(view)
        } else {
            animation.onAnimateHide(view, visibility)
        }
    }

    @BindingAdapter("changeVisibilityAnimation", "viewCompatVisibility")
    @JvmStatic
    fun changeVisibilityWithAnimation(
        view: View,
        animation: IVisibilityAnimation,
        isVisible: Boolean
    ) = changeVisibilityWithAnimation(view, animation, if (isVisible) View.VISIBLE else View.GONE)

    @BindingAdapter("onLoadingAnimation")
    @JvmStatic
    fun onLoadingAnimation(imageView: AppCompatImageView, isLoading: Boolean) {
        if (isLoading) {
            imageView.animate().rotationBy(360f).setDuration(800).start()
        }
    }

    @BindingAdapter("edtPasswordVisibility")
    @JvmStatic
    fun passwordVisibility(
        editText: AppCompatEditText,
        enabled: Boolean
    ) {
        editText.apply {
            inputType =
                InputType.TYPE_CLASS_TEXT or if (enabled) InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD else InputType.TYPE_TEXT_VARIATION_PASSWORD
            text?.let {
                setSelection(it.length)
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    @BindingAdapter(value = ["selectedValue", "selectedValueAttrChanged"], requireAll = false)
    @JvmStatic
    fun bindSpinnerData(
        pAppCompatSpinner: AppCompatSpinner,
        newSelectedValue: String?,
        newTextAttrChanged: InverseBindingListener
    ) {
        pAppCompatSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                newTextAttrChanged.onChange()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
        if (newSelectedValue != null) {
            val pos =
                (pAppCompatSpinner.adapter as? ArrayAdapter<String?>)?.getPosition(newSelectedValue)
            pAppCompatSpinner.setSelection(pos ?: 0, true)
        }
    }

    @InverseBindingAdapter(attribute = "selectedValue", event = "selectedValueAttrChanged")
    fun captureSelectedValue(pAppCompatSpinner: AppCompatSpinner): String {
        return pAppCompatSpinner.selectedItem as String
    }
}