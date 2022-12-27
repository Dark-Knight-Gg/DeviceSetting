@file:Suppress("unused")

package vn.com.vti.common.util

import android.content.Context
import android.content.SharedPreferences
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import vn.com.vti.common.serializer.Serializer
import java.lang.reflect.Type

@Suppress("unused")
class AppSharedPreferences(
    private val preferences: SharedPreferences,
    private val serializer: Serializer,
) : SharedPreferences by preferences {

    constructor(
        context: Context,
        name: String,
        serializer: Serializer,
    ) : this(context.getSharedPreferences(name, Context.MODE_PRIVATE), serializer)

    fun getString(key: String): String? = getString(key, null)

    fun getStringSet(key: String): MutableSet<String>? = getStringSet(key, null)

    fun putSerializable(key: String, obj: Any?) {
        obj?.let {
            serializer.serialize(it).let { json ->
                if (json.isEmpty()) delete(key)
                else save(key, json)
            }
        } ?: delete(key)
    }

    fun <T> getSerializable(key: String, clazz: Class<T>): T? =
        getString(key, null)?.let {
            serializer.deserialize(it, clazz)
        }

    fun <T> getSerializable(key: String, type: Type): T? =
        getString(key, null)?.let {
            serializer.deserialize(it, type)
        }

    inline fun <reified T> getSerializable(key: String): T? = getSerializable(key, T::class.java)

    fun clears(vararg names: String) {
        applyChanges {
            names.forEach { remove(it) }
        }
    }

    inline fun clear(predicate: (String) -> Boolean) {
        applyChanges {
            all.keys.forEach {
                if (predicate(it)) remove(it)
            }
        }
    }

    companion object {
        const val TOKEN_KEY = "TOKEN_KEY"
    }
}

fun newEncryptedPreferences(context: Context, name: String): SharedPreferences {
    require(name.isNotEmpty())
    val builder = MasterKey.Builder(context.applicationContext)
    val spec = KeyGenParameterSpec.Builder(
        "_androidx_security_master_key_",
        KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
    ).setBlockModes(KeyProperties.BLOCK_MODE_GCM)
        .setRandomizedEncryptionRequired(true)
        .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
        .setKeySize(256)
        .build()
    builder.setKeyGenParameterSpec(spec)

    return EncryptedSharedPreferences.create(
        context.applicationContext,
        name, builder.build(),
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )
}

inline fun SharedPreferences.applyChanges(
    changes: SharedPreferences.Editor.() -> Unit,
) {
    edit().run {
        changes()
        apply()
    }
}

inline fun SharedPreferences.commitChanges(
    changes: SharedPreferences.Editor.() -> Unit,
) {
    edit().run {
        changes()
        commit()
    }
}

fun SharedPreferences.save(key: String, value: String?) {
    edit().putString(key, value).apply()
}

fun SharedPreferences.save(key: String, value: Int) {
    edit().putInt(key, value).apply()
}

fun SharedPreferences.save(key: String, value: Long) {
    edit().putLong(key, value).apply()
}

fun SharedPreferences.save(key: String, value: Boolean) {
    edit().putBoolean(key, value).apply()
}

fun SharedPreferences.delete(key: String) {
    edit().remove(key).apply()
}

fun SharedPreferences.clear() {
    edit().clear().apply()
}