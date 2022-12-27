package vn.com.vti.common.util

import android.os.Parcel
import android.os.Parcelable
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat.CLOCK_24H
import timber.log.Timber
import vn.com.vti.common.R
import vn.com.vti.common.util.extension.DateTimeXs.toCalendarInstance
import java.util.*

fun Fragment.showSingleDatePicker(
    currentInLocal: Long,
    constraints: LongRange? = null,
    callback: (Long) -> Unit,
) {
    val utcCompensation = Calendar.getInstance().get(Calendar.ZONE_OFFSET)
    val builder: MaterialDatePicker.Builder<Long> = MaterialDatePicker.Builder
        .datePicker()
        .setTheme(com.google.android.material.R.style.ThemeOverlay_MaterialComponents_MaterialCalendar)
        .setSelection(currentInLocal + utcCompensation)
    val constraintsBuilder = CalendarConstraints.Builder()
    constraints?.let {
        if (it.first > 0) {
            constraintsBuilder.setStart(it.first)
        }
        if (it.last < Long.MAX_VALUE) {
            constraintsBuilder.setEnd(it.last)
        }
        constraintsBuilder.setValidator(DateInRangeValidator(it))
    }
    try {
        builder.setCalendarConstraints(
            constraintsBuilder
                .build()
        )
        builder.build().apply {
            addOnPositiveButtonClickListener { nullableMs ->
                nullableMs?.let { ms ->
                    callback(ms - utcCompensation)
                }
            }
            show(
                this@showSingleDatePicker.childFragmentManager,
                MaterialDatePicker::class.java.simpleName
            )
        }
    } catch (e: IllegalArgumentException) {
        Timber.e(e)
        context?.let {
            Toast.makeText(it, R.string.msg_common_error, Toast.LENGTH_SHORT).show()
        }
    }
}

fun Fragment.showRangeDatePicker(
    selectedRangeInLocal: Pair<Long, Long>?,
    constraints: LongRange? = null,
    callback: (Pair<Long, Long>) -> Unit
) {
    val utcCompensation = Calendar.getInstance().get(Calendar.ZONE_OFFSET)
    val pickerBuilder = MaterialDatePicker.Builder
        .dateRangePicker()
        .apply {
            selectedRangeInLocal?.let {
                setSelection((it.first + utcCompensation) androidXTo (it.second + utcCompensation))
            }
        }
    val constraintsBuilder = CalendarConstraints.Builder()
    constraints?.let {
        constraintsBuilder.setStart(it.first)
        constraintsBuilder.setEnd(it.last)
        constraintsBuilder.setValidator(DateInRangeValidator(it))
    }
    try {
        pickerBuilder
            .setCalendarConstraints(constraintsBuilder.build())
            .setTheme(com.google.android.material.R.style.ThemeOverlay_MaterialComponents_MaterialCalendar)
        pickerBuilder.build().apply {
            addOnPositiveButtonClickListener {
                it?.let {
                    callback((it.first - utcCompensation) to (it.second - utcCompensation))
                }
            }
            show(
                this@showRangeDatePicker.childFragmentManager,
                "ACTION_CALENDAR_PICKER"
            )
        }
    } catch (e: IllegalArgumentException) {
        Timber.e(e)
        context?.let {
            Toast.makeText(it, R.string.msg_common_error, Toast.LENGTH_SHORT).show()
        }
    }
}

fun Fragment.showTimePicker(
    currentSelected: Long?,
    callback: (Long) -> Unit
) {
    val calendar = (currentSelected ?: System.currentTimeMillis()).toCalendarInstance()
    val pickerBuilder: MaterialTimePicker.Builder = MaterialTimePicker.Builder().apply {
//        setTheme(R.style.ThemeOverlay_MaterialComponents_MaterialCalendar)
        setHour(calendar[Calendar.HOUR_OF_DAY])
        setMinute(calendar[Calendar.MINUTE])
        setTimeFormat(CLOCK_24H)
    }
    try {
        pickerBuilder.build().apply {
            addOnPositiveButtonClickListener {
                calendar[Calendar.HOUR_OF_DAY] = hour
                calendar[Calendar.MINUTE] = minute
                callback(calendar.timeInMillis)
            }
            show(
                this@showTimePicker.childFragmentManager,
                "ACTION_CALENDAR_PICKER"
            )
        }
    } catch (e: IllegalArgumentException) {
        Timber.e(e)
        context?.let {
            Toast.makeText(it, R.string.msg_common_error, Toast.LENGTH_SHORT).show()
        }
    }
}

data class DateInRangeValidator(private val range: LongRange) : CalendarConstraints.DateValidator {

    constructor(parcel: Parcel) : this(parcel.readLong()..parcel.readLong())

    override fun isValid(date: Long): Boolean = date in range

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(range.first)
        parcel.writeLong(range.last)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<DateInRangeValidator> {
        override fun createFromParcel(parcel: Parcel): DateInRangeValidator {
            return DateInRangeValidator(parcel)
        }

        override fun newArray(size: Int): Array<DateInRangeValidator?> {
            return arrayOfNulls(size)
        }
    }

}