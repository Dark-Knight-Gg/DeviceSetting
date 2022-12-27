@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package vn.com.vti.common.scene

import android.content.res.Resources
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.annotation.CallSuper
import androidx.core.view.MenuProvider
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import timber.log.Timber
import vn.com.vti.common.model.ConfirmRequest
import vn.com.vti.common.model.UiText
import vn.com.vti.common.viewmodel.AbsViewModel
import vn.com.vti.common.viewmodel.Scene
import vn.com.vti.common.viewmodel.observeViewModelDirection
import vn.com.vti.common.viewmodel.observeViewModelEvent
import java.lang.ref.WeakReference

@Suppress("unused")
abstract class BaseFragment : Fragment(), Scene, Stage {

    private var baseActivityWeakReference: WeakReference<BaseActivity<*>?>? = null

    @CallSuper
    override fun onDestroy() {
        super.onDestroy()
        baseActivityWeakReference = null
    }

    protected fun getBaseActivity(): BaseActivity<*>? {
        return baseActivityWeakReference?.get() ?: (activity as? BaseActivity<*>)?.also {
            baseActivityWeakReference = WeakReference(it)
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
        contract: ActivityResultContract<I, O>,
        callback: ActivityResultCallback<O>
    ): ActivityResultLauncher<I> = registerForActivityResult(contract, callback)

    protected inline fun addMenuProvider(
        state: Lifecycle.State = Lifecycle.State.RESUMED,
        lifecycleOwner: LifecycleOwner = viewLifecycleOwner,
        crossinline fnCreateMenu: (menu: Menu, menuInflater: MenuInflater) -> Unit,
        crossinline fnMenuItemSelected: (menuItem: MenuItem) -> Boolean
    ): MenuProvider {
        return object : MenuProvider {

            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                fnCreateMenu(menu, menuInflater)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean =
                fnMenuItemSelected(menuItem)

        }.also {
            requireActivity().addMenuProvider(it, lifecycleOwner, state)
        }
    }

    protected inline fun addMenuProvider(
        state: Lifecycle.State = Lifecycle.State.RESUMED,
        lifecycleOwner: LifecycleOwner = viewLifecycleOwner,
        crossinline fnCreateMenu: (menu: Menu, menuInflater: MenuInflater) -> Unit,
        crossinline fnMenuItemSelected: (menuItem: MenuItem) -> Boolean,
        crossinline fnPrepareMenu: (menu: Menu) -> Unit
    ): MenuProvider {
        return object : MenuProvider {

            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                fnCreateMenu(menu, menuInflater)
            }

            override fun onPrepareMenu(menu: Menu) {
                fnPrepareMenu(menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean =
                fnMenuItemSelected(menuItem)

        }.also {
            requireActivity().addMenuProvider(it, lifecycleOwner, state)
        }
    }
}

@Suppress("unused")
abstract class BaseVmFragment<VIEWMODEL : AbsViewModel> :
    BaseFragment() {

    /**
     * Get viewmodel which currently attached to this scene, may be null in [.onSceneCreate]
     *
     * @return the viewmodel
     */
    protected abstract val viewModel: VIEWMODEL

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.run {
            onBind(arguments)
            onAttachScene(this@BaseVmFragment)
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

    @CallSuper
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        bindViewModelToUiView()
    }

    @CallSuper
    override fun onDestroy() {
        viewModel.run {
            onDetachScene(this@BaseVmFragment)
            onUnbind()
        }
        super.onDestroy()
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

    private fun bindViewModelToUiView() {
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
        observeViewModelEvent(viewModel, this)
        observeViewModelDirection(viewModel)
    }

    override fun getSceneResource(): Resources = resources

    override fun lifecycleOwner(): LifecycleOwner = this

    override fun <I, O> registerForResultCallback(
        contract: ActivityResultContract<I, O>,
        callback: ActivityResultCallback<O>
    ): ActivityResultLauncher<I> = registerForActivityResult(contract, callback)

    /**
     * Call after scene finish it's initialization
     */
    protected open fun onSceneReady() {}
}

inline fun Fragment.pagerRegisterOnBackpressHandler(crossinline handler: (() -> Boolean)): LifecycleObserver {
    val callback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            if (handler()) {
                return
            }
            if (childFragmentManager.backStackEntryCount > 0) {
                childFragmentManager.popBackStack()
                return
            }
            isEnabled = false
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
    }
    requireActivity().onBackPressedDispatcher.addCallback(this, callback)
    childFragmentManager.addOnBackStackChangedListener {
        callback.isEnabled = childFragmentManager.backStackEntryCount > 0
    }
    return object : DefaultLifecycleObserver {

        override fun onResume(owner: LifecycleOwner) {
            super.onResume(owner)
            callback.isEnabled = childFragmentManager.backStackEntryCount > 0
        }

        override fun onPause(owner: LifecycleOwner) {
            super.onPause(owner)
            callback.isEnabled = false
        }

    }.also {
        lifecycle.addObserver(it)
    }
}

fun FragmentManager.currentNavigationFragment(): Fragment? =
    primaryNavigationFragment?.childFragmentManager?.fragments?.first()