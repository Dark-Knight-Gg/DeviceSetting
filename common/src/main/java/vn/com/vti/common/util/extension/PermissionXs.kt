@file:Suppress("unused")

package vn.com.vti.common.util.extension

import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import vn.com.vti.common.viewmodel.impl.BaseAndroidViewModel

object PermissionUtils {

    fun isAllPermissionsGranted(results: IntArray): Boolean {
        if (results.isEmpty()) {
            throw IllegalArgumentException("Permission cannot be empty")
        }
        return results.all { it == PackageManager.PERMISSION_GRANTED }
    }
}

fun BaseAndroidViewModel.isAllPermissionsGranted(vararg permission: String): Boolean {
    if (permission.isEmpty()) {
        throw IllegalArgumentException("Permission cannot be empty")
    }
    return permission.all {
        ContextCompat.checkSelfPermission(
            getApplication(),
            it
        ) == PackageManager.PERMISSION_GRANTED
    }
}

fun Context.getRequiredPermissions(): List<String> {
    val info = this.packageManager
        .getPackageInfo(this.packageName, PackageManager.GET_PERMISSIONS)
    return info.requestedPermissions.filter {
        ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
    }
}

fun Context.isAllPermissionsGranted(vararg permission: String): Boolean {
    if (permission.isEmpty()) {
        throw IllegalArgumentException("Permission cannot be empty")
    }
    return permission.all {
        ContextCompat.checkSelfPermission(
            this,
            it
        ) == PackageManager.PERMISSION_GRANTED
    }
}