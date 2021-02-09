package com.mikore.ppqr.module

import android.app.Application
import android.content.Context
import com.mikore.ppqr.AppApplication
import com.mikore.ppqr.database.AppRepo
import dagger.*
import dagger.android.AndroidInjectionModule

@Component(modules = [AndroidInjectionModule::class, AppModule::class, ActivityModule::class])
interface AppComponent {
    @Component.Builder
    interface Builder {
        @BindsInstance
        fun application(app: Application): Builder
        fun build(): AppComponent
    }

    fun inject(app: AppApplication)
}

@Module
class AppModule {
    @Provides
    @Reusable
    fun provideAppRepo(app: Application): AppRepo = AppRepo(app)
}