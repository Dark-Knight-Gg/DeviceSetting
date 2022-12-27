package vn.com.vti.common.util

import android.util.Patterns

fun String.isEmail() = matches(Patterns.EMAIL_ADDRESS.toRegex())