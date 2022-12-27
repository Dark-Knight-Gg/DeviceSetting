@file:Suppress("unused")

package vn.com.vti.common.util.extension

import android.view.View
import androidx.arch.core.util.Function
import androidx.databinding.BindingAdapter
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import kotlinx.coroutines.flow.MutableStateFlow
import vn.com.vti.common.util.setOnSafeClickListener

object BindingLiveDataMethods {

    @JvmStatic
    @BindingAdapter("onClickToggleBoolean")
    fun View.onClickToggleBoolean(liveData: MutableLiveData<Boolean>?) {
        if (liveData == null) setOnClickListener(null)
        else setOnSafeClickListener {
            liveData.toggle()
        }
    }

    @JvmStatic
    @BindingAdapter("onClickToggleBoolean")
    fun View.onClickToggleBoolean(stateFlow: MutableStateFlow<Boolean>?) {
        if (stateFlow == null) setOnClickListener(null)
        else setOnSafeClickListener {
            stateFlow.value = !stateFlow.value
        }
    }

    @JvmStatic
    fun MutableLiveData<Boolean>.toggle() {
        this.value = this.value?.let { !it } ?: true
    }

    @JvmStatic
    fun MutableLiveData<Int>.add(amount: Int) {
        this.value = this.value?.let { it + amount } ?: amount
    }

    @JvmStatic
    fun MutableLiveData<Float>.add(amount: Float) {
        this.value = this.value?.let { it + amount } ?: amount
    }
}

fun <T> MutableLiveData<T>.distinctSetValue(newValue: T?): Boolean {
    return if (newValue != value) {
        value = newValue
        true
    } else false
}

fun <X, Y> distinctMap(
    source: LiveData<X>,
    mapFunction: Function<X, Y>
): LiveData<Y> {
    val result = MediatorLiveData<Y>()
    result.addSource(source) {
        result.distinctSetValue(mapFunction.apply(it))
    }
    return result
}

fun LiveData<Boolean>.unbox(): Boolean = this.value ?: false

fun LiveData<Long>.unbox(): Long = this.value ?: 0L

fun LiveData<Int>.unbox(): Int = this.value ?: 0

fun LiveData<Float>.unbox(): Float = this.value ?: 0.0f

fun LiveData<Double>.unbox(): Double = this.value ?: 0.0

fun LiveData<Short>.unbox(): Short = this.value ?: 0

fun MutableLiveData<Boolean>.toggle() {
    this.value = this.value?.let { !it } ?: true
}

operator fun LiveData<Long>.plus(arg: Long): Long = this.value?.let { it + arg } ?: arg

operator fun MutableLiveData<Long>.plusAssign(arg: Long) {
    this.value = this + arg
}

operator fun LiveData<Long>.minus(arg: Long): Long = this.value?.let { it - arg } ?: -arg

operator fun MutableLiveData<Long>.minusAssign(arg: Long) {
    this.value = this - arg
}

operator fun LiveData<Long>.times(arg: Long): Long = this.value?.let { it * arg } ?: 0L

operator fun MutableLiveData<Long>.timesAssign(arg: Long) {
    this.value = this * arg
}

operator fun LiveData<Long>.div(arg: Long): Long = this.value?.let { it / arg } ?: 0L

operator fun MutableLiveData<Long>.divAssign(arg: Long) {
    this.value = this / arg
}

operator fun LiveData<Long>.rem(arg: Long): Long = this.value?.let { it % arg } ?: 0L

operator fun LiveData<Int>.plus(arg: Int): Int = this.value?.let { it + arg } ?: arg

operator fun MutableLiveData<Int>.plusAssign(arg: Int) {
    this.value = this + arg
}

operator fun LiveData<Int>.minus(arg: Int): Int = this.value?.let { it - arg } ?: -arg

operator fun MutableLiveData<Int>.minusAssign(arg: Int) {
    this.value = this - arg
}

operator fun LiveData<Int>.times(arg: Int): Int = this.value?.let { it * arg } ?: 0

operator fun MutableLiveData<Int>.timesAssign(arg: Int) {
    this.value = this * arg
}

operator fun LiveData<Int>.div(arg: Int): Int = this.value?.let { it / arg } ?: 0

operator fun MutableLiveData<Int>.divAssign(arg: Int) {
    this.value = this / arg
}

operator fun LiveData<Int>.rem(arg: Int): Int = this.value?.let { it % arg } ?: 0

operator fun LiveData<Double>.plus(arg: Double): Double = this.value?.let { it + arg } ?: arg

operator fun MutableLiveData<Double>.plusAssign(arg: Double) {
    this.value = this + arg
}

operator fun LiveData<Double>.minus(arg: Double): Double =
    this.value?.let { it - arg } ?: -arg

operator fun MutableLiveData<Double>.minusAssign(arg: Double) {
    this.value = this - arg
}

operator fun LiveData<Double>.times(arg: Double): Double =
    this.value?.let { it * arg } ?: 0.0

operator fun MutableLiveData<Double>.timesAssign(arg: Double) {
    this.value = this * arg
}

operator fun LiveData<Double>.div(arg: Double): Double = this.value?.let { it / arg } ?: 0.0

operator fun MutableLiveData<Double>.divAssign(arg: Double) {
    this.value = this / arg
}

operator fun LiveData<Double>.rem(arg: Double): Double = this.value?.let { it % arg } ?: 0.0

operator fun LiveData<Float>.plus(arg: Float): Float = this.value?.let { it + arg } ?: arg

operator fun MutableLiveData<Float>.plusAssign(arg: Float) {
    this.value = this + arg
}

operator fun LiveData<Float>.minus(arg: Float): Float = this.value?.let { it - arg } ?: -arg

operator fun MutableLiveData<Float>.minusAssign(arg: Float) {
    this.value = this - arg
}

operator fun LiveData<Float>.times(arg: Float): Float = this.value?.let { it * arg } ?: 0f

operator fun MutableLiveData<Float>.timesAssign(arg: Float) {
    this.value = this * arg
}

operator fun LiveData<Float>.div(arg: Float): Float = this.value?.let { it / arg } ?: 0f

operator fun MutableLiveData<Float>.divAssign(arg: Float) {
    this.value = this / arg
}

operator fun LiveData<Float>.rem(arg: Float): Float = this.value?.let { it % arg } ?: 0f

operator fun <T : Comparable<T>> LiveData<T>.compareTo(arg: T?): Int {
    return value?.let {
        if (arg == null) 1 else it.compareTo(arg)
    } ?: if (arg == null) 0 else -1
}

operator fun <T : Comparable<T>> LiveData<T>.compareTo(arg: LiveData<out T>): Int {
    return this.compareTo(arg.value)
}

operator fun <T> LiveData<out List<T>>.contains(arg: T) = value?.let { arg in it } ?: false

operator fun <T> LiveData<List<T>>.get(arg: Int) = value?.get(arg)

operator fun <T> LiveData<out MutableList<in T>>.set(index: Int, arg: T) = value?.set(index, arg)

fun <T> mediator(combine: () -> T, vararg sources: LiveData<*>): MediatorLiveData<T> {
    return MediatorLiveData<T>().apply {
        Observer<Any> { this.setValue(combine.invoke()) }.also {
            sources.forEach { source ->
                this.addSource(source, it)
            }
        }
    }
}