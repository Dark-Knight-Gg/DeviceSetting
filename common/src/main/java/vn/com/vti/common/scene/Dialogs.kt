@file:Suppress("unused")

package vn.com.vti.common.scene

import android.app.Dialog
import android.content.res.Resources
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Toast
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.annotation.CallSuper
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LifecycleOwner
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import timber.log.Timber
import vn.com.vti.common.model.ConfirmRequest
import vn.com.vti.common.model.UiText
import vn.com.vti.common.model.buildAlertDialog
import vn.com.vti.common.viewmodel.AbsViewModel
import vn.com.vti.common.viewmodel.Scene
import vn.com.vti.common.viewmodel.observeViewModelDirection
import vn.com.vti.common.viewmodel.observeViewModelEvent
import java.lang.ref.WeakReference

open class BaseDialog : DialogFragment(), Scene, Stage {

    private var baseActivityWeakReference: WeakReference<BaseActivity<*>?>? = null

    @CallSuper
    override fun onDestroy() {
        super.onDestroy()
        baseActivityWeakReference = null
    }

    @Suppress("MemberVisibilityCanBePrivate")
    protected fun getBaseActivity(): BaseActivity<*>? {
        return baseActivityWeakReference.let { reference ->
            if (reference?.get() == null) {
                (activity as? BaseActivity<*>)?.also {
                    baseActivityWeakReference = WeakReference(it)
                }
            } else reference.get()
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return super.onCreateDialog(savedInstanceState).apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            setCancelable(false)
            setCanceledOnTouchOutside(false)
        }
    }

    fun show(activity: FragmentActivity?, tag: String) {
        activity?.let {
            if (!isAdded) {
                try {
                    show(it.supportFragmentManager, tag)
                } catch (ex: IllegalStateException) {
                    Timber.e("show Dialog get IllegalStateException act[${it.javaClass.simpleName}] tag[${tag}] class[${javaClass.simpleName}]")
                    ex.printStackTrace()
                }
            }
        }
    }

    override fun notifyBlockingTaskStart() {
        getBaseActivity()?.notifyBlockingTaskStart()
    }

    override fun notifyBlockingTaskFinish() {
        getBaseActivity()?.notifyBlockingTaskFinish()
    }

    override fun showMessage(request: ConfirmRequest) {
        getBaseActivity()?.showMessage(request)
    }

    override fun toast(message: UiText) {
        val context = context ?: return
        Toast.makeText(context, message.getBy(context), Toast.LENGTH_SHORT).show()
    }

    override fun getSceneResource(): Resources = resources

    override fun lifecycleOwner(): LifecycleOwner = this

    override fun <I, O> registerForResultCallback(
        contract: ActivityResultContract<I, O>, callback: ActivityResultCallback<O>
    ): ActivityResultLauncher<I> = registerForActivityResult(contract, callback)

    val isShow: Boolean
        get() = dialog?.isShowing ?: false

    /**
     * Call after scene finish it's initialization
     */
    protected open fun onSceneReady() {}
}

@Suppress("MemberVisibilityCanBePrivate")
abstract class BaseDialogFragment<VIEWMODEL : AbsViewModel> : BaseDialog(), Stage {

    protected abstract val viewModel: VIEWMODEL

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.run {
            onAttachScene(this@BaseDialogFragment)
            onBind(arguments)
            onViewModelCreated(this)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        return onCreateViewDataBinding(inflater, container).apply {
            lifecycleOwner = viewLifecycleOwner
        }.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setupComponents()
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.run {
            setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
            )
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
    }

    @CallSuper
    override fun onDestroy() {
        super.onDestroy()
        viewModel.run {
            if (getRunningTaskCount().value > 0) {
                notifyBlockingTaskFinish()
            }
            onUnbind()
            onDetachScene(this@BaseDialogFragment)
        }
    }


    /**
     * Create content viewBinding
     *
     * @return the content viewBinding
     */
    protected abstract fun onCreateViewDataBinding(
        inflater: LayoutInflater,
        container: ViewGroup?,
    ): ViewDataBinding

    /**
     * Define view-model variables to bind viewmodel with viewbinding
     *
     * @return viewmodel variables id (BR.vm,... or [BinderConst.NOT_BINDING])
     */
    protected abstract fun getViewModelVariableId(): Int

    private fun setupComponents() {
        val viewBinding = view?.let {
            DataBindingUtil.findBinding<ViewDataBinding>(it)
        } ?: return
        viewModel.run {
            getViewModelVariableId().let {
                if (it != BinderConst.NOT_BINDING) {
                    viewBinding.setVariable(it, this)
                }
            }
            onReady()
        }
        Timber.d("INIT s=${javaClass.simpleName} v=${viewBinding.javaClass.simpleName} vm=${viewModel.javaClass.simpleName}")
        viewBinding.executePendingBindings()
        onSceneReady()
    }

    protected open fun onViewModelCreated(viewmodel: VIEWMODEL) {
        observeViewModelDirection(viewModel)
        observeViewModelEvent(viewModel, this)
    }
}

abstract class BaseBottomSheetDialogFragment<VIEWMODEL : AbsViewModel> :
    BottomSheetDialogFragment(), Scene, Stage {

    protected abstract val viewModel: VIEWMODEL

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.run {
            onAttachScene(this@BaseBottomSheetDialogFragment)
            onBind(arguments)
            onViewModelCreated(this)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        return onCreateViewDataBinding(inflater, container).apply {
            lifecycleOwner = viewLifecycleOwner
        }.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setupComponents()
    }

    @CallSuper
    override fun onDestroy() {
        super.onDestroy()
        viewModel.run {
            if (getRunningTaskCount().value > 0) {
                notifyBlockingTaskFinish()
            }
            onUnbind()
            onDetachScene(this@BaseBottomSheetDialogFragment)
        }
        baseActivityWeakReference = null
    }


    /**
     * Create content viewbinding
     *
     * @return the content viewbinding
     */
    protected abstract fun onCreateViewDataBinding(
        inflater: LayoutInflater,
        container: ViewGroup?,
    ): ViewDataBinding

    /**
     * Define view-model variables to bind viewmodel with viewbinding
     *
     * @return viewmodel variables id (BR.vm,... or [BinderConst.NOT_BINDING])
     */
    protected abstract fun getViewModelVariableId(): Int

    private fun setupComponents() {
        val viewBinding = view?.let {
            DataBindingUtil.findBinding<ViewDataBinding>(it)
        } ?: return
        viewModel.run {
            getViewModelVariableId().let {
                if (it != BinderConst.NOT_BINDING) {
                    viewBinding.setVariable(it, this)
                }
            }
            onReady()
        }
        Timber.d("INIT s=${javaClass.simpleName} v=${viewBinding.javaClass.simpleName} vm=${viewModel.javaClass.simpleName}")
        viewBinding.executePendingBindings()
        onSceneReady()
    }

    private var baseActivityWeakReference: WeakReference<BaseActivity<*>?>? = null

    @Suppress("MemberVisibilityCanBePrivate")
    protected fun getBaseActivity(): BaseActivity<*>? {
        return baseActivityWeakReference.let { refs ->
            if (refs?.get() == null) {
                (activity as? BaseActivity<*>)?.also {
                    baseActivityWeakReference = WeakReference(it)
                }
            } else refs.get()
        }
    }

    protected open fun onSceneReady() {}

    override fun notifyBlockingTaskStart() {
        getBaseActivity()?.notifyBlockingTaskStart()
    }

    override fun notifyBlockingTaskFinish() {
        getBaseActivity()?.notifyBlockingTaskFinish()
    }

    override fun showMessage(request: ConfirmRequest) {
        getBaseActivity()?.showMessage(request)
    }

    override fun toast(message: UiText) {
        val context = context ?: return
        Toast.makeText(context, message.getBy(context), Toast.LENGTH_SHORT).show()
    }

    override fun getSceneResource(): Resources = resources

    override fun lifecycleOwner(): LifecycleOwner = this

    override fun <I, O> registerForResultCallback(
        contract: ActivityResultContract<I, O>, callback: ActivityResultCallback<O>
    ): ActivityResultLauncher<I> = registerForActivityResult(contract, callback)

    protected open fun onViewModelCreated(viewmodel: VIEWMODEL) {
        observeViewModelDirection(viewModel)
        observeViewModelEvent(viewModel, this)
    }
}

class AlertDialogFragment(private val confirmRequest: ConfirmRequest) : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return confirmRequest.buildAlertDialog(requireContext())
    }
}

fun FragmentActivity.dismissDialogFragmentByTag(tag: String?) {
    supportFragmentManager.findFragmentByTag(tag)?.run {
        when (this) {
            is BaseDialog -> if (isShow) dismissAllowingStateLoss()
            is DialogFragment -> dismissAllowingStateLoss()
        }
    }
}

fun Fragment.dismissDialogFragmentByTag(tag: String?) {
    childFragmentManager.findFragmentByTag(tag)?.run {
        when (this) {
            is BaseDialog -> if (isShow) dismissAllowingStateLoss()
            is DialogFragment -> dismissAllowingStateLoss()
        }
    }
}