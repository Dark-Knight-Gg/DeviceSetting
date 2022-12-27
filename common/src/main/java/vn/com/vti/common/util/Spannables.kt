package vn.com.vti.common.util

import android.graphics.Color
import android.os.Build
import android.os.Parcel
import android.text.SpannableStringBuilder
import android.text.style.AbsoluteSizeSpan
import android.text.style.LeadingMarginSpan
import android.text.style.QuoteSpan
import androidx.annotation.ColorInt
import androidx.core.text.inSpans

inline fun SpannableStringBuilder.quote(
    @ColorInt stripColor: Int = Color.TRANSPARENT,
    stripWidth: Int = 0,
    stripGap: Int = 60,
    builder: SpannableStringBuilder.() -> Unit,
): SpannableStringBuilder {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P)
        inSpans(QuoteSpan(stripColor, stripWidth, stripGap), builder)
    else {
        //did not work as expected
        val parcel = Parcel.obtain().apply {
            writeInts(stripColor, stripWidth, stripGap)
        }
        inSpans(QuoteSpan(parcel), builder).also {
            parcel.recycle()
        }
    }
}

inline fun SpannableStringBuilder.standardLeadingMargin(
    firstIndent: Int = 0,
    restIndent: Int = 0,
    builder: SpannableStringBuilder.() -> Unit,
): SpannableStringBuilder = inSpans(LeadingMarginSpan.Standard(firstIndent, restIndent), builder)

fun Parcel.writeInts(vararg value: Int) = value.forEach { writeInt(it) }

fun SpannableStringBuilder.breakParagraph(
    gap: Int = 15,
    dp: Boolean = true,
): SpannableStringBuilder = append("\r\n").inSpans(AbsoluteSizeSpan(gap, dp)) {
    append("\r\n")
}

inline fun <T> List<T>.joinToSpannable(
    builder: SpannableStringBuilder = SpannableStringBuilder(),
    separator: SpannableStringBuilder.() -> Unit = { breakParagraph() },
    action: SpannableStringBuilder.(T) -> Unit = { append(it.toString()) },
): SpannableStringBuilder {
    firstOrNull()?.let {
        builder.action(it)
        for (index in 1..lastIndex) {
            builder.separator()
            builder.action(this[index])
        }
    }
    return builder
}