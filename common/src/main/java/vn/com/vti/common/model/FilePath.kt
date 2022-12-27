package vn.com.vti.common.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize


@Parcelize
data class FilePath(
    val local: Boolean, val path: String
) : Parcelable