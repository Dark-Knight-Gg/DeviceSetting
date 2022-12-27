@file:Suppress("unused")

package vn.com.vti.devicesetting.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.reactivex.rxjava3.disposables.Disposable
import vn.com.vti.common.domain.fetcher.UseCaseFetcher
import vn.com.vti.common.domain.fetcher.impl.RxUseCaseFetcher
import vn.com.vti.devicesetting.module.restful.RestfulApiModule


@Module(
    includes = [
        BindsDependencies::class,
        RestfulApiModule::class,
    ]
)
@InstallIn(SingletonComponent::class)
interface AppModules

@Module
@InstallIn(SingletonComponent::class)
interface BindsDependencies {
    @Binds
    fun bindDisposablesUseCaseFetcher(fetcher: RxUseCaseFetcher): UseCaseFetcher<Disposable>
}