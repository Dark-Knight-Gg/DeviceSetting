package vn.com.vti.common.util.livedata

import androidx.lifecycle.LiveData

class TransformLiveData<S, T> @JvmOverloads constructor(
    private val transform: (S?) -> T?,
    value: T? = null,
) : LiveData<T>(value) {

    public override fun setValue(value: T?) {
        super.setValue(value)
    }

    fun setSourceValue(newValue: S?) {
        value = transform(newValue)
    }

    fun postSourceValue(newValue: S?) {
        postValue(transform(newValue))
    }

    companion object {
        fun <S, T> create(converter: (S?) -> T?): TransformLiveData<S, T> =
            TransformLiveData(converter)
    }
}