@file:Suppress("unused")

package vn.com.vti.common.util

import android.content.SharedPreferences
import android.net.Uri
import androidx.core.content.edit
import androidx.core.net.toUri
import com.google.gson.reflect.TypeToken
import vn.com.vti.common.serializer.Serializer
import java.lang.reflect.Type
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

abstract class PrimitivePreferenceDelegate<T>(
    protected val preferences: SharedPreferences,
    protected val prefKey: String
) : ReadWriteProperty<Any?, T> {

    init {
        if (prefKey.isEmpty())
            throw IllegalArgumentException("preferences cannot be empty")
    }
}

class BooleanPreferenceDelegate(
    preferences: SharedPreferences,
    prefKey: String,
    defaultValue: Boolean = false
) : PrimitivePreferenceDelegate<Boolean>(preferences, prefKey) {

    private var backingField: Boolean = preferences.getBoolean(prefKey, defaultValue)

    override fun getValue(thisRef: Any?, property: KProperty<*>) = backingField

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: Boolean) {
        if (backingField != value) {
            backingField = value
            preferences.edit {
                putBoolean(prefKey, value)
            }
        }
    }
}

fun booleanPreference(
    preferences: SharedPreferences,
    prefKey: String,
    defaultValue: Boolean = false
) = BooleanPreferenceDelegate(preferences, prefKey, defaultValue)

class StringPreferenceDelegate(
    preferences: SharedPreferences,
    prefKey: String,
    defaultValue: String? = null
) : PrimitivePreferenceDelegate<String?>(preferences, prefKey) {

    private var backingField: String? = preferences.getString(prefKey, defaultValue)

    override fun getValue(thisRef: Any?, property: KProperty<*>) = backingField

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: String?) {
        if (backingField != value) {
            backingField = value
            preferences.edit {
                putString(prefKey, backingField)
            }
        }
    }
}

fun stringPreference(
    preferences: SharedPreferences,
    prefKey: String,
    defaultValue: String? = null
) = StringPreferenceDelegate(preferences, prefKey, defaultValue)

class NonNullStringPreferenceDelegate(
    preferences: SharedPreferences,
    prefKey: String,
    defaultValue: String = ""
) : PrimitivePreferenceDelegate<String>(preferences, prefKey) {

    private var backingField: String = preferences.getString(prefKey, defaultValue) ?: defaultValue

    override fun getValue(thisRef: Any?, property: KProperty<*>) = backingField

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: String) {
        if (backingField != value) {
            backingField = value
            preferences.edit {
                putString(prefKey, backingField)
            }
        }
    }
}

fun nonNullStringPreference(
    preferences: SharedPreferences,
    prefKey: String,
    defaultValue: String = ""
) = NonNullStringPreferenceDelegate(preferences, prefKey, defaultValue)

class IntPreferenceDelegate(
    preferences: SharedPreferences,
    prefKey: String,
    defaultValue: Int
) : PrimitivePreferenceDelegate<Int>(preferences, prefKey) {

    private var backingField: Int = preferences.getInt(prefKey, defaultValue)

    override fun getValue(thisRef: Any?, property: KProperty<*>) = backingField

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: Int) {
        if (backingField != value) {
            backingField = value
            preferences.edit {
                putInt(prefKey, value)
            }
        }
    }
}

fun intPreference(
    preferences: SharedPreferences,
    prefKey: String,
    defaultValue: Int = 0
) = IntPreferenceDelegate(preferences, prefKey, defaultValue)

class LongPreferenceDelegate(
    preferences: SharedPreferences,
    prefKey: String,
    defaultValue: Long
) : PrimitivePreferenceDelegate<Long>(preferences, prefKey) {

    private var backingField: Long = preferences.getLong(prefKey, defaultValue)

    override fun getValue(thisRef: Any?, property: KProperty<*>) = backingField

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: Long) {
        if (backingField != value) {
            backingField = value
            preferences.edit {
                putLong(prefKey, value)
            }
        }
    }
}

fun longPreference(
    preferences: SharedPreferences,
    prefKey: String,
    defaultValue: Long = 0
) = LongPreferenceDelegate(preferences, prefKey, defaultValue)

class FloatPreferenceDelegate(
    preferences: SharedPreferences,
    prefKey: String,
    defaultValue: Float
) : PrimitivePreferenceDelegate<Float>(preferences, prefKey) {

    private var backingField: Float = preferences.getFloat(prefKey, defaultValue)

    override fun getValue(thisRef: Any?, property: KProperty<*>) = backingField

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: Float) {
        if (backingField != value) {
            backingField = value
            preferences.edit {
                putFloat(prefKey, value)
            }
        }
    }
}

fun floatPreference(
    preferences: SharedPreferences,
    prefKey: String,
    defaultValue: Float = 0f
) = FloatPreferenceDelegate(preferences, prefKey, defaultValue)

class StringSetPreferenceDelegate(
    preferences: SharedPreferences,
    prefKey: String,
    defaultValue: Set<String>? = null
) : PrimitivePreferenceDelegate<Set<String>?>(preferences, prefKey) {

    private var backingField: Set<String>? = preferences.getStringSet(prefKey, defaultValue)

    override fun getValue(thisRef: Any?, property: KProperty<*>) = backingField

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: Set<String>?) {
        if (backingField != value) {
            backingField = value
            preferences.edit {
                putStringSet(prefKey, backingField)
            }
        }
    }
}

fun stringSetPreference(
    preferences: SharedPreferences,
    prefKey: String,
    defaultValue: Set<String>? = null
) = StringSetPreferenceDelegate(preferences, prefKey, defaultValue)

class SerializablePreferenceDelegate<T>(
    private val preferences: AppSharedPreferences,
    private val prefKey: String,
    type: Type,
    defaultValue: T
) : ReadWriteProperty<Any?, T> {

    private var backingField: T

    init {
        if (prefKey.isEmpty())
            throw IllegalArgumentException("preferences cannot be empty")
        backingField = preferences.getSerializable(prefKey, type) ?: defaultValue
    }

    override operator fun getValue(thisRef: Any?, property: KProperty<*>): T = backingField

    override operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        backingField = value
        if (value != null) {
            preferences.putSerializable(prefKey, value)
        } else {
            preferences.edit {
                remove(prefKey)
            }
        }
    }
}

inline fun <reified T> serializablePreference(
    preferences: AppSharedPreferences,
    prefKey: String,
    defaultValue: T? = null
) = SerializablePreferenceDelegate(
    preferences,
    prefKey,
    TypeToken.get(T::class.java).type,
    defaultValue
)

inline fun <reified T> serializablePreference(
    preferences: SharedPreferences,
    serializer: Serializer,
    prefKey: String,
    defaultValue: T? = null
) = SerializablePreferenceDelegate(
    AppSharedPreferences(preferences, serializer),
    prefKey,
    TypeToken.get(T::class.java).type,
    defaultValue
)

class UriPreferenceDelegate(
    preferences: SharedPreferences,
    prefKey: String,
    defaultValue: Uri? = null
) : PrimitivePreferenceDelegate<Uri?>(preferences, prefKey) {

    private var backingField: Uri? =
        preferences.getString(prefKey, null).safeToUri() ?: defaultValue

    override fun getValue(thisRef: Any?, property: KProperty<*>) = backingField

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: Uri?) {
        if (backingField != value) {
            backingField = value
            preferences.edit {
                putString(prefKey, backingField.toString())
            }
        }
    }

    private fun String?.safeToUri(): Uri? {
        return if (this.isNullOrEmpty()) null
        else try {
            this.toUri()
        } catch (e: Exception) {
            null
        }
    }
}

fun uriPreference(
    preferences: SharedPreferences,
    prefKey: String,
    defaultValue: Uri? = null
) = UriPreferenceDelegate(preferences, prefKey, defaultValue)

