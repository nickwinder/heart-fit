package com.nfx.android.heartfit

import com.nfx.android.heartfit.model.HeartRateData

/**
 * NFX Development
 * Created by nick on 8/8/17.
 */
interface HeartRateView {
    fun updateMinimumHeartRate(heartRate: Int)

    fun updateMaximumHeartRate(heartRate: Int)

    fun updateAverageHeartRate(heartRate: Int)

    fun updateGraphData(heartRateData: List<HeartRateData>)
}