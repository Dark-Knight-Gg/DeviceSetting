package vn.com.vti.common.util.textrender

import androidx.annotation.StringDef

@StringDef(
    ContentType.TEXT_PLAIN,
    ContentType.TEXT_HTML
)
@Retention(AnnotationRetention.SOURCE)
annotation class ContentType {
    companion object {
        const val TEXT_PLAIN = "text/plain"
        const val TEXT_HTML = "text/html"
    }
}