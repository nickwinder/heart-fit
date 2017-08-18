package com.nfx.android.heartfit.dependancyinjection.module

import android.app.Activity
import com.nfx.android.heartfit.datainterface.GoogleFitHeartRateInterface
import com.nfx.android.heartfit.datainterface.HeartRateDataInterface
import com.nfx.android.heartfit.model.HeartRateData
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.whenever
import dagger.Module
import dagger.Provides
import io.reactivex.Single
import io.reactivex.rxkotlin.toSingle
import org.mockito.Mockito

/**
 * NFX Development
 * Created by nick on 8/17/17.
 */
@Module
class TestNetworkModule : NetworkModule() {

    private var googleFitHeartRateInterface: HeartRateDataInterface =
            Mockito.mock(GoogleFitHeartRateInterface::class.java)

    val minValue = 10f
    val maxValue = 100f
    var average = 0f
    val listOfHeartRateData = listOf(HeartRateData(0, minValue), HeartRateData(1, maxValue))

    @Provides
    override fun providesHeartRateInterface(activity: Activity): HeartRateDataInterface {
        setupMockCalls()
        return googleFitHeartRateInterface
    }

    private fun setupMockCalls() {

        average = 0f
        listOfHeartRateData.forEach { average += it.value }
        average /= listOfHeartRateData.size

        whenever(googleFitHeartRateInterface.getAverageHeartRate(any())).thenReturn(Single.just(average))
        whenever(googleFitHeartRateInterface.getMaxHeartRate(any())).thenReturn(
                Single.just(listOfHeartRateData.maxBy { heartRateData -> heartRateData.value }))
        whenever(googleFitHeartRateInterface.getMinHeartRate(any())).thenReturn(
                Single.just(listOfHeartRateData.minBy { heartRateData -> heartRateData.value }))

        whenever(googleFitHeartRateInterface.getHeartRateData(any(), any()))
                .thenReturn(listOfHeartRateData.toSingle())
    }
}
