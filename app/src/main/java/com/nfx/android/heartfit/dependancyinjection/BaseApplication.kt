package com.nfx.android.heartfit.dependancyinjection

import android.app.Application

import com.nfx.android.heartfit.dependancyinjection.components.ApplicationComponent
import com.nfx.android.heartfit.dependancyinjection.components.DaggerApplicationComponent
import com.nfx.android.heartfit.dependancyinjection.module.ApplicationModule

/**
 * NFX Development
 * Created by nick on 7/27/17.
 */
class BaseApplication : Application() {
    @Suppress("DEPRECATION") // Currently has the deprecated tag as it is unused as of now.
    private val component: ApplicationComponent by lazy {
         DaggerApplicationComponent
                .builder()
                 .applicationModule(ApplicationModule(this))
                .build()
    }

    override fun onCreate() {
        super.onCreate()
        component.inject(this)
    }
}