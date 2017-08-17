package com.nfx.android.heartfit.datainterface

import com.nfx.android.heartfit.model.HeartRateData
import io.reactivex.Single
import java.util.*

/**
 * NFX Development
 * Created by nick on 7/27/17.
 */
interface HeartRateDataInterface {
    fun getHeartRateData(day : Calendar, resolution: Int = 24) : Single<List<HeartRateData>>
    fun getAverageHeartRate(day: Calendar) : Single<Float>
    fun getMaxHeartRate(day: Calendar) : Single<HeartRateData>
    fun getMinHeartRate(day: Calendar) : Single<HeartRateData>
}