package vn.com.vti.common.util.input

import android.text.InputFilter
import android.text.Spanned
import android.text.TextUtils
import vn.com.vti.common.bindconverter.TextDoubleConverter
import java.util.regex.Pattern

class DecimalDigitsInputFilter(
    integerDigits: Int, fractionDigits: Int,
    private val min: Double = -Double.MAX_VALUE,
    private val max: Double = Double.MAX_VALUE
) : InputFilter {

    private val validationPattern: Pattern
    private val leadingZerosPattern: Pattern = Pattern.compile("^0{2,}")

    init {
        if (integerDigits < 1) {
            throw IllegalArgumentException("integerDigits cannot be less than 1")
        }
        validationPattern = Pattern.compile("-?\\d{0,$integerDigits}(\\.\\d{0,$fractionDigits})?")
    }

    override fun filter(
        source: CharSequence,
        start: Int,
        end: Int,
        dest: Spanned,
        dstart: Int,
        dend: Int
    ): CharSequence? {
        return if (source.isEmpty()) {
            val out = TextUtils.concat(
                dest.subSequence(0, dstart),
                dest.subSequence(dend, dest.length)
            )
            if (leadingZerosPattern.matcher(out).find()) dest.subSequence(dstart, dend)
            else if (validationPattern.matcher(out).matches()) {
                val number = TextDoubleConverter.textToDouble(out.toString())
                if (number == null || number >= min) null
                else dest.subSequence(dstart, dend)
            } else dest.subSequence(dstart, dend)
        } else {
            val out = TextUtils.concat(
                dest.subSequence(0, dstart),
                source.subSequence(start, end),
                dest.subSequence(dend, dest.length)
            )
            if (leadingZerosPattern.matcher(out).find()) ""
            else if (validationPattern.matcher(out).matches()) {
                val number = TextDoubleConverter.textToDouble(out.toString())
                if (number == null || number <= max) null
                else ""
            } else ""
        }
    }
}