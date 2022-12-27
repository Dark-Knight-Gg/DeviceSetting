package vn.com.vti.common.util.rx.math

import org.reactivestreams.Publisher
import org.reactivestreams.Subscriber
import vn.com.vti.common.util.rx.FlowableSource
import vn.com.vti.common.util.rx.KotlinDeferredScalarSubscriber

class FlowableAverageFloat(source: Publisher<out Number>) : FlowableSource<Number, Float>(source) {

    override fun subscribeActual(subscriber: Subscriber<in Float>) {
        source.subscribe(AverageFloatSubscriber(subscriber))
    }

    internal class AverageFloatSubscriber(downstream: Subscriber<in Float>) :
        KotlinDeferredScalarSubscriber<Number, Float>(downstream) {

        var accumulator = 0f
        var count = 0

        override fun onNext(value: Number) {
            accumulator += value.toFloat()
            count++
        }

        override fun onComplete() {
            val c = count
            if (c != 0) {
                complete(accumulator / c)
            } else {
                downstream.onComplete()
            }
        }

        companion object {
            private const val serialVersionUID = 600979972678601618L
        }
    }
}