package vn.com.vti.common.util.extension

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.annotation.FloatRange
import androidx.annotation.IntRange
import androidx.annotation.LayoutRes
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.R
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

fun <T : ViewDataBinding> ViewGroup.inflateBinding(
    @LayoutRes layoutResId: Int,
    attachedToRoot: Boolean = false,
): T = LayoutInflater.from(context).let {
    DataBindingUtil.inflate(it, layoutResId, this, attachedToRoot)
}

fun <T : ViewDataBinding> LayoutInflater.inflateBinding(
    @LayoutRes layoutResId: Int,
    parent: ViewGroup? = null,
    attachedToRoot: Boolean = false,
): T = DataBindingUtil.inflate(this, layoutResId, parent, attachedToRoot)

fun View.padding(
    @IntRange(from = 0) left: Int = this.paddingLeft,
    @IntRange(from = 0) top: Int = this.paddingTop,
    @IntRange(from = 0) right: Int = this.paddingRight,
    @IntRange(from = 0) bottom: Int = this.paddingBottom
) {
    setPadding(left, top, right, bottom)
}

fun View.paddingRelative(
    @IntRange(from = 0) start: Int = this.paddingStart,
    @IntRange(from = 0) top: Int = this.paddingTop,
    @IntRange(from = 0) end: Int = this.paddingEnd,
    @IntRange(from = 0) bottom: Int = this.paddingBottom
) {
    setPaddingRelative(start, top, end, bottom)
}

fun RecyclerView.removeAllItemDecorations() {
    ((itemDecorationCount - 1) downTo 0).forEach { removeItemDecorationAt(it) }
}

var ViewDataBinding.visibility: Int
    get() = root.visibility
    set(value) {
        root.visibility = value
    }

var ViewDataBinding.isEnabled: Boolean
    get() = root.isEnabled
    set(value) {
        root.isEnabled = value
    }

fun BottomSheetDialogFragment.setupFullscreen() {
    setupPercentage(1.0f)
}

fun BottomSheetDialogFragment.setupInset(
    @FloatRange(from = 0.0) insets: Float
) {
    dialog?.apply {
        val bottomSheet = findViewById<View>(R.id.design_bottom_sheet)
        bottomSheet.layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
    }
    view?.run {
        post {
            val parent = parent as View
            val params = parent.layoutParams as CoordinatorLayout.LayoutParams
            (params.behavior as? BottomSheetBehavior<*>)?.let {
                val peekHeight = (measuredHeight - insets).toInt()
                it.peekHeight = peekHeight
                params.height = peekHeight
            }
        }
    }
}

fun BottomSheetDialogFragment.setupWrapContent() {
    dialog?.apply {
        val bottomSheet = findViewById<View>(R.id.design_bottom_sheet)
        bottomSheet.layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
        bottomSheet.background = ColorDrawable(Color.TRANSPARENT)
    }
    view?.run {
        post {
            (dialog as? BottomSheetDialog)?.behavior?.let {
                it.skipCollapsed = true
                it.isFitToContents = true
            }
        }
    }
}

fun BottomSheetDialogFragment.setupPercentage(
    @FloatRange(from = 0.0, to = 1.0) heightPercentage: Float
) {
    dialog?.apply {
        val bottomSheet = findViewById<View>(R.id.design_bottom_sheet)
        bottomSheet.layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
        bottomSheet.background = ColorDrawable(Color.TRANSPARENT)
    }
    view?.run {
        post {
            (dialog as? BottomSheetDialog)?.behavior?.let {
                val parent = parent as View
                val params = parent.layoutParams as ViewGroup.LayoutParams
                val windowsSize = resources.displayMetrics.heightPixels
                val peekHeight = (windowsSize * heightPercentage).toInt()
                it.peekHeight = peekHeight
                params.height = peekHeight
            }
        }
    }
}

fun View.setOnConsecutiveClickListener(
    @IntRange(from = 1) times: Int = 7, debounce: Long = 1_000L, onClick: (View) -> Unit
) {
    setOnClickListener(object : View.OnClickListener {

        private var lastClicked = 0L
        private var count = 0

        override fun onClick(v: View?) {
            val current = System.currentTimeMillis()
            if (current - lastClicked <= debounce) {
                lastClicked = current
                if (++count == times) {
                    count = 0
                    onClick(this@setOnConsecutiveClickListener)
                }
            } else {
                lastClicked = current
                count = 0
            }
        }

    })
}

fun View.dismissKeyboard(): Unit {
    (context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager)?.run {
        hideSoftInputFromWindow(windowToken, 0)
    }
}