package com.nfx.android.heartfit.dependancyinjection.components

import com.nfx.android.heartfit.dependancyinjection.BaseApplication
import com.nfx.android.heartfit.dependancyinjection.module.ApplicationModule
import com.nfx.android.heartfit.dependancyinjection.scopes.PerApplication
import dagger.Component

/**
 * NFX Development
 * Created by nick on 7/27/17.
 */
@PerApplication
@Component(modules = arrayOf(ApplicationModule::class))
interface ApplicationComponent {

    fun inject(baseApplication: BaseApplication)
}