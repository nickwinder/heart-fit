package com.nfx.android.heartfit.dependancyinjection

import android.app.Application

import com.nfx.android.heartfit.dependancyinjection.components.ApplicationComponent
import com.nfx.android.heartfit.dependancyinjection.components.DaggerApplicationComponent

/**
 * NFX Development
 * Created by nick on 7/27/17.
 */
class BaseApplication : Application() {
    val component: ApplicationComponent by lazy {
         DaggerApplicationComponent
                .builder()
                .application(this)
                .build()
    }

    override fun onCreate() {
        super.onCreate()
        component.inject(this)
    }
}