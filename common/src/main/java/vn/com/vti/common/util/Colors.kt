package vn.com.vti.common.util

import android.graphics.Color
import androidx.annotation.ColorInt
import timber.log.Timber

fun String?.asColor(fallback: String) =
    runCatching {
        Color.parseColor(this ?: fallback)
    }.getOrElse {
        Timber.w("Cannot parse color from $this")
        Color.parseColor(fallback)
    }

fun String?.asColor(@ColorInt fallback: Int) =
    runCatching {
        Color.parseColor(this)
    }.getOrElse {
        Timber.w("Cannot parse color from $this")
        fallback
    }

fun Int.asRgbString() = String.format("#%06X", 0xFFFFFF and this)