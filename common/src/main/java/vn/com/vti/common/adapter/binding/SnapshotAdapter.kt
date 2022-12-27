package vn.com.vti.common.adapter.binding

import android.annotation.SuppressLint
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import timber.log.Timber
import vn.com.vti.common.util.weak

@Suppress("MemberVisibilityCanBePrivate")
abstract class SnapshotBindingArrayAdapter<M> : BindingAdapter<M>(), ISnapshotAdapter {

    private var connectedJob: Job? by weak()

    private var snapshotSource: SnapshotListFlow<out M>? by weak()

    var dataList: List<M> = emptyList()

    override fun getItem(position: Int): M = dataList[position]

    override fun getItemCount() = dataList.size

    @SuppressLint("NotifyDataSetChanged")
    open fun updateRefBySnapshotData() {
        val data = snapshotSource?.data ?: return
        dataList = data
        notifyDataSetChanged()
    }

    fun connectWith(lifecycleOwner: LifecycleOwner, snapshot: SnapshotListFlow<out M>) {
        lifecycleOwner.run {
            lifecycleScope.launch {
                connectedJob?.let {
                    if (it.isActive) it.cancelAndJoin()
                }
                connectedJob = launch {
                    repeatOnLifecycle(Lifecycle.State.STARTED) {
                        snapshotSource = snapshot
                        updateRefBySnapshotData()
                        snapshot.modificationFlow.collectLatest {
                            if (snapshot.lastestVersion == it.version) it.dispatchToAdapter(this@SnapshotBindingArrayAdapter)
                            else updateRefBySnapshotData()
                        }
                    }
                }
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onAddModification(index: Int, count: Int) {
        if (index == 0 && count == itemCount) {
            //Ignore Inconsistency detected
            notifyDataSetChanged()
        } else if (count > 1) notifyItemRangeInserted(
            index, count
        ) else notifyItemInserted(index)
    }

    override fun onUpdateModification(index: Int, count: Int) {
        if (count > 1) notifyItemRangeChanged(
            index, count
        ) else notifyItemChanged(index)
    }

    override fun onRemoveModification(index: Int, count: Int) {
        if (count > 1) notifyItemRangeRemoved(
            index, count
        ) else notifyItemRemoved(index)
    }

    override fun onAllModification(oldCount: Int, newCount: Int) {
        if (oldCount == newCount) {
            notifyItemRangeChanged(0, oldCount)
        } else {
            // If it throws `Inconsistency detected` change this `else` body to adapter.notifyDataSetChanged() with @SuppressLint("NotifyDataSetChanged")
            notifyItemRangeRemoved(0, oldCount)
            notifyItemRangeInserted(0, newCount)
        }
    }

    override fun onReferenceModification() {
        updateRefBySnapshotData()
    }
}

interface SnapshotListFlow<E> {

    val dataFlow: StateFlow<List<E>>

    val modificationFlow: Flow<SnapshotModification>

    val lastestVersion: Int

    val data: List<E>
        get() = dataFlow.value
}

class MutableSnapshotListFlow<E>(value: List<E>? = emptyList()) : SnapshotListFlow<E> {

    private val mDataFlow: MutableStateFlow<MutableList<E>> =
        MutableStateFlow(value?.toMutableList() ?: mutableListOf())

    private val mLock: Mutex = Mutex()

    private val mModificationFlow = Channel<SnapshotModification>(
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    override val dataFlow: StateFlow<List<E>>
        get() = mDataFlow

    override val modificationFlow: Flow<SnapshotModification> by lazy {
        mModificationFlow.receiveAsFlow()
            .shareIn(CoroutineScope(Dispatchers.IO), SharingStarted.Eagerly)
    }

    suspend fun setData(data: List<E>) {
        withContext(Dispatchers.IO) {
            Timber.d("SnapshotBindingArrayAdapter set data ${data.size}")
            mDataFlow.value = data.toMutableList()
            mModificationFlow.send(ReferenceModification)
        }
    }

    fun launchIn(scope: CoroutineScope, transaction: suspend (SnapshotScope<E>.() -> Unit)) {
        scope.launch {
            mLock.withLock {
                SnapshotScope(++mLastestVersion, mDataFlow.value, mModificationFlow).transaction()
            }
        }
    }

    suspend fun commit(transaction: suspend (SnapshotScope<E>.() -> Unit)) {
        mLock.withLock {
            SnapshotScope(++mLastestVersion, mDataFlow.value, mModificationFlow).transaction()
        }
    }

    suspend fun commitAll(transaction: suspend ((MutableList<E>) -> Unit)) {
        mLock.withLock {
            val data = mDataFlow.value
            val oldCount = data.size
            val newCount = coroutineScope {
                transaction(data)
                data.size
            }
            mModificationFlow.send(AllModification(++mLastestVersion, oldCount, newCount))
        }
    }

    private var mLastestVersion: Int = 0

    override val lastestVersion: Int
        get() = mLastestVersion
}

class SnapshotScope<E>(
    private val version: Int,
    private val mutableList: MutableList<E>,
    private val flow: Channel<SnapshotModification>
) {

    val value: List<E>
        get() = mutableList

    suspend fun add(data: E) {
        mutableList.add(data)
        flow.send(AddModification(version, mutableList.lastIndex, 1))
    }

    suspend fun add(index: Int, data: E) {
        mutableList.add(index, data)
        flow.send(AddModification(version, index, 1))
    }

    suspend fun remove(data: E) {
        val index = mutableList.indexOf(data)
        if (index != -1) flow.send(RemoveModification(version, index, 1))
    }

    suspend fun addAll(data: Collection<E>) {
        if (data.isEmpty()) return
        val index = mutableList.size
        mutableList.addAll(data)
        flow.send(AddModification(version, index, data.size))
    }

    suspend fun addAll(index: Int, data: Collection<E>) {
        mutableList.addAll(index, data)
        flow.send(AddModification(version, index, data.size))
    }

    suspend fun removeAt(index: Int) {
        mutableList.removeAt(index)
        flow.send(RemoveModification(version, index, 1))
    }

    suspend fun removeAll(fromIndex: Int, toIndex: Int) {
        mutableList.subList(fromIndex, toIndex).clear()
        flow.send(RemoveModification(version, fromIndex, toIndex - fromIndex))
    }

    suspend fun set(index: Int, data: E) {
        mutableList[index] = data
        flow.send(UpdateModification(version, index, 1))
    }

    suspend fun update(index: Int) {
        flow.send(UpdateModification(version, index, 1))
    }

    suspend fun clear() {
        val count = mutableList.size
        if (count > 0) {
            mutableList.clear()
            flow.send(RemoveModification(version, 0, count))
        }
    }
}

sealed class SnapshotModification {

    abstract val version: Int

    internal abstract suspend fun dispatchToAdapter(adapter: ISnapshotAdapter)
}

interface ISnapshotAdapter {

    fun onAddModification(index: Int, count: Int)

    fun onUpdateModification(index: Int, count: Int)

    fun onRemoveModification(index: Int, count: Int)

    fun onAllModification(oldCount: Int, newCount: Int)

    fun onReferenceModification()
}

data class AddModification(override val version: Int, val index: Int, val count: Int) :
    SnapshotModification() {

    @SuppressLint("NotifyDataSetChanged")
    override suspend fun dispatchToAdapter(adapter: ISnapshotAdapter) {
        adapter.onAddModification(index, count)
    }

}

data class UpdateModification(override val version: Int, val index: Int, val count: Int) :
    SnapshotModification() {

    override suspend fun dispatchToAdapter(adapter: ISnapshotAdapter) {
        adapter.onUpdateModification(index, count)
    }

}

data class RemoveModification(override val version: Int, val index: Int, val count: Int) :
    SnapshotModification() {

    override suspend fun dispatchToAdapter(adapter: ISnapshotAdapter) {
        adapter.onRemoveModification(index, count)
    }

}

data class AllModification(override val version: Int, val oldCount: Int, val newCount: Int) :
    SnapshotModification() {

    override suspend fun dispatchToAdapter(adapter: ISnapshotAdapter) {
        adapter.onAllModification(oldCount, newCount)
    }

}

object ReferenceModification : SnapshotModification() {

    override val version: Int
        get() = -1

    override suspend fun dispatchToAdapter(adapter: ISnapshotAdapter) {
        adapter.onReferenceModification()
    }

}