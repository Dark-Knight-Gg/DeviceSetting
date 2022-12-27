@file:Suppress("unused")

package vn.com.vti.common.util.extension

inline fun <X, Y> Iterable<X>.toSet(map: (X) -> Y): Set<Y> {
    return HashSet<Y>().also { set ->
        this@toSet.forEach {
            set.add(map(it))
        }
    }
}

inline fun <K, V> MutableMap<K, V>.mergeIfPresent(
    key: K,
    newValue: V?,
    remapping: (oldValue: V) -> V?
) {
    val oldValue = this[key]
    if (oldValue != null) {
        val mergeValue = remapping(oldValue)
        if (mergeValue != null) {
            this[key] = mergeValue
        } else {
            this.remove(key)
        }
    } else if (newValue != null) {
        this[key] = newValue
    }
}


inline fun <T, R> Iterable<T>.foldOn(initial: R, operation: R.(T) -> Unit): R {
    val accumulator = initial
    for (element in this) accumulator.operation(element)
    return accumulator
}

inline fun <T> List<T>.firstWithIndex(predicate: (T) -> Boolean): Pair<Int, T>? {
    for ((index, item) in this.withIndex()) {
        if (predicate(item))
            return index to item
    }
    return null
}