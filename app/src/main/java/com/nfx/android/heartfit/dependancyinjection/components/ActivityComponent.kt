package com.nfx.android.heartfit.dependancyinjection.components

import com.nfx.android.heartfit.HeartRateGraph
import com.nfx.android.heartfit.LoginScreen
import com.nfx.android.heartfit.dependancyinjection.BaseActivity
import com.nfx.android.heartfit.dependancyinjection.module.ActivityModule
import com.nfx.android.heartfit.dependancyinjection.module.NetworkModule
import com.nfx.android.heartfit.dependancyinjection.scopes.PerActivity
import dagger.BindsInstance
import dagger.Component

/**
 * NFX Development
 * Created by nick on 7/27/17.
 */
@PerActivity
@Component(modules = arrayOf(ActivityModule::class, NetworkModule::class))
interface ActivityComponent {
    @Component.Builder
    interface Builder {
        @BindsInstance
        fun activity(activity: BaseActivity): Builder

        fun build(): ActivityComponent
    }

    fun inject(healthRateGraph: HeartRateGraph)
    fun inject(healthRateGraph: LoginScreen)
}