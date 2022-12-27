package vn.com.vti.devicesetting.module.restful

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import vn.com.vti.common.network.Retrofits
import vn.com.vti.common.network.interceptor.ConnectivityMonitor
import vn.com.vti.devicesetting.module.restful.service.PrimaryApiService
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RestfulApiModule {

    @Singleton
    @Provides
    fun provideClientService(
        @ApplicationContext
        context: Context,
    ): PrimaryApiService =
        Retrofits.newClient(
            domain = "http://10.1.52.121:8000/api/",
            preRequestInterceptor = listOf(
                ConnectivityMonitor(context),
                AuthInterceptor()
            )
        ).create(PrimaryApiService::class.java)
}
