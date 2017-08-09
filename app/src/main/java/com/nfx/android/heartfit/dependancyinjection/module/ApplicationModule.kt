package com.nfx.android.heartfit.dependancyinjection.module

import android.app.Application
import android.content.Context

import com.nfx.android.heartfit.dependancyinjection.BaseApplication

import dagger.Module
import dagger.Provides

/**
 * NFX Development
 * Created by nick on 7/27/17.
 */
@Module
class ApplicationModule {
    @Provides
    internal fun provideContext(application: BaseApplication): Context {
        return application.applicationContext
    }
}