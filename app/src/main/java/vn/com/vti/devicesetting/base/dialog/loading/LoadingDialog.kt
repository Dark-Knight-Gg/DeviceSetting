package vn.com.vti.devicesetting.base.dialog.loading

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.*
import android.widget.FrameLayout
import androidx.fragment.app.DialogFragment
import vn.com.vti.devicesetting.R

class LoadingDialog : DialogFragment() {
    private var gravity = Gravity.CENTER

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return super.onCreateDialog(savedInstanceState).apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            setCancelable(false)
            setCanceledOnTouchOutside(false)
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.apply {
            setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val content: View = inflater.inflate(R.layout.dialog_loading, container, false)
        if (gravity != Gravity.CENTER) {
            val progress = content.findViewById<View>(R.id.progress)
            val params = progress.layoutParams as FrameLayout.LayoutParams
            params.gravity = gravity
        }
        return content
    }

    companion object {
        fun newInstance(): LoadingDialog {
            return LoadingDialog()
        }

        fun newInstance(gravity: Int): LoadingDialog {
            val dialog = LoadingDialog()
            dialog.gravity = gravity
            return dialog
        }
    }
}