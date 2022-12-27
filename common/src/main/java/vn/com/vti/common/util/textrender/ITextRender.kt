package vn.com.vti.common.util.textrender

import android.widget.TextView

interface ITextRender {
    fun apply(target: TextView, content: String?)
}