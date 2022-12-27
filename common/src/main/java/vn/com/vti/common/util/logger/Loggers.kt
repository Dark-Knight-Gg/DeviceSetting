package vn.com.vti.common.util.logger

import android.util.Log
import com.google.firebase.crashlytics.FirebaseCrashlytics
import timber.log.Timber

open class CrashlyticTree : Timber.Tree() {

    protected val crashlytics: FirebaseCrashlytics by lazy {
        FirebaseCrashlytics.getInstance()
    }

    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        if (priority <= Log.WARN) {
            return
        }
        if (t != null) {
            crashlytics.recordException(t)
        } else {
            crashlytics.log("tag=${tag ?: "empty"} level=${priority} message=${message}")
        }
    }
}

open class DebugCrashlyticTree : Timber.DebugTree() {

    protected val crashlytics: FirebaseCrashlytics by lazy {
        FirebaseCrashlytics.getInstance()
    }

    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        super.log(priority, tag, message, t)
        if (priority <= Log.INFO) {
            return
        }
        if (t != null) {
            crashlytics.recordException(t)
        } else {
            crashlytics.log("tag=${tag ?: "empty"} level=${priority} message=${message}")
        }
    }
}