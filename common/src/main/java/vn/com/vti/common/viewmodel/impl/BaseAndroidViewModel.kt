package vn.com.vti.common.viewmodel.impl

import android.app.Activity
import android.app.Application
import android.content.Intent
import android.content.res.Resources
import android.os.Bundle
import androidx.annotation.CallSuper
import androidx.annotation.MainThread
import androidx.annotation.StringRes
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavArgs
import androidx.navigation.NavArgsLazy
import androidx.navigation.NavDirections
import androidx.navigation.NavOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber
import vn.com.vti.common.adapter.binding.MutableSnapshotListFlow
import vn.com.vti.common.adapter.binding.SnapshotScope
import vn.com.vti.common.model.ConfirmRequest
import vn.com.vti.common.model.UiText
import vn.com.vti.common.util.livedata.RefCountController
import vn.com.vti.common.util.weak
import vn.com.vti.common.viewmodel.*
import kotlin.coroutines.CoroutineContext

open class BaseAndroidViewModel(application: Application) : AndroidViewModel(application),
    AbsViewModel {

    protected var sceneRefs by weak<Scene>()
        private set

    var argumentRefs by weak<Bundle>()
        private set

    private val mRunningTaskCount = RefCountController()

    private val mDirectionPublisherChannel = eventChannelOf<Direction>()

    private val mDirectionPublisher by lazy {
        mDirectionPublisherChannel.receiveAsFlow()
    }

    private val mConfirmEventChannel = eventChannelOf<ConfirmRequest>()

    private val mConfirmEvent by lazy {
        mConfirmEventChannel.receiveAsFlow()
    }

    private val mToastEventChannel = eventChannelOf<UiText>()

    private val mToastEvent by lazy {
        mToastEventChannel.receiveAsFlow()
    }

    override fun onCreate() {
        Timber.d("BaseAndroidViewModel ${javaClass.simpleName} created")
    }

    @CallSuper
    override fun onBind(args: Bundle?) {
        argumentRefs = args
    }

    override fun onNewArguments(action: String?, args: Bundle?): Boolean = true

    @CallSuper
    override fun onAttachScene(scene: Scene) {
        sceneRefs = scene
    }

    override fun onReady() {
        //intentionally blank: default implementation
    }

    @CallSuper
    override fun onDetachScene(scene: Scene) {
        sceneRefs = null
    }

    override fun onUnbind() {
        mRunningTaskCount.clearRefCount()
        argumentRefs = null
    }

    override fun getDirections(): Flow<Direction> = mDirectionPublisher

    override fun getRunningTaskCount(): StateFlow<Int> = mRunningTaskCount.counter

    override fun getConfirmEvent(): Flow<ConfirmRequest> = mConfirmEvent

    override fun getToastEvent(): Flow<UiText> = mToastEvent

    protected open fun redirect(intent: Intent) {
        mDirectionPublisherChannel.sendByViewModelScope(IntentDirection(intent))
    }

    protected open fun redirect(
        direction: NavDirections,
        options: NavOptions? = null,
        finish: Boolean = false
    ) {
        mDirectionPublisherChannel.sendByViewModelScope(
            NavActionDirection(
                direction,
                options,
                finish
            )
        )
    }

    protected open fun redirect(activityClass: Class<out Activity>, finish: Boolean = false) {
        mDirectionPublisherChannel.sendByViewModelScope(
            IntentDirection(
                Intent(
                    getApplication(),
                    activityClass
                ), finish = finish
            )
        )
    }

    protected open fun redirect(direction: Direction) {
        mDirectionPublisherChannel.sendByViewModelScope(direction)
    }

    protected open fun notifyTaskStart() {
        mRunningTaskCount.increase()
    }

    protected open fun notifyTaskFinish() {
        mRunningTaskCount.decrease()
    }

    protected open fun notifyAllTaskCleared() {
        mRunningTaskCount.clearRefCount()
    }

    protected open fun requestConfirm(request: ConfirmRequest) {
        mConfirmEventChannel.sendByViewModelScope(request)
    }

    protected open fun toast(@StringRes resId: Int) {
        mToastEventChannel.sendByViewModelScope(UiText.of(resId))
    }

    protected open fun toast(text: String) {
        mToastEventChannel.sendByViewModelScope(UiText.of(text))
    }

    protected val resource: Resources
        get() = sceneRefs?.getSceneResource() ?: getApplication<Application>().resources

    protected fun <T> MutableSharedFlow<T>.emitOnViewModelScope(value: T) {
        viewModelScope.launch {
            this@emitOnViewModelScope.emit(value)
        }
    }

    protected fun <T> Channel<T>.sendByViewModelScope(value: T) {
        viewModelScope.launch {
            this@sendByViewModelScope.send(value)
        }
    }

    protected fun <T> Flow<T>.collectLatestOnViewModelScope(collector: (T) -> Unit) {
        viewModelScope.launch {
            collectLatest(collector)
        }
    }

    protected fun <T> MutableSnapshotListFlow<T>.commitOnViewModelScope(transaction: suspend (SnapshotScope<T>.() -> Unit)) {
        viewModelScope.launch {
            commit(transaction)
        }
    }
}

@MainThread
inline fun <reified Args : NavArgs> BaseAndroidViewModel.navArgs() = NavArgsLazy(Args::class) {
    argumentRefs ?: throw IllegalStateException("ViewModel $this has null arguments")
}

fun <T> eventChannelOf(buffer: Int = 1, strategy: BufferOverflow = BufferOverflow.DROP_OLDEST) =
    Channel<T>(capacity = buffer, onBufferOverflow = strategy)

fun <T> sharedEventFlow(buffer: Int = 1, strategy: BufferOverflow = BufferOverflow.DROP_OLDEST) =
    MutableSharedFlow<T>(extraBufferCapacity = buffer, onBufferOverflow = strategy)

val ViewModel.useCaseContext: CoroutineContext get() = viewModelScope.coroutineContext + Dispatchers.IO