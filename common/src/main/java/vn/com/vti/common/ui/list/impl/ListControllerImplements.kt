package vn.com.vti.common.ui.list.impl

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import vn.com.vti.common.adapter.BaseAdapter
import vn.com.vti.common.adapter.binding.SnapshotListFlow
import vn.com.vti.common.adapter.itf.OnDataChangedListener
import vn.com.vti.common.annotation.LoadingType
import vn.com.vti.common.model.UiDrawable
import vn.com.vti.common.model.UiText
import vn.com.vti.common.ui.list.IAdapterController
import vn.com.vti.common.ui.list.IListController
import vn.com.vti.common.ui.list.ISnapshotController
import vn.com.vti.common.ui.list.IStateListFlowController

@Suppress("unused", "MemberVisibilityCanBePrivate")
open class ListController(
    scope: CoroutineScope,
    enableEmptyState: Boolean = false,
    loadmoreIndicate: Boolean = false,
    /**
     * Delegate events
     */
    private var callback: Listener? = null,
) : IListController {
    /**
     * Indicate list-content is loading data. It may be init-load, loadmore, refresh-load. Should not use to indicate show or hide indicator
     */
    private val stateOfInternalLoading: MutableStateFlow<Boolean> = MutableStateFlow(false)

    /**
     * Indicate swipe refresh layout state, true if refreshing, false otherwise
     */
    private val stateOfInternalRefreshing: MutableStateFlow<Boolean> = MutableStateFlow(false)

    /**
     * Indicate swipe-to-refresh feature is enable or not
     */
    private val stateOfInternalRefreshEnabled: MutableStateFlow<Boolean> = MutableStateFlow(false)

    /**
     * Indicate list content is empty or not. Should not use to judge empty message and drawable show or hide
     */
    protected val stateOfInternalEmptyState: MutableStateFlow<Boolean> = MutableStateFlow(true)

    private val stateOfLoading: StateFlow<Boolean>

    /**
     * Provide empty drawable
     */
    private val stateOfEmptyDrawable: MutableStateFlow<UiDrawable?> = MutableStateFlow(null)

    /**
     * Provide empty message
     */
    private val stateOfEmptyMessage: MutableStateFlow<UiText?> = MutableStateFlow(null)

    /**
     * Indicate empty-state of list-content after combining state. It's @[combine]
     */
    private val stateOfEmptyState: StateFlow<Boolean>

    /**
     * Indicate loading-state of list-content after combining state. Use to show blocking-loading-indicator. It's @[combine]
     * <br></br>Can be change combining-method by override @[ListController.combineLoadingState]
     */
    override val isLoading: StateFlow<Boolean>
        get() = stateOfLoading

    /**
     * Indicate loadmore-state of list-content after combining state. Use to show blocking-loading-indicator.
     * It's @[combine]
     * <br></br>Can be change combining-method by override @[ListController.combineLoadingMoreState]
     */
    private val stateOfLoadingMore: StateFlow<Boolean>

    override val isLoadingMore: StateFlow<Boolean>
        get() = stateOfLoadingMore

    override fun observableEmptyMessage(): StateFlow<UiText?> = stateOfEmptyMessage

    override fun observableEmptyDrawable(): StateFlow<UiDrawable?> = stateOfEmptyDrawable

    override val isRefreshing: StateFlow<Boolean>
        get() = stateOfInternalRefreshing

    override val isRefreshEnabled: StateFlow<Boolean>
        get() = stateOfInternalRefreshEnabled

    override val isEmptyData: StateFlow<Boolean>
        get() = stateOfEmptyState

    override fun onRecyclerLoadmore(totalItemsCount: Int, view: RecyclerView) {
        if (view.adapter?.itemCount == 0) return
        callback?.onContentLoadmore(totalItemsCount, this)
    }

    override fun onSwipeRefresh() {
        stateOfInternalRefreshing.value = true
        callback?.onContentRefresh(this)
    }

    override fun onRequestTryAgain() {
        callback?.onRequestTryAgain(this)
    }

    override fun notifyLoaderStarted(loadingType: Int): Boolean {
        if (loadingType != LoadingType.BLOCKING) {
            stateOfInternalLoading.value = true
            return true
        }
        return false
    }

    override fun notifyLoaderFinished(loadingType: Int): Boolean {
        if (loadingType != LoadingType.BLOCKING) {
            stateOfInternalLoading.value = false
            stateOfInternalRefreshing.value = false
            return true
        }
        return false
    }

    protected fun combineLoadingMoreState(
        flagLoading: Boolean, flagRefreshing: Boolean, flagEmptyData: Boolean
    ): Boolean = flagLoading && !flagRefreshing && !flagEmptyData

    protected fun combineLoadingState(
        flagLoading: Boolean, flagRefreshing: Boolean, flagEmptyData: Boolean
    ): Boolean = flagLoading && !flagRefreshing && flagEmptyData

    protected fun combineEmptyState(
        flagLoading: Boolean, flagRefreshing: Boolean, flagEmptyData: Boolean
    ): Boolean = !flagLoading && !flagRefreshing && flagEmptyData

    fun setRefreshEnable(enable: Boolean) {
        stateOfInternalRefreshEnabled.value = enable
    }

    fun setEmptyDrawable(@DrawableRes drawableResId: Int) {
        stateOfEmptyDrawable.value = drawableResId.takeIf { it != 0 }?.let {
            UiDrawable.ofResourceId(drawableResId)
        }
    }

    fun setEmptyMessage(message: String?) {
        stateOfEmptyMessage.value = message?.let { UiText.of(it) }
    }

    fun setEmptyMessage(@StringRes messageResId: Int) {
        stateOfEmptyMessage.value = UiText.of(messageResId)
    }

    interface Listener {

        fun onContentLoadmore(
            totalItemsCount: Int, controller: IListController
        )

        fun onContentRefresh(controller: IListController)

        fun onRequestTryAgain(controller: IListController)
    }

    init {
        stateOfEmptyState = if (enableEmptyState) combine(
            stateOfInternalLoading, stateOfInternalRefreshing, stateOfInternalEmptyState
        ) { loading, refreshing, empty ->
            combineEmptyState(loading, refreshing, empty)
        }.stateIn(scope, SharingStarted.Eagerly, false)
        else MutableStateFlow(false)
        stateOfLoading = combine(
            stateOfInternalLoading, stateOfInternalRefreshing, stateOfInternalEmptyState
        ) { loading, refreshing, empty ->
            combineLoadingState(loading, refreshing, empty)
        }.stateIn(scope, SharingStarted.Eagerly, false)
        stateOfLoadingMore = if (loadmoreIndicate) combine(
            stateOfInternalLoading, stateOfInternalRefreshing, stateOfInternalEmptyState
        ) { loading, refreshing, empty ->
            combineLoadingMoreState(loading, refreshing, empty)
        }.stateIn(scope, SharingStarted.Eagerly, false)
        else MutableStateFlow(false)
    }
}

open class SnapshotListController<T>(
    scope: CoroutineScope,
    private val snapshotListFlow: SnapshotListFlow<T>,
    enableEmptyState: Boolean = false,
    loadmoreIndicate: Boolean = false,
    callback: Listener? = null,
) : ListController(scope, enableEmptyState, loadmoreIndicate, callback), ISnapshotController<T> {

    init {
        scope.launch(Dispatchers.IO) {
            try {
                val source = snapshotListFlow.modificationFlow
                source.collectLatest {
                    stateOfInternalEmptyState.value = snapshotListFlow.data.isEmpty()
                }
            } catch (e: Exception) {
                snapshotListFlow.dataFlow.collectLatest {
                    stateOfInternalEmptyState.value = it.isEmpty()
                }
            }
        }
    }

    override val dataSource: SnapshotListFlow<T>
        get() = snapshotListFlow

}

open class AdapterListController(
    scope: CoroutineScope,
    private val adapter: BaseAdapter<*>,
    enableEmptyState: Boolean = false,
    loadmoreIndicate: Boolean = false,
    callback: Listener? = null,
) : ListController(scope, enableEmptyState, loadmoreIndicate, callback), IAdapterController {

    init {
        adapter.setDataChangedListener(object : OnDataChangedListener {
            override fun onDataSetEmpty() {
                stateOfInternalEmptyState.value = true
            }

            override fun onDataSetFilled() {
                stateOfInternalEmptyState.value = false
            }
        })
        if (adapter.itemCount > 0) {
            stateOfInternalEmptyState.value = false
        }
    }

    override val dataAdapter: RecyclerView.Adapter<*>
        get() = adapter
}

open class StateListFlowController<T>(
    scope: CoroutineScope,
    private val stateListFlow: StateFlow<List<T>>,
    enableEmptyState: Boolean = false,
    loadmoreIndicate: Boolean = false,
    callback: Listener? = null,
) : ListController(scope, enableEmptyState, loadmoreIndicate, callback),
    IStateListFlowController<T> {

    init {
        scope.launch(Dispatchers.IO) {
            stateListFlow.collectLatest {
                stateOfInternalEmptyState.value = it.isEmpty()
            }
        }
    }

    override val stateFlow: StateFlow<List<T>>
        get() = stateListFlow

}