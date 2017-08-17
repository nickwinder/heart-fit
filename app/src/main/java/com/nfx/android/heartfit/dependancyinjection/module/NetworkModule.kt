package com.nfx.android.heartfit.dependancyinjection.module

import android.app.Activity
import com.nfx.android.heartfit.datainterface.GoogleFitHeartRateInterface
import com.nfx.android.heartfit.datainterface.HeartRateDataInterface

import dagger.Module
import dagger.Provides

/**
 * NFX Development
 * Created by nick on 7/27/17.
 */

@Module
internal class NetworkModule {
    @Provides
    internal fun providesHeartRateInterface(activity: Activity) : HeartRateDataInterface {
        return GoogleFitHeartRateInterface(activity)
    }
}
