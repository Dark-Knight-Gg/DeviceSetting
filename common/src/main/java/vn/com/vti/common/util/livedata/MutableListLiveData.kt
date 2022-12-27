package vn.com.vti.common.util.livedata

import androidx.lifecycle.MutableLiveData
import java.util.*

class MutableListLiveData<E>(value: MutableList<E> = mutableListOf()) :
    MutableLiveData<MutableList<E>>(value) {
    init {
        Objects.requireNonNull(value)
    }

    override fun setValue(value: MutableList<E>?) {
        super.setValue(value ?: ArrayList())
    }

    override fun postValue(value: MutableList<E>?) {
        super.postValue(value ?: ArrayList())
    }

    inline fun commitChange(transaction: MutableList<E>.() -> Unit) {
        (value ?: ArrayList()).let {
            it.transaction()
            setValue(it)
        }
    }

    inline fun postChange(transaction: MutableList<E>.() -> Unit) {
        (value ?: ArrayList()).let {
            it.transaction()
            postValue(it)
        }
    }
}