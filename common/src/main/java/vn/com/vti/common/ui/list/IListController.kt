package vn.com.vti.common.ui.list

import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.flow.StateFlow
import vn.com.vti.common.adapter.binding.SnapshotListFlow
import vn.com.vti.common.annotation.LoadingType
import vn.com.vti.common.model.UiDrawable
import vn.com.vti.common.model.UiText

interface ISnapshotController<T> : IListController {

    val dataSource: SnapshotListFlow<T>
}

interface IAdapterController : IListController {

    val dataAdapter: RecyclerView.Adapter<*>
}

interface IStateListFlowController<T> : IListController {

    val stateFlow: StateFlow<List<T>>
}

interface IListController {
    /**
     * indicate loading state
     *
     * @return observable
     */
    val isLoading: StateFlow<Boolean>

    /**
     * indicate loading more state
     *
     * @return observable
     */
    val isLoadingMore: StateFlow<Boolean>

    /**
     * indicate refreshing state
     *
     * @return observable
     */
    val isRefreshing: StateFlow<Boolean>

    /**
     * indicate enable or disable refresh-action
     *
     * @return observable
     */
    val isRefreshEnabled: StateFlow<Boolean>

    /**
     * indiate empty data state
     *
     * @return observable, null if disable empty-data
     */
    val isEmptyData: StateFlow<Boolean>

    /**
     * indiate empty message
     *
     * @return observable, null if disable empty-data
     */
    fun observableEmptyMessage(): StateFlow<UiText?>

    /**
     * indiate empty drawable
     *
     * @return observable, null if disable empty-data
     */
    fun observableEmptyDrawable(): StateFlow<UiDrawable?>

    /**
     * provide loadmore listener for recycler view
     */
    fun onRecyclerLoadmore(totalItemsCount: Int, view: RecyclerView)

    /**
     * trigger when user swipe to refresh
     */
    fun onSwipeRefresh()

    /**
     * trigger when user click on empty icon or message to reload
     */
    fun onRequestTryAgain()

    /**
     * Call when a loading-action starts
     *
     * @return true if handle loading, false otherwise
     */
    fun notifyLoaderStarted(@LoadingType loadingType: Int): Boolean

    /**
     * Call when a loading-action finished
     *
     * @return true if handle loading, false otherwise
     */
    fun notifyLoaderFinished(@LoadingType loadingType: Int): Boolean

    fun load() {}
}