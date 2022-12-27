package vn.com.vti.common.util.rx

import io.reactivex.rxjava3.internal.subscribers.DeferredScalarSubscriber
import org.reactivestreams.Subscriber

abstract class KotlinDeferredScalarSubscriber<T, R>(downstream: Subscriber<in R>) :
    DeferredScalarSubscriber<T, R>(downstream) {

    override fun toByte(): Byte = get().toByte()

    override fun toChar(): Char = get().toChar()

    override fun toShort(): Short = get().toShort()
}