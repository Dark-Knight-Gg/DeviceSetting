package vn.com.vti.common.scene

import androidx.annotation.IntDef

@IntDef(
    BinderConst.NOT_BINDING,
    BinderConst.DEFAULT
)
@Retention(AnnotationRetention.SOURCE)
annotation class BinderConst {
    companion object {
        const val NOT_BINDING = -1
        const val DEFAULT = 0
    }
}