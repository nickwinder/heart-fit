package com.nfx.android.heartfit.dependancyinjection.module

import android.content.Context
import com.nfx.android.heartfit.dependancyinjection.BaseApplication
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

/**
 * NFX Development
 * Created by nick on 7/27/17.
 */
@Module
class ApplicationModule(private val baseApplication: BaseApplication) {
    @Provides
    @Singleton
    fun provideContext(): Context = baseApplication.applicationContext
}