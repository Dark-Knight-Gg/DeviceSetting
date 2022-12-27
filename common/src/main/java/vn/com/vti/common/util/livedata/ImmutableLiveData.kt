package vn.com.vti.common.util.livedata

import androidx.lifecycle.LiveData
import java.util.*

class ImmutableLiveData<T>(value: T) : LiveData<T>(value) {

    init {
        Objects.requireNonNull(value)
    }

}