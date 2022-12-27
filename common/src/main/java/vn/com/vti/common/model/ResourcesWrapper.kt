@file:Suppress("unused", "OPT_IN_IS_NOT_ENABLED")

package vn.com.vti.common.model

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Parcelable
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.*
import androidx.core.content.res.ResourcesCompat
import androidx.databinding.BindingAdapter
import com.google.android.material.color.MaterialColors
import kotlinx.parcelize.Parcelize
import timber.log.Timber
import vn.com.vti.common.util.extension.DateTimeXs.toTimeString
import java.io.Serializable

internal fun interface ByContext<T> {

    fun getBy(context: Context): T
}

abstract class UiText : ByContext<String>, Serializable {

    data class Plain(private val text: String) : UiText() {

        override fun getBy(context: Context): String = text

    }

    data class Dynamic(@StringRes private val stringResId: Int) : UiText() {

        override fun getBy(context: Context): String = context.getString(stringResId)
    }

    class DynamicFormation(@StringRes private val stringResId: Int, private vararg val args: Any) :
        UiText() {

        override fun getBy(context: Context): String = context.getString(stringResId, *args)

    }

    class NestedFormation(
        @StringRes private val stringResId: Int, private vararg val args: UiText
    ) : UiText() {

        override fun getBy(context: Context): String = context.getString(stringResId).let {
            val formationArgs = args.map { src ->
                src.getBy(context)
            }.toTypedArray()
            it.format(*formationArgs)
        }

    }

    data class Plural(@PluralsRes private val stringResId: Int, private val quantity: Int) :
        UiText() {

        override fun getBy(context: Context): String =
            context.resources.getQuantityString(stringResId, quantity)

    }

    class PluralFormation(
        @PluralsRes private val stringResId: Int,
        private val quantity: Int,
        private vararg val args: Any,
    ) : UiText() {

        override fun getBy(context: Context): String =
            context.resources.getQuantityString(stringResId, quantity, *args)

    }

    data class DateTimeFormation(
        @StringRes private val pattern: Int, private val timeInMillis: Long
    ) : UiText() {

        override fun getBy(context: Context): String =
            timeInMillis.toTimeString(context.getString(pattern))
    }

    data class DateTimePeriodFormation(
        @StringRes private val pattern: Int,
        private val period: Pair<Long, Long>,
        @StringRes private val joiner: Int? = null
    ) : UiText() {

        override fun getBy(context: Context): String =
            period.toTimeString(context.getString(pattern))
    }

    companion object {

        fun of(@StringRes stringResId: Int): UiText = Dynamic(stringResId)

        fun of(plain: String): UiText = Plain(plain)

        fun of(@StringRes stringResId: Int, vararg args: Any): UiText =
            DynamicFormation(stringResId, *args)

        fun dateTime(pattern: String, timeInMillis: Long): UiText =
            of(timeInMillis.toTimeString(pattern))

        fun dateTime(@StringRes pattern: Int, timeInMillis: Long): UiText =
            DateTimeFormation(pattern, timeInMillis)

        fun period(@StringRes pattern: Int, period: Pair<Long, Long>): UiText =
            DateTimePeriodFormation(pattern, period)

        fun absent(plain: String?): UiText? = plain?.let {
            Plain(plain)
        }
    }
}

sealed class UiArrayText : ByContext<Array<String>>, Serializable {

    @Parcelize
    class Plain(private val values: Array<String>) : UiArrayText(), Parcelable {

        override fun getBy(context: Context): Array<String> = values

    }

    @Parcelize
    class Dynamic(@ArrayRes private val arrayResId: Int) : UiArrayText(), Parcelable {

        override fun getBy(context: Context): Array<String> =
            context.resources.getStringArray(arrayResId)

    }
}

sealed class UiColor : ByContext<Int>, Serializable {

    @Parcelize
    data class Plain(@ColorInt private val color: Int) : UiColor(), Parcelable {

        @ColorInt
        override fun getBy(context: Context): Int = color

    }

    @Parcelize
    data class Dynamic(@ColorRes private val colorResId: Int) : UiColor(), Parcelable {

        @ColorInt
        override fun getBy(context: Context): Int = context.getColor(colorResId)
    }

    @Parcelize
    data class AttributeRefs(
        @AttrRes private val colorAttributeResId: Int,
        private val fallbackColor: UiColor = Plain(Color.TRANSPARENT),
    ) : UiColor(), Parcelable {

        @ColorInt
        override fun getBy(context: Context): Int = try {
            MaterialColors.getColor(
                context, colorAttributeResId, "Cannot resolve $colorAttributeResId"
            )
        } catch (e: Exception) {
            Timber.e(e)
            fallbackColor.getBy(context)
        }
    }
}

sealed class UiColorStateList : ByContext<ColorStateList>, Serializable {

    @Parcelize
    data class Plain(@ColorInt private val color: Int) : UiColorStateList(), Parcelable {

        override fun getBy(context: Context): ColorStateList = ColorStateList.valueOf(color)

    }

    @Parcelize
    data class SolidAttrRef(
        @AttrRes private val colorAttributeResId: Int,
        private val fallbackColor: UiColor = UiColor.Plain(Color.TRANSPARENT)
    ) : UiColorStateList(), Parcelable {

        override fun getBy(context: Context): ColorStateList = ColorStateList.valueOf(
            try {
                MaterialColors.getColor(
                    context, colorAttributeResId, "Cannot resolve $colorAttributeResId"
                )
            } catch (e: Exception) {
                fallbackColor.getBy(context)
            }
        )
    }

    @Parcelize
    data class Dynamic(@ColorRes private val colorStateListResId: Int) : UiColorStateList(),
        Parcelable {

        override fun getBy(context: Context): ColorStateList =
            context.resources.getColorStateList(colorStateListResId, context.theme)

    }
}

sealed class UiDrawable : ByContext<Drawable?>, Serializable {

    @Parcelize
    data class Solid(@ColorInt private val color: Int) : UiDrawable(), Parcelable {

        override fun getBy(context: Context): Drawable = ColorDrawable(color)

    }

    @Parcelize
    data class Dynamic(@DrawableRes private val drawableResId: Int) : UiDrawable(), Parcelable {

        override fun getBy(context: Context): Drawable? =
            ResourcesCompat.getDrawable(context.resources, drawableResId, context.theme)

    }

    companion object {

        fun ofColor(@ColorInt colorInt: Int): UiDrawable = Solid(colorInt)

        fun ofResourceId(@DrawableRes res: Int): UiDrawable = Dynamic(res)
    }
}

object ResourcesWrapperBindingAdapter {

    @JvmStatic
    @BindingAdapter("android:text")
    fun TextView.setUiText(uiText: UiText?) {
        text = uiText?.getBy(context)
    }

    @JvmStatic
    @BindingAdapter("android:textColor")
    fun TextView.setUiTextColor(uiColor: UiColor?) {
        uiColor?.getBy(context)?.let {
            setTextColor(it)
        }
    }

    @JvmStatic
    @BindingAdapter("android:textColor")
    fun TextView.setUiTextColor(uiColor: UiColorStateList?) {
        uiColor?.getBy(context)?.let {
            setTextColor(it)
        }
    }

    @JvmStatic
    @BindingAdapter("android:background")
    fun View.setUiColorBackground(uiColor: UiColor?) {
        if (uiColor == null) background = null
        else setBackgroundColor(uiColor.getBy(context))
    }

    @JvmStatic
    @BindingAdapter("android:backgroundTint")
    fun View.setUiColorBackgroundTint(uiColor: UiColor?) {
        backgroundTintList = uiColor?.getBy(context)?.let {
            ColorStateList.valueOf(it)
        }
    }

    @JvmStatic
    @BindingAdapter("android:backgroundTint")
    fun View.setUiColorBackgroundTint(uiColor: UiColorStateList?) {
        backgroundTintList = uiColor?.getBy(context)
    }

    @JvmStatic
    @BindingAdapter("android:tint")
    fun ImageView.setUiColorTint(uiColor: UiColor?) {
        imageTintList = uiColor?.getBy(context)?.let {
            ColorStateList.valueOf(it)
        }
    }

    @JvmStatic
    @BindingAdapter("android:tint")
    fun ImageView.setUiColorTint(uiColor: UiColorStateList?) {
        imageTintList = uiColor?.getBy(context)
    }

    @JvmStatic
    @BindingAdapter("android:src")
    fun ImageView.setUiDrawable(uiDrawable: UiDrawable?) {
        setImageDrawable(uiDrawable?.getBy(context))
    }
}