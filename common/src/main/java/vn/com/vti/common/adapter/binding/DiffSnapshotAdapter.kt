@file:Suppress("unused")

package vn.com.vti.common.adapter.binding

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.AdapterListUpdateCallback
import androidx.recyclerview.widget.AsyncDifferConfig
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import timber.log.Timber
import vn.com.vti.common.util.weak
import vn.com.vti.common.viewmodel.collectLatestOnLifecycle

@Suppress("MemberVisibilityCanBePrivate")
abstract class DiffUtilBindingArrayAdapter<M>(
    diffCallback: DiffUtil.ItemCallback<M>,
) : BindingAdapter<M>() {

    private var connectedJob: Job? by weak()

    private val currentListChangedListener: AsyncListDiffer.ListListener<M> =
        AsyncListDiffer.ListListener<M> { previousList, currentList ->
            this@DiffUtilBindingArrayAdapter.onCurrentListChanged(
                previousList, currentList
            )
        }

    @Suppress("LeakingThis")
    private val asyncListDiffer: AsyncListDiffer<M> = AsyncListDiffer<M>(
        AdapterListUpdateCallback(this), AsyncDifferConfig.Builder(diffCallback).build()
    ).also {
        it.addListListener(currentListChangedListener)
    }

    override fun getItem(position: Int): M = asyncListDiffer.currentList[position]

    override fun getItemCount() = asyncListDiffer.currentList.size

    fun connectWith(lifecycleOwner: LifecycleOwner, flow: StateFlow<List<M>>) {
        lifecycleOwner.run {
            lifecycleScope.launch {
                connectedJob?.let {
                    if (it.isActive) it.cancelAndJoin()
                }
                connectedJob = launch {
                    flow.collectLatestOnLifecycle(lifecycleOwner) {
                        asyncListDiffer.submitList(it)
                    }
                }
            }
        }
    }

    fun connectWith(lifecycleOwner: LifecycleOwner, snapshot: SnapshotListFlow<M>) {
        lifecycleOwner.run {
            lifecycleScope.launch {
                connectedJob?.let {
                    if (it.isActive) it.cancelAndJoin()
                }
                connectedJob = launch {
                    try {
                        val source = snapshot.modificationFlow
                        Timber.d(
                            "This $snapshot supports modification-flow, we recommend you to use DiffUtilBindingArrayAdapter with DiffSnapshotListFlow"
                        )
                        source.collectLatestOnLifecycle(lifecycleOwner) {
                            asyncListDiffer.submitList(snapshot.data.toMutableList())
                        }
                    } catch (e: Exception) {
                        Timber.d("This $snapshot support data-source directly")
                        snapshot.dataFlow.collectLatestOnLifecycle(lifecycleOwner) {
                            asyncListDiffer.submitList(it)
                        }
                    }
                }
            }
        }
    }

    open fun onCurrentListChanged(previousList: List<M>, currentList: List<M>) {

    }
}

/**
 * For compatible with [vn.com.vti.common.ui.list.IListController]
 */
class DiffSnapshotListFlow<E>(value: List<E>? = emptyList()) : SnapshotListFlow<E> {

    private val mDataFlow: MutableStateFlow<List<E>> = MutableStateFlow(value ?: emptyList())

    private val mLock: Mutex = Mutex()

    override val dataFlow: StateFlow<List<E>>
        get() = mDataFlow

    override val modificationFlow: Flow<SnapshotModification>
        get() {
            //Consider to using `MutableSnapshotListFlow` if you want to update ui by `SnapshotModification`
            throw IllegalStateException("Not Supported")
        }

    fun setData(data: List<E>) {
        mDataFlow.value = data
    }

    fun launchIn(scope: CoroutineScope, transaction: (MutableList<E>.() -> Unit)) {
        scope.launch {
            commit(transaction)
        }
    }

    suspend fun commit(transaction: (MutableList<E>.() -> Unit)) {
        mLock.withLock {
            mDataFlow.commit(transaction)
        }
    }

    override val lastestVersion: Int = -1
}

@Suppress("FunctionName")
fun <T> MutableStateListFlow(list: List<T> = emptyList()): MutableStateFlow<List<T>> =
    MutableStateFlow(list)

val StateFlow<List<*>>.size: Int
    get() = value.size

fun <T> MutableStateFlow<List<T>>.commit(transaction: MutableList<T>.() -> Unit) {
    this.value = value.toMutableList().also {
        it.transaction()
    }
}

fun <T> MutableStateFlow<List<T>>.add(item: T) {
    this.value = value.toMutableList().also {
        it.add(item)
    }
}

fun <T> MutableStateFlow<List<T>>.add(index: Int, item: T) {
    this.value = value.toMutableList().also {
        it.add(index, item)
    }
}

fun <T> MutableStateFlow<List<T>>.addAll(items: Collection<T>) {
    this.value = value.toMutableList().also {
        it.addAll(items)
    }
}

fun <T> MutableStateFlow<List<T>>.addAll(index: Int, items: Collection<T>) {
    this.value = value.toMutableList().also {
        it.addAll(index, items)
    }
}

fun <T> MutableStateFlow<List<T>>.remove(item: T): Boolean {
    val list = this.value.toMutableList()
    return if (list.remove(item)) {
        this.value = list
        true
    } else false
}

fun <T> MutableStateFlow<List<T>>.removeAt(index: Int) {
    val list = this.value.toMutableList()
    list.removeAt(index)
    this.value = list
}

fun <T> MutableStateFlow<List<T>>.set(index: Int, item: T) {
    val list = this.value.toMutableList()
    list[index] = item
    this.value = list
}

fun <T> MutableStateFlow<List<T>>.clear() {
    this.value = mutableListOf()
}

fun <T> MutableStateFlow<List<T>>.clear(fromIndex: Int, toIndex: Int) {
    val list = this.value.toMutableList()
    list.subList(fromIndex, toIndex).clear()
    this.value = list
}

suspend inline fun <reified ROOT, reified EXPANDABLE> DiffSnapshotListFlow<in ROOT>.flattenExpandableToggleStateByType(
    position: Int,
    crossinline onExtractExpandableNode: (EXPANDABLE) -> List<ROOT>?,
    crossinline onBuildCompletelyExpandableNode: suspend (oldParent: EXPANDABLE) -> EXPANDABLE
) where EXPANDABLE : ROOT {
    val lastIndex = data.lastIndex
    if (position < 0 || position > lastIndex) return
    val data = data
    val currentNode = (data[position] as? EXPANDABLE) ?: return
    if (position == lastIndex || data[position + 1] is EXPANDABLE) {
        // collapsed -> expanding
        val newNode = if (onExtractExpandableNode(currentNode).isNullOrEmpty()) {
            onBuildCompletelyExpandableNode(currentNode)
        } else currentNode
        commit {
            if (currentNode !== newNode) {
                set(position, newNode)
            }
            val newItems = onExtractExpandableNode(newNode)
            if (!newItems.isNullOrEmpty()) addAll(position + 1, newItems)
        }
    } else {
        // expanded -> collapsing
        val start = position + 1
        val count = data.size
        val end = (start until count).firstOrNull {
            data[it] is EXPANDABLE
        } ?: count
        commit {
            subList(start, end).clear()
        }
    }
}

suspend inline fun <reified EXPANDABLE> DiffSnapshotListFlow<EXPANDABLE>.flattenExpandableToggleStateByLevel(
    position: Int,
    crossinline level: EXPANDABLE.() -> Int,
    crossinline onExtractExpandableNode: (EXPANDABLE) -> List<EXPANDABLE>?,
    crossinline onBuildCompletelyExpandableNode: suspend (oldParent: EXPANDABLE) -> EXPANDABLE
) {
    val lastIndex = data.lastIndex
    if (position < 0 || position > lastIndex) return
    val data = data
    val currentNode = data[position]
    val currentLevel = currentNode.level()
    if (position == lastIndex || data[position + 1].level() <= currentLevel) {
        // collapsed -> expanding
        val newNode = if (onExtractExpandableNode(currentNode).isNullOrEmpty()) {
            onBuildCompletelyExpandableNode(currentNode)
        } else currentNode
        commit {
            if (currentNode !== newNode) {
                set(position, newNode)
            }
            val newItems = onExtractExpandableNode(newNode)
            if (!newItems.isNullOrEmpty()) addAll(position + 1, newItems)
        }
    } else {
        // expanded -> collapsing
        val start = position + 1
        val count = data.size
        val end = (start until count).firstOrNull {
            data[it].level() <= currentLevel
        } ?: count
        commit {
            subList(start, end).clear()
        }
    }
}