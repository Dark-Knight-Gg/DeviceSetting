package vn.com.vti.common.util.extension

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import androidx.annotation.ColorInt
import androidx.annotation.IntRange
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.Fragment

@Suppress("DEPRECATION")
fun Activity.setStatusBarColor(@ColorInt statusColor: Int = Color.TRANSPARENT) {
    window.apply {
        clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        statusBarColor = statusColor
    }
}

@Suppress("DEPRECATION")
@SuppressLint("InlinedApi", "ObsoleteSdkInt")
fun Activity.setupFullscreenMode(darkMode: Boolean) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        window.apply {
            val sdk = Build.VERSION.SDK_INT
            val flag = when {
                sdk >= Build.VERSION_CODES.R -> WindowInsetsController.BEHAVIOR_SHOW_BARS_BY_SWIPE
                sdk >= Build.VERSION_CODES.M -> View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                else -> return
            }
            decorView.systemUiVisibility =
                flag.let {
                    if (darkMode) it and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
                    else it or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                }
        }
    }
}

@Suppress("DEPRECATION")
@SuppressLint("InlinedApi")
fun Activity.setStatusBarDarkMode(enable: Boolean) {
    window.apply {
        decorView.systemUiVisibility =
            decorView.systemUiVisibility.let {
                if (enable) it and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
                else it or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            }
    }
}

fun Activity.postDelay(action: () -> Unit, @IntRange(from = 1) delayMillis: Long) {
    window?.decorView?.postDelayed(action, delayMillis)
}

fun Fragment.adjustImeResize() {
    activity?.window?.apply {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            setDecorFitsSystemWindows(true)
            this@adjustImeResize.view?.setOnApplyWindowInsetsListener { v, insets ->
                val imeInsets = insets.getInsets(WindowInsets.Type.ime())
                v.translationX = imeInsets.bottom.toFloat()
                insets
            }
        } else {
            @Suppress("DEPRECATION")
            setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
            this@adjustImeResize.view?.setOnApplyWindowInsetsListener { v, insets ->
                if (insets.hasInsets()) {
                    @Suppress("DEPRECATION")
                    v.translationX = insets.systemWindowInsetBottom.toFloat()
                } else {
                    v.translationX = 0.0f
                }
                insets
            }
        }
    }
}

fun Activity.dismissKeyboard() {
    val target = currentFocus ?: return
    getSystemService(this, InputMethodManager::class.java)?.hideSoftInputFromWindow(
        target.windowToken,
        0
    )
}

fun Fragment.dismissKeyboard() {
    view?.apply {
        val target = activity?.currentFocus ?: this
        getSystemService(context, InputMethodManager::class.java)?.hideSoftInputFromWindow(
            target.windowToken,
            0
        )
    }
}

inline fun Application.registerCompatActivityLifecycleCallbacks(
    crossinline onCreated: (Activity, Bundle?) -> Unit = { _, _ -> },
    crossinline onStarted: (Activity) -> Unit = { },
    crossinline onResumed: (Activity) -> Unit = { },
    crossinline onPaused: (Activity) -> Unit = { },
    crossinline onStopped: (Activity) -> Unit = { },
    crossinline onSaveInstanceState: (Activity, Bundle) -> Unit = { _, _ -> },
    crossinline onDestroyed: (Activity) -> Unit = { },

    ): Application.ActivityLifecycleCallbacks = object : Application.ActivityLifecycleCallbacks {

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        onCreated(activity, savedInstanceState)
    }

    override fun onActivityStarted(activity: Activity) {
        onStarted(activity)
    }

    override fun onActivityResumed(activity: Activity) {
        onResumed(activity)
    }

    override fun onActivityPaused(activity: Activity) {
        onPaused(activity)
    }

    override fun onActivityStopped(activity: Activity) {
        onStopped(activity)
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
        onSaveInstanceState(activity, outState)
    }

    override fun onActivityDestroyed(activity: Activity) {
        onDestroyed(activity)
    }

}.also {
    registerActivityLifecycleCallbacks(it)
}