package vn.com.vti.common.model

import android.content.Context
import androidx.annotation.StyleRes
import androidx.appcompat.app.AlertDialog
import java.io.Serializable

data class ConfirmRequest(
    val icon: UiDrawable? = null,
    val title: UiText? = null,
    val message: UiText? = null,
    val positive: UiText? = null,
    val negative: UiText? = null,
    val neutral: UiText? = null,
    val cancelable: Boolean = true,
    val timeOutInMillis: Long? = null,
    @Transient val onPositiveSelected: (() -> Unit)? = null,
    @Transient val onNegativeSelected: (() -> Unit)? = null,
    @Transient val onNeutralSelected: (() -> Unit)? = null,
) : Serializable

fun ConfirmRequest.buildAlertDialog(context: Context, @StyleRes style: Int = 0): AlertDialog =
    AlertDialog.Builder(context, style)
        .apply {
            setTitle(title?.getBy(context))
                .setMessage(message?.getBy(context))
                .setCancelable(cancelable)
                .setIcon(icon?.getBy(context))
            positive?.let {
                setPositiveButton(it.getBy(context)) { dialog, _ ->
                    dialog.dismiss()
                    onPositiveSelected?.invoke()
                }
            }
            negative?.let {
                setNegativeButton(it.getBy(context)) { dialog, _ ->
                    dialog.dismiss()
                    onNegativeSelected?.invoke()
                }
            }
            neutral?.let {
                setNeutralButton(it.getBy(context)) { dialog, _ ->
                    dialog.dismiss()
                    onNeutralSelected?.invoke()
                }
            }
        }.create()

