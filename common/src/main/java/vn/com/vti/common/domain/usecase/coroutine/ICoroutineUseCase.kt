@file:Suppress("unused")

package vn.com.vti.common.domain.usecase.coroutine

import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

fun interface ICoroutineUseCase<out RESULT, in PARAMS> {

    suspend fun fetch(
        context: CoroutineContext,
        params: PARAMS,
    ): RESULT
}

suspend fun <RESULT> ICoroutineUseCase<RESULT, Unit>.fetch(context: CoroutineContext): RESULT =
    fetch(context, Unit)

fun interface IFlowUseCase<out RESULT, in PARAMS> {

    suspend fun fetch(
        context: CoroutineContext,
        params: PARAMS,
    ): Flow<RESULT>
}

suspend fun <RESULT> IFlowUseCase<RESULT, Unit>.fetch(context: CoroutineContext): Flow<RESULT> =
    fetch(context, Unit)

abstract class CoroutineUseCase<out RESULT, in PARAMS> : ICoroutineUseCase<RESULT, PARAMS> {

    override suspend fun fetch(
        context: CoroutineContext,
        params: PARAMS,
    ): RESULT = withContext(context) {
        run(params = params)
    }

    abstract suspend fun run(params: PARAMS): RESULT
}

abstract class DeferedCoroutineUseCase<out RESULT, in PARAMS> :
    ICoroutineUseCase<Deferred<RESULT>, PARAMS> {

    @Suppress("DeferredIsResult")
    override suspend fun fetch(context: CoroutineContext, params: PARAMS): Deferred<RESULT> =
        withContext(context) {
            async { run(params = params) }
        }

    abstract suspend fun run(params: PARAMS): RESULT
}

abstract class FlowUseCase<out RESULT, in PARAMS> : IFlowUseCase<RESULT, PARAMS> {

    override suspend fun fetch(context: CoroutineContext, params: PARAMS): Flow<RESULT> =
        withContext(context) {
            run(params = params)
        }

    abstract suspend fun run(params: PARAMS): Flow<RESULT>
}