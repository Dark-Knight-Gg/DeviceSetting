package vn.com.vti.common.util

import java.lang.ref.WeakReference
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class LazilyWeakReferenceDelegate<T>(private val initializer: () -> T) :
    ReadWriteProperty<Any?, T> {

    private var weakRef: WeakReference<T> = WeakReference(null)

    override fun getValue(thisRef: Any?, property: KProperty<*>): T {
        return weakRef.get() ?: initializer().also {
            weakRef = WeakReference(it)
        }
    }

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        if (value == null) weakRef.clear()
        else weakRef = WeakReference(value)
    }
}

class WeakReferenceDelegate<T>(weakReference: WeakReference<T>) : ReadWriteProperty<Any?, T?> {

    private var weakRef: WeakReference<T> = weakReference

    constructor(initializer: () -> T) : this(WeakReference(initializer()))

    override fun getValue(thisRef: Any?, property: KProperty<*>): T? = weakRef.get()

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: T?) {
        if (value == null) weakRef.clear()
        else weakRef = WeakReference(value)
    }

}

fun <T> weakLazily(initializer: () -> T) = LazilyWeakReferenceDelegate(initializer)

fun <T> weak(initializer: () -> T) = WeakReferenceDelegate(initializer)

fun <T> weak(reference: WeakReference<T>) = WeakReferenceDelegate(reference)

fun <T> weak(value: T) = WeakReferenceDelegate(WeakReference(value))

fun <T> weak() = WeakReferenceDelegate<T?>(WeakReference(null))