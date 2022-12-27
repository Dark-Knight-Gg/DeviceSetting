package vn.com.vti.common.util.rx

import io.reactivex.rxjava3.core.Flowable
import org.reactivestreams.Publisher

abstract class FlowableSource<T, R>(protected val source: Publisher<out T>) : Flowable<R>()