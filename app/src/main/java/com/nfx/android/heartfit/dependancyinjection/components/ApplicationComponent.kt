package com.nfx.android.heartfit.dependancyinjection.components

import android.app.Application
import com.nfx.android.heartfit.dependancyinjection.BaseApplication
import com.nfx.android.heartfit.dependancyinjection.module.ApplicationModule
import com.nfx.android.heartfit.dependancyinjection.scopes.PerApplication
import dagger.BindsInstance
import dagger.Component

/**
 * NFX Development
 * Created by nick on 7/27/17.
 */
@PerApplication
@Component(modules = arrayOf(ApplicationModule::class))
interface ApplicationComponent {
    @Component.Builder
    interface Builder {
        @BindsInstance
        fun application(application: BaseApplication): Builder

        fun build(): ApplicationComponent
    }

    fun inject(baseApplication: BaseApplication)
}