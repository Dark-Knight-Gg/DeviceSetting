package vn.com.vti.common.util.livedata

import androidx.annotation.IntRange
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.common.base.Optional
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.BackpressureStrategy
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.schedulers.Schedulers
import io.reactivex.rxjava3.subjects.PublishSubject
import io.reactivex.rxjava3.subjects.Subject
import timber.log.Timber
import java.util.concurrent.TimeUnit

class ReactiveXLiveData<T : Any>(
    rxFunction: (Flowable<Optional<T>>) -> Flowable<Optional<T>>,
    strategy: BackpressureStrategy = BackpressureStrategy.LATEST
) : MutableLiveData<T>() {

    private val beater: Subject<Optional<T>> =
        PublishSubject.create<Optional<T>>().also { subject ->
            val flowable = subject.toFlowable(strategy)
            rxFunction(flowable)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    super.setValue(it.orNull())
                }, Timber::e)
        }

    constructor(
        rxFunction: (Flowable<Optional<T>>) -> Flowable<Optional<T>>,
        strategy: BackpressureStrategy = BackpressureStrategy.LATEST,
        t: T
    ) : this(rxFunction, strategy) {
        beater.onNext(Optional.of(t))
        super.setValue(t)
    }

    override fun setValue(value: T) {
        beater.onNext(Optional.of(value))
    }

    override fun postValue(value: T) {
        beater.onNext(Optional.of(value))
    }
}

class SeparatedRxLiveData<S, D> : MutableLiveData<S> {

    constructor(
        rxFunction: (Flowable<Optional<S>>) -> Flowable<Optional<D>>,
        strategy: BackpressureStrategy = BackpressureStrategy.LATEST,
    ) : super() {
        beater = PublishSubject.create()
        initialize(rxFunction, strategy)
    }

    @Suppress("unused")
    constructor(
        rxFunction: (Flowable<Optional<S>>) -> Flowable<Optional<D>>,
        strategy: BackpressureStrategy = BackpressureStrategy.LATEST,
        initValue: S
    ) : super(initValue) {
        beater = PublishSubject.create()
        initialize(rxFunction, strategy)
        beater.onNext(Optional.fromNullable(initValue))
    }

    private fun initialize(
        rxFunction: (Flowable<Optional<S>>) -> Flowable<Optional<D>>,
        strategy: BackpressureStrategy = BackpressureStrategy.LATEST
    ) {
        val flowable = beater.toFlowable(strategy)
        rxFunction(flowable)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                processedLiveData.value = if (it.isPresent) it.get() else null
            }, {
                Timber.e(it)
            })
    }

    private val processedLiveData: MutableLiveData<D> = MutableLiveData()

    private val beater: Subject<Optional<S>>

    fun getRxLiveData(): LiveData<D> = processedLiveData

    val rxData: LiveData<D> get() = processedLiveData

    override fun setValue(value: S?) {
        super.setValue(value)
        beater.onNext(Optional.fromNullable(value))
    }

    override fun postValue(value: S?) {
        super.postValue(value)
        beater.onNext(Optional.fromNullable(value))
    }
}

fun <T> debounceLiveData(
    @IntRange(from = 0) debounce: Long = 500,
    timeUnit: TimeUnit = TimeUnit.MILLISECONDS,
): SeparatedRxLiveData<T, T> =
    SeparatedRxLiveData(
        rxFunction = {
            return@SeparatedRxLiveData it.debounce(debounce, timeUnit)
        },
        strategy = BackpressureStrategy.LATEST
    )