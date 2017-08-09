package com.nfx.android.heartfit.dependancyinjection.module

import android.app.Activity
import android.content.Context

import com.nfx.android.heartfit.dependancyinjection.BaseActivity

import dagger.Module
import dagger.Provides

/**
 * NFX Development
 * Created by nick on 7/27/17.
 */
@Module
class ActivityModule {
    @Provides
    internal fun provideActivity(activity: BaseActivity): Activity {
        return activity
    }
    @Provides
    internal fun provideContext(activity: BaseActivity): Context {
        return activity
    }
}
