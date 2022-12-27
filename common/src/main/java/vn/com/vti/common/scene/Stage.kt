@file:Suppress("unused")

package vn.com.vti.common.scene

import androidx.activity.ComponentActivity
import androidx.annotation.MainThread
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.navigation.fragment.findNavController
import vn.com.vti.common.model.ConfirmRequest
import vn.com.vti.common.model.UiText
import vn.com.vti.common.viewmodel.Direction
import vn.com.vti.common.viewmodel.impl.BaseAndroidViewModel
import kotlin.reflect.KClass

interface Stage {

    fun notifyBlockingTaskStart()

    fun notifyBlockingTaskFinish()

    fun showMessage(request: ConfirmRequest)

    fun toast(message: UiText)

    fun onDispatchDirectionEvent(direction: Direction): Boolean = false

    fun toast(text: String) = toast(UiText.of(text))
}

inline fun <reified T> Fragment.setResultAndFinishByStateHandle(name: String, value: T) {
    findNavController().run {
        previousBackStackEntry?.savedStateHandle?.set(
            name, value
        )
        navigateUp()
    }
}

inline fun <reified T> Fragment.getSavedStateLiveData(
    name: String, clearPrevious: Boolean = false
): LiveData<T>? {
    return findNavController().currentBackStackEntry?.savedStateHandle?.let {
        if (clearPrevious && it.contains(name)) it.remove<T>(name)
        it.getLiveData(name)
    }
}

fun Fragment.cancelResultByStateHandleAndFinish(name: String) {
    findNavController().run {
        previousBackStackEntry?.savedStateHandle?.remove<Any>(name)
        navigateUp()
    }
}

inline fun <reified T : Any> Fragment.createOneShotLiveData(
    name: String, crossinline observer: (T) -> Unit
) {
    findNavController().currentBackStackEntry?.savedStateHandle?.run {
        remove<T>(name)
        getLiveData<T>(name).observe(this@createOneShotLiveData, Observer { model ->
            if (model == null) return@Observer
            observer(model)
            findNavController().currentBackStackEntry?.savedStateHandle?.let {
                it.getLiveData<T>(name).removeObservers(this@createOneShotLiveData)
                it.remove<T>(name)
            }
        })
    }
}

fun <T> Fragment.observeSavedStateHandle(
    name: String, observer: Observer<T>
) {
    findNavController().currentBackStackEntry?.savedStateHandle?.run {
        remove<T>(name)
        getLiveData<T>(name).observe(this@observeSavedStateHandle, observer)
    }
}

@MainThread
inline fun <reified VM : BaseAndroidViewModel> ComponentActivity.absViewModels(
): Lazy<VM> = AbsViewModelLazy(VM::class,
    { viewModelStore },
    { defaultViewModelProviderFactory },
    { defaultViewModelCreationExtras })

@MainThread
inline fun <reified VM : BaseAndroidViewModel> Fragment.absViewModels(
) = AbsViewModelLazy(VM::class,
    { viewModelStore },
    { defaultViewModelProviderFactory },
    { defaultViewModelCreationExtras })

@MainThread
inline fun <reified VM : BaseAndroidViewModel> Fragment.absActivityViewModels() = AbsViewModelLazy(
    VM::class,
    { requireActivity().viewModelStore },
    { requireActivity().defaultViewModelProviderFactory },
    { requireActivity().defaultViewModelCreationExtras },
    invokeOnCreate = false
)

@MainThread
inline fun <reified VM : BaseAndroidViewModel> Fragment.absParentViewModels() =
    AbsViewModelLazy(
        VM::class,
        { requireParentFragment().viewModelStore },
        { requireParentFragment().defaultViewModelProviderFactory },
        { requireParentFragment().defaultViewModelCreationExtras },
        invokeOnCreate = false
    )

/**
 * Copy from [androidx.lifecycle.ViewModelLazy] to add a call for [BaseAndroidViewModel.onCreate] method
 */
class AbsViewModelLazy<VM : BaseAndroidViewModel>(
    private val viewModelClass: KClass<VM>,
    private val storeProducer: () -> ViewModelStore,
    private val factoryProducer: () -> ViewModelProvider.Factory,
    private val creationExtrasProducer: () -> CreationExtras = { CreationExtras.Empty },
    private val invokeOnCreate: Boolean = true
) : Lazy<VM> {

    private var cached: VM? = null

    override val value: VM
        get() {
            val viewModel = cached
            return if (viewModel == null) {
                val factory = factoryProducer()
                val store = storeProducer()
                val extras = creationExtrasProducer()
                ViewModelProvider(store, factory, extras)[viewModelClass.java].also {
                    cached = it
                    if (invokeOnCreate) it.onCreate()
                }
            } else viewModel
        }

    override fun isInitialized(): Boolean = cached != null
}

