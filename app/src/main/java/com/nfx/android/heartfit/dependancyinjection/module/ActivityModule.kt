package com.nfx.android.heartfit.dependancyinjection.module

import android.app.Activity
import android.content.Context
import com.nfx.android.heartfit.dependancyinjection.BaseActivity
import com.nfx.android.heartfit.dependancyinjection.scopes.PerActivity
import dagger.Module
import dagger.Provides

/**
 * NFX Development
 * Created by nick on 7/27/17.
 */
@Module
class ActivityModule(val activity: BaseActivity) {
    @Provides
    @PerActivity
    fun provideContext(): Context = activity

    @Provides
    @PerActivity
    fun provideActivity(): Activity = activity
}