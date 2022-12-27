package vn.com.vti.common.util.rx

import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.plugins.RxJavaPlugins
import org.reactivestreams.Publisher
import vn.com.vti.common.util.rx.math.FlowableAverageFloat

fun Publisher<out Number>.averageFloat(): Flowable<Float> {
    return RxJavaPlugins.onAssembly(FlowableAverageFloat(this))
}