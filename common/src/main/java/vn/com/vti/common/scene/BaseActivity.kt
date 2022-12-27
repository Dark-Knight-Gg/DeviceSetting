package vn.com.vti.common.scene

import android.content.Intent
import android.content.res.Resources
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.annotation.CallSuper
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.findNavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import timber.log.Timber
import vn.com.vti.common.model.ConfirmRequest
import vn.com.vti.common.model.UiText
import vn.com.vti.common.util.livedata.BlockingTaskCountObserver
import vn.com.vti.common.util.livedata.RefCountController
import vn.com.vti.common.util.weakLazily
import vn.com.vti.common.viewmodel.*

abstract class BaseActivity<VIEWMODEL : AbsViewModel> : AppCompatActivity(), Scene, Stage {

    companion object {
        private const val TAG_LOADING_DIALOG = "TAG_LOADING_DIALOG"
        private const val TAG_CONFIRM_DIALOG = "TAG_CONFIRM_DIALOG"
    }

    private val runningTaskCount = RefCountController()

    /**
     * Get viewmodel which currently attached to this scene, may be null in [.onSceneCreate]
     *
     * @return the viewmodel
     */
    protected abstract val viewModel: VIEWMODEL

    private val lock = Mutex()
    private val loadingDialogWeakReference: DialogFragment by weakLazily { provideLoadingDialog() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        onSceneCreate(savedInstanceState)
        runningTaskCount.counter.collectOnLifecycle(
            this,
            repeatOnState = Lifecycle.State.CREATED,
            BlockingTaskCountObserver(this::class.java.simpleName, onBlocking = {
                showLoading()
            }, onUnblocking = {
                dismissLoading()
            })
        )
        val viewBinding = onCreateViewDataBinding().apply {
            lifecycleOwner = this@BaseActivity
        }
        viewModel.run {
            onAttachScene(this@BaseActivity)
            onBind(intent?.extras)
            getViewModelVariableId().let {
                if (it != BinderConst.NOT_BINDING) {
                    viewBinding.setVariable(it, this)
                }
            }
            onViewModelCreated(this)
            onReady()
        }
        Timber.d("INIT s=${javaClass.simpleName} v=${viewBinding.javaClass.simpleName} vm=${viewModel.javaClass.simpleName}")
        viewBinding.executePendingBindings()
        onSceneReady(savedInstanceState)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        intent?.run {
            if (viewModel.onNewArguments(action, extras)) {
                setIntent(this)
            }
        }
    }

    @CallSuper
    override fun onDestroy() {
        super.onDestroy()
        viewModel.run {
            onUnbind()
            onDetachScene(this@BaseActivity)
        }
    }

    @Synchronized
    override fun notifyBlockingTaskStart() {
        runningTaskCount.increase()
    }

    @Synchronized
    override fun notifyBlockingTaskFinish() {
        runningTaskCount.decrease()
    }

    open fun showLoading() {
        lifecycleScope.launch(Dispatchers.Main) {
            lock.withLock {
                provideLoadingDialog().run {
                    show(this@BaseActivity.supportFragmentManager, TAG_LOADING_DIALOG)
                    lifecycleScope.launch {
                        delay(500)
                        if (runningTaskCount.counter.value == 0) {
                            this@BaseActivity.dismissDialogFragmentByTag(TAG_LOADING_DIALOG)
                        }
                    }
                }
            }
        }
    }

    open fun dismissLoading() {
        lifecycleScope.launch(Dispatchers.Main) {
            lock.withLock {
                dismissDialogFragmentByTag(TAG_LOADING_DIALOG)
            }
        }
    }

    override fun showMessage(request: ConfirmRequest) {
        dismissDialogFragmentByTag(TAG_CONFIRM_DIALOG)
        provideConfirmDialog(request).show(supportFragmentManager, TAG_CONFIRM_DIALOG)
    }

    override fun toast(message: UiText) {
        Toast.makeText(this, message.getBy(this), Toast.LENGTH_SHORT).show()
    }

    /**
     * Create content viewbinding
     *
     * @return the content viewbinding
     */
    protected abstract fun onCreateViewDataBinding(): ViewDataBinding

    /**
     * Support function for [.setContentView] with create viewbinding from layout resource id
     *
     * @param layoutRes the layout resource id
     * @param <T>       type of generated binding class
     * @return the viewbinding
     */
    protected fun <T : ViewDataBinding> setContentViewBinding(@LayoutRes layoutRes: Int): T {
        return DataBindingUtil.setContentView(this, layoutRes)
    }

    /**
     * Define view-model variables to bind viewmodel with viewbinding
     *
     * @return viewmodel variables id (BR.vm,... or [BinderConst.DEFAULT])
     */
    protected abstract fun getViewModelVariableId(): Int

    protected open fun provideLoadingDialog(): DialogFragment = DialogFragment()

    protected open fun provideConfirmDialog(confirmRequest: ConfirmRequest): DialogFragment =
        AlertDialogFragment(confirmRequest)

    protected open fun provideNavHostId(): Int? = null

    /**
     * Call when scene starts to initialize
     */
    protected open fun onSceneCreate(savedInstanceState: Bundle?) {
        //intentionally blank
    }

    /**
     * Call after scene finish it's initialization
     */
    protected open fun onSceneReady(savedInstanceState: Bundle?) {
        //intentionally blank
    }

    /**
     * For handle direction event which passes from a child fragment
     * (usually a navigation event from slider menu)
     */
    open fun dispatchDelegationDirectionEvent(direction: Direction) {
        defaultHandleDirection(direction, provideNavHostId())
    }

    protected open fun onViewModelCreated(viewModel: VIEWMODEL) {
        observeViewModelDirection(viewModel, provideNavHostId())
        observeViewModelEvent(viewModel, this)
    }

    protected fun getDefaultNavController(): NavController? = provideNavHostId()?.let {
        findNavController(it)
    }

    override fun getSceneResource(): Resources = resources

    override fun lifecycleOwner(): LifecycleOwner = this

    override fun <I, O> registerForResultCallback(
        contract: ActivityResultContract<I, O>, callback: ActivityResultCallback<O>
    ): ActivityResultLauncher<I> = registerForActivityResult(contract, callback)

    protected fun getCurrentFragment(): Fragment? = provideNavHostId()?.let {
        supportFragmentManager.findFragmentById(it)?.childFragmentManager?.fragments?.getOrNull(0)
    }
}
