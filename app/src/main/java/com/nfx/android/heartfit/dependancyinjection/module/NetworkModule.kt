package com.nfx.android.heartfit.dependancyinjection.module

import android.app.Activity
import android.app.Application
import android.content.Context
import com.nfx.android.heartfit.network.GoogleFitHeartRateInterface
import com.nfx.android.heartfit.network.HeartRateDataInterface
import com.nfx.android.heartfit.network.TestHeartRateDataInterface

import dagger.Module
import dagger.Provides
import java.util.*

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
