package vn.com.vti.common.annotation

import androidx.annotation.IntDef

@IntDef(
    LoadingType.NONE,
    LoadingType.FREE,
    LoadingType.BLOCKING,
    LoadingType.PROGRESSIVE
)
@Retention(AnnotationRetention.SOURCE)
annotation class LoadingType {
    companion object {
        const val PROGRESSIVE = 3
        const val BLOCKING = 2
        const val FREE = 1
        const val NONE = 0
    }
}