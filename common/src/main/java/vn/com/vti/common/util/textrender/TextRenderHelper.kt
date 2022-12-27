package vn.com.vti.common.util.textrender

import android.os.Build
import android.text.Html
import android.text.TextUtils
import android.widget.TextView

@Suppress("DEPRECATION")
object TextRenderHelper {
    fun create(@ContentType contentType: String): ITextRender {
        return when (contentType) {
            ContentType.TEXT_HTML -> object : ITextRender {
                override fun apply(target: TextView, content: String?) {
                    textAsHtml(target, content)
                }
            }
            else -> object : ITextRender {
                override fun apply(target: TextView, content: String?) {
                    target.text = content
                }
            }
        }
    }

    fun textAsHtml(textView: TextView, text: String?) {
        if (TextUtils.isEmpty(text)) textView.text = null
        else textView.text =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) Html.fromHtml(
                text,
                Html.FROM_HTML_MODE_LEGACY
            )
            else Html.fromHtml(text)

    }
}