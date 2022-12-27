package vn.com.vti.common.bindconverter

import android.text.InputFilter
import android.text.InputType
import android.widget.EditText
import androidx.appcompat.widget.AppCompatEditText
import androidx.databinding.BindingAdapter
import androidx.databinding.InverseMethod
import vn.com.vti.common.util.input.DecimalDigitsInputFilter
import vn.com.vti.common.util.input.IntegerDigitsInputFilter
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.*

object Precision {
    //float	4 bytes	Sufficient for storing 6 to 7 decimal digits, for more precision, you should use Double or BigDecimal
    const val INTEGER_PRECISION = 7
    const val FRACTION_PRECISION = 3

    const val MAX_INPUT_DOUBLE = 9_999_999.999
    const val MAX_INPUT_INT = 9_999_999
    const val MAX_INPUT_FLOAT = 9_999_999f

    fun defaultDecimalInputFilter(
        min: Double = -MAX_INPUT_DOUBLE, max: Double = MAX_INPUT_DOUBLE
    ): InputFilter = DecimalDigitsInputFilter(INTEGER_PRECISION, FRACTION_PRECISION, min, max)

    fun defaultIntegerInputFilter(
        min: Int = -MAX_INPUT_INT, max: Int = MAX_INPUT_INT
    ): InputFilter = IntegerDigitsInputFilter(INTEGER_PRECISION, min, max)
}

fun String?.fromCurrencyToInt(): Int? = this?.let {
    currencyFormat.parse(it)?.toInt()
}

fun String?.fromCurrencyToLong(): Long? = this?.let {
    currencyFormat.parse(it)?.toLong()
}

fun String.purifyNumberString(): String = replace("[^\\d]".toRegex(), "")

fun Int?.format(): String? = TextIntConverter.intToText(this)

fun Long?.formatCurrency(): String? = CurrencyConverter.longToCurrencyText(this)

fun Long?.format(): String? = TextLongConverter.longToText(this)

fun Float?.formatCurrency(): String? = CurrencyConverter.floatToCurrencyText(this)

fun Float?.format(): String? = TextFloatConverter.floatToText(this)

fun Double?.format(): String? = TextDoubleConverter.doubleToText(this)

object TextIntConverter {

    @InverseMethod("textToInt")
    @JvmStatic
    fun intToText(
        value: Int?,
    ): String? {
        return value?.toString()
    }

    @JvmStatic
    fun textToInt(
        value: String?,
    ): Int? {
        return value?.toIntOrNull()
    }
}

val currencyFormat = DecimalFormat().apply {
    maximumIntegerDigits = 30
    minimumFractionDigits = 0
    maximumFractionDigits = 2
    decimalFormatSymbols = DecimalFormatSymbols(Locale.JAPAN)
    isGroupingUsed = true
    groupingSize = 3
}

object CurrencyConverter {

    fun longToCurrencyText(
        value: Long?,
    ): String? {
        return value?.let {
            currencyFormat.format(it)
        }
    }

    fun floatToCurrencyText(
        value: Float?
    ): String? {
        return value?.let {
            currencyFormat.format(it)
        }
    }
}

object TextLongConverter {

    @InverseMethod("textToLong")
    @JvmStatic
    fun longToText(
        value: Long?,
    ): String? {
        return value?.toString()
    }

    @JvmStatic
    fun textToLong(
        value: String?,
    ): Long? {
        return value?.toLongOrNull()
    }
}

object TextFloatConverter {

    private val numberFormat = DecimalFormat().apply {
        maximumIntegerDigits = Precision.INTEGER_PRECISION
        minimumFractionDigits = 0
        maximumFractionDigits = 1
        isGroupingUsed = false
        decimalFormatSymbols = DecimalFormatSymbols(Locale.US)
    }

    @InverseMethod("textToFloat")
    @JvmStatic
    fun floatToText(
        value: Float?,
    ): String? {
        return value?.let {
            numberFormat.format(it.toBigDecimal())
        }
    }

    @JvmStatic
    fun textToFloat(
        value: String?,
    ): Float? {
        return value?.toFloatOrNull()
    }
}

object TextDoubleConverter {

    private val numberFormat = DecimalFormat().apply {
        maximumIntegerDigits = Precision.INTEGER_PRECISION
        minimumFractionDigits = 0
        maximumFractionDigits = Precision.FRACTION_PRECISION
        isGroupingUsed = false
        decimalFormatSymbols = DecimalFormatSymbols(Locale.US)
    }

    @InverseMethod("textToDouble")
    @JvmStatic
    fun doubleToText(
        value: Double?,
    ): String? {
        return value?.let {
            numberFormat.format(it)
        }
    }

    @JvmStatic
    fun textToDouble(
        value: String?,
    ): Double? {
        return value?.toDoubleOrNull()
    }

    @JvmStatic
    fun <T : Number> formatDouble(
        value: T?,
    ): String? {
        value ?: return null
        return numberFormat.format(value.toDouble())
    }
}

object TextBindingConverters {

    @BindingAdapter("enableInputDecimal")
    @JvmStatic
    fun AppCompatEditText.enableInputDecimal(enabled: Boolean?) {
        this.inputType = if (enabled == true) {
            InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
        } else {
            InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_NORMAL
        }
    }

    @BindingAdapter("numberInputDecimal")
    @JvmStatic
    fun AppCompatEditText.numberInputDecimal(enabled: Boolean?) {
        this.inputType = if (enabled == true) {
            this.filters = arrayOf(
                Precision.defaultDecimalInputFilter()
            )
            InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
        } else {
            this.filters = arrayOf(
                Precision.defaultIntegerInputFilter()
            )
            InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_NORMAL
        }
    }

    @BindingAdapter("bindInputFilter")
    @JvmStatic
    fun EditText.bindInputFilter(filter: InputFilter?) {
        this.filters = filter?.let {
            arrayOf(it)
        } ?: emptyArray()
    }

    @BindingAdapter(value = ["bindInputFilter", "android:maxLength"])
    @JvmStatic
    fun EditText.bindInputFilter(filter: InputFilter?, maxLength: Int?) {
        listOfNotNull(filter, maxLength?.let {
            InputFilter.LengthFilter(maxLength)
        }).toTypedArray().let {
            this.filters = it
        }
    }
}

