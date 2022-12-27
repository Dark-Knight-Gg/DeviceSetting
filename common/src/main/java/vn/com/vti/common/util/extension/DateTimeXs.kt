package vn.com.vti.common.util.extension

import android.text.TextUtils
import org.jetbrains.annotations.Contract
import timber.log.Timber
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

@Suppress("unused", "MemberVisibilityCanBePrivate")
object DateTimeXs {

    const val FORMAT_DATE_TIME_RFC_822 = "yyyy-MM-dd'T'HH:mm:ss'Z'"
    const val FORMAT_DATE_TIME_ISO_8601 = "yyyy-MM-dd'T'HH:mm:ssXXX"
    const val FORMAT_DATE_TIME_ISO = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"
    const val FORMAT_DATE_TIME_ISO_LOCAL = "yyyy-MM-dd'T'HH:mm:ss.SSSZ"

    const val SECONDS: Long = 1000
    const val MINUTES = SECONDS * 60
    const val HOURS = MINUTES * 60
    const val DAY = HOURS * 24
    const val WEEK = DAY * 7
    const val YEAR = DAY * 365

    const val TIMEZONE_UTC = "UTC"

    val TIMEZONE_UTC_INSTANT: TimeZone = TimeZone.getTimeZone(TIMEZONE_UTC)

    @Contract("_ -> new")
    fun getSimpleDateFormat(pattern: String) = SimpleDateFormat(pattern, Locale.getDefault())

    @Contract("_ -> new")
    fun getSimpleDateFormat(pattern: String, timeZone: String?) =
        SimpleDateFormat(pattern, Locale.getDefault()).apply {
            if (timeZone.isNullOrEmpty()) {
                return@apply
            }
            this.timeZone = TimeZone.getTimeZone(timeZone)
        }

    fun String.toDate(pattern: String, timeZone: String? = null): Date? {
        return if (TextUtils.isEmpty(this)) {
            null
        } else try {
            getSimpleDateFormat(pattern, timeZone).parse(this)
        } catch (e: ParseException) {
            Timber.e("parse date exception $pattern -> $this")
            null
        }
    }

    fun String.toDate(dateFormat: SimpleDateFormat): Date? {
        return if (TextUtils.isEmpty(this)) {
            null
        } else try {
            dateFormat.parse(this)
        } catch (e: ParseException) {
            Timber.e("parse date exception -> $this")
            null
        }
    }

    fun Date.toTimeString(formatter: SimpleDateFormat): String {
        return formatter.format(this)
    }

    fun Date.toTimeString(pattern: String): String {
        return getSimpleDateFormat(pattern).format(this)
    }

    fun Long.toTimeString(formatter: SimpleDateFormat): String {
        return if (this <= 0L) "" else formatter.format(Date(this))
    }

    @JvmStatic
    fun Long.toTimeString(pattern: String): String = if (this <= 0L) ""
    else getSimpleDateFormat(pattern).format(Date(this))

    fun Long.toTimeString(locale: Locale, pattern: String): String {
        return if (this <= 0L) "" else SimpleDateFormat(pattern, locale).format(Date(this))
    }

    fun Long.toTimeString(locale: Locale, pattern: String, timeZone: TimeZone): String {
        return if (this <= 0L) "" else SimpleDateFormat(pattern, locale).apply {
            this.timeZone = timeZone
        }.format(Date(this))
    }

    @JvmStatic
    @JvmOverloads
    fun Pair<Long, Long>?.toTimeString(pattern: String, joiner: CharSequence = " - "): String {
        if (this == null) return ""
        val formatter = getSimpleDateFormat(pattern)
        return if (this.first <= 0L && this.second <= 0L) ""
        else "${formatter.format(Date(this.first))}$joiner${formatter.format(Date(this.second))}"
    }

    @JvmStatic
    fun toDateRangeString(fromDate: Long, toDate: Long, pattern: String) =
        (fromDate to toDate).toTimeString(pattern)

    fun Date.toCalendarInstance(): Calendar = Calendar.getInstance().apply {
        time = this@toCalendarInstance
    }

    fun Long.toCalendarInstance(): Calendar = Calendar.getInstance().apply {
        timeInMillis = this@toCalendarInstance
    }

    fun Long.toUtcCalendarInstance(): Calendar =
        Calendar.getInstance(TimeZone.getTimeZone(TIMEZONE_UTC)).apply {
            timeInMillis = this@toUtcCalendarInstance
        }

    fun Long.toUtcInstantString(): String {
        return SimpleDateFormat(FORMAT_DATE_TIME_RFC_822, Locale.US).apply {
            timeZone = TIMEZONE_UTC_INSTANT
        }.format(Date(this))
    }

    fun Long.toTimeZoneInstantString(): String {
        return SimpleDateFormat(FORMAT_DATE_TIME_ISO_LOCAL, Locale.getDefault()).format(Date(this))
    }

    fun Long.toLocalInstantString(): String =
        SimpleDateFormat(FORMAT_DATE_TIME_ISO_8601, Locale.US).format(Date(this))

    fun Long.toInstantString(format: String): String =
        SimpleDateFormat(format, Locale.getDefault()).format(Date(this))

    fun String.fromUtcInstantToDate(): Date? {
        return SimpleDateFormat(FORMAT_DATE_TIME_RFC_822, Locale.US).apply {
            timeZone = TIMEZONE_UTC_INSTANT
        }.runCatching {
            parse(this@fromUtcInstantToDate)
        }.getOrElse {
            Timber.e(
                it,
                "Cannot parse fromUtcInstantToDate of ${this@fromUtcInstantToDate} $FORMAT_DATE_TIME_RFC_822"
            )
            null
        }
    }

    fun String.fromLocalInstantToDate(): Date? {
        return SimpleDateFormat(FORMAT_DATE_TIME_ISO_8601, Locale.US).apply {
            timeZone = TIMEZONE_UTC_INSTANT
        }.runCatching {
            parse(this@fromLocalInstantToDate)
        }.getOrElse {
            Timber.e(
                it,
                "Cannot parse fromUtcInstantToDate of ${this@fromLocalInstantToDate} $FORMAT_DATE_TIME_ISO_8601"
            )
            null
        }
    }

    fun String.convertTimeString(inPattern: String, outPattern: String): String? {
        return if (TextUtils.isEmpty(this)) {
            null
        } else SimpleDateFormat(inPattern, Locale.getDefault()).let {
            try {
                it.parse(this)?.run {
                    it.applyPattern(outPattern)
                    it.format(this)
                }
            } catch (e: ParseException) {
                Timber.e("parse date exception $this in $inPattern to $outPattern")
                null
            }
        }
    }

    fun String.convertUtcTimeToLocalTime(inPattern: String, outPattern: String): String? {
        return if (TextUtils.isEmpty(this)) {
            null
        } else getSimpleDateFormat(inPattern, TIMEZONE_UTC).let {
            try {
                it.parse(this)?.run {
                    it.timeZone = TimeZone.getDefault()
                    it.applyPattern(outPattern)
                    it.format(this)
                }
            } catch (e: ParseException) {
                Timber.e("parse date exception $this in $inPattern to $outPattern")
                null
            }
        }
    }

    fun from(year: Int, month: Int, dayOfMonth: Int): Date = Calendar.getInstance().apply {
        clear()
        set(year, month, dayOfMonth)
    }.time

    fun Calendar.setTime(
        year: Int = 0,
        month: Int = 0,
        dayOfMonth: Int = 1,
        hourOfDay: Int = 0,
        minute: Int = 0,
        seconds: Int = 0
    ) {
        this[Calendar.YEAR] = year
        this[Calendar.MONTH] = month
        this[Calendar.DAY_OF_MONTH] = dayOfMonth
        this[Calendar.HOUR_OF_DAY] = hourOfDay
        this[Calendar.MINUTE] = minute
        this[Calendar.SECOND] = seconds
        this[Calendar.MILLISECOND] = 0
    }

    fun Calendar.toFirstMillisOfDay(): Calendar {
        this[Calendar.HOUR_OF_DAY] = 0
        this[Calendar.MINUTE] = 0
        this[Calendar.SECOND] = 0
        this[Calendar.MILLISECOND] = 0
        return this
    }

    fun Calendar.toLastMillisOfDay() {
        this[Calendar.HOUR_OF_DAY] = 23
        this[Calendar.MINUTE] = 59
        this[Calendar.SECOND] = 59
        this[Calendar.MILLISECOND] = 999
    }

    fun isToday(millis: Long): Boolean {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = millis
            toFirstMillisOfDay()
        }
        val now = Calendar.getInstance().apply {
            toFirstMillisOfDay()
        }
        return now.timeInMillis == calendar.timeInMillis
    }

    fun getDay(date: Date): Long {
        return date.time / DAY
    }

    fun getDay(millis: Long): Long {
        return millis / DAY
    }
}