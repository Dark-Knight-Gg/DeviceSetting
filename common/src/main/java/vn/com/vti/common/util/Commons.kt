package vn.com.vti.common.util

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.widget.Toast
import androidx.annotation.IntRange
import androidx.annotation.StringRes
import androidx.work.WorkManager
import androidx.work.WorkRequest
import timber.log.Timber

infix fun <F, S> F.nativeTo(s: S) = android.util.Pair(this, s)

infix fun <F, S> F.androidXTo(s: S) = androidx.core.util.Pair(this, s)

inline fun <T : CharSequence> T?.notNullOrEmptyLet(block: (T) -> Unit) {
    if (!isNullOrEmpty()) block(this)
}

inline fun <R : Collection<*>> R?.notNullOrEmptyLet(block: (R) -> Unit) {
    if (!isNullOrEmpty()) block(this)
}

infix fun <K, V> Pair<K, V>.putInto(map: MutableMap<in K, in V>) {
    map[first] = second
}

fun MutableMap<String, String>.putIfNotEmpty(key: String, value: String?) {
    value.notNullOrEmptyLet {
        put(key, it)
    }
}

fun Context.toast(text: String, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, text, duration).show()
}

fun Context.toast(@StringRes text: Int, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, text, duration).show()
}

fun Context.enqueueWorkRequest(request: WorkRequest) {
    WorkManager.getInstance(this).enqueue(request)
}

fun Bundle.toDataIntent() = Intent().also {
    it.putExtras(this)
}

@Suppress("DEPRECATION")
inline fun <reified T : java.io.Serializable> Bundle.compatGetSerializable(key: String): T =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        getSerializable(key, T::class.java)!!
    } else getSerializable(key) as T

@Suppress("DEPRECATION")
inline fun <reified T : Parcelable> Bundle.compatGetParcelable(key: String): T =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        getParcelable(key, T::class.java)!!
    } else getParcelable(key)!!

fun String.ellipsize(@IntRange(from = 1) size: Int, symbol: String = "\u2026"): String {
    return if (size in 1 until length) substring(0, size) + symbol
    else this
}

inline fun <T, R> T.runSafety(runnable: T.() -> R): R? {
    return try {
        runnable()
    } catch (e: Exception) {
        Timber.e(e)
        return null
    }
}

inline fun <T, R> T.runSafety(
    runnable: T.() -> R, onException: T.(Exception) -> R? = {
        Timber.e(it)
        null
    }
): R? {
    return try {
        runnable()
    } catch (e: Exception) {
        return onException(e)
    }
}

inline fun runSafety(runnable: () -> Unit) {
    try {
        runnable()
    } catch (e: Exception) {
        Timber.e(e)
    }
}

fun Activity.restartApplication() {
    val intent = packageManager.getLaunchIntentForPackage(packageName)
    finishAffinity()
    startActivity(intent)
}