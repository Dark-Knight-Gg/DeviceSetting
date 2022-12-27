package vn.com.vti.common.util.livedata

import androidx.lifecycle.MutableLiveData

class PredicateMutableLiveData<T>(private val condition: (old: T?, update: T?) -> T?) :
    MutableLiveData<T>() {

    override fun postValue(value: T) {
        super.postValue(condition(this.value, value))
    }

    override fun setValue(value: T) {
        super.setValue(condition(this.value, value))
    }

}

class NonNullPredicateMutableLiveData<T>(initialValue: T, private val condition: (T) -> T) :
    MutableLiveData<T>(condition(initialValue)) {

    override fun postValue(value: T) {
        super.postValue(condition(value))
    }

    override fun setValue(value: T) {
        super.setValue(condition(value))
    }

}