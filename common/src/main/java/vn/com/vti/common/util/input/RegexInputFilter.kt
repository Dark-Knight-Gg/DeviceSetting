package vn.com.vti.common.util.input

import android.text.InputFilter
import android.text.Spanned
import android.text.TextUtils

class RegexInputFilter(private val regex: Regex) : InputFilter {

    override fun filter(
        source: CharSequence,
        start: Int,
        end: Int,
        dest: Spanned,
        dstart: Int,
        dend: Int,
    ): CharSequence? {
        return if (source.isEmpty()) {
            val out = TextUtils.concat(
                dest.subSequence(0, dstart),
                dest.subSequence(dend, dest.length)
            )
            if (regex.matches(out)) null
            else dest.subSequence(dstart, dend)
        } else {
            val out = TextUtils.concat(
                dest.subSequence(0, dstart),
                source.subSequence(start, end),
                dest.subSequence(dend, dest.length)
            )
            if (regex.matches(out)) null
            else ""
        }
    }

}

fun inputFilterByCharacter(vararg chars: Char): InputFilter =
    chars.joinToString(separator = "").let {
        if (it.isEmpty()) throw IllegalArgumentException("Characters must be not empty")
        else "^[$it]*$".toRegex()
    }.let {
        RegexInputFilter(it)
    }

fun inputOnlyAlphabetAndNumeric(): InputFilter = RegexInputFilter("^[a-zA-Z0-9]*$".toRegex())