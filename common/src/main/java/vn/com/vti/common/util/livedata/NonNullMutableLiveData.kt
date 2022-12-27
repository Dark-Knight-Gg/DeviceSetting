package vn.com.vti.common.util.livedata

import androidx.lifecycle.MutableLiveData
import java.util.*

class NonNullMutableLiveData<T>(value: T) : MutableLiveData<T>(value) {
    init {
        Objects.requireNonNull(value)
    }

    override fun postValue(value: T) {
        Objects.requireNonNull(value)
        super.postValue(value)
    }

    override fun setValue(value: T) {
        Objects.requireNonNull(value)
        super.setValue(value)
    }

    override fun getValue(): T {
        return super.getValue()!!
    }
}