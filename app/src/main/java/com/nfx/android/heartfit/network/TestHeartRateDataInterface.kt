package com.nfx.android.heartfit.network

import com.nfx.android.heartfit.model.HeartRateData
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.SingleOnSubscribe
import java.util.*

/**
 * NFX Development
 * Created by nick on 7/27/17.
 */
class TestHeartRateDataInterface : HeartRateDataInterface {
    val maxHeartRate = 180
    val minHeartRate = 50

    override fun getAverageHeartRate(day: Calendar): Single<Float> {
        return getHeartRateData(day)
                .flatMap{t -> Single.create(SingleOnSubscribe<Float> { e ->
                    var average = 0f
                    t.forEach { average += it.value }
                    average /= t.size
                    e.onSuccess(average)
                })}
    }

    override fun getMaxHeartRate(day: Calendar): Single<HeartRateData> {
        return getHeartRateData(day)
                .flatMapObservable { t -> Observable.fromIterable(t)}
                .reduce({ t1 , t2 -> if(t1.value > t2.value) t1 else t2 })
                .toSingle()
    }

    override fun getMinHeartRate(day: Calendar): Single<HeartRateData> {
        return getHeartRateData(day)
                .flatMapObservable { t -> Observable.fromIterable(t)}
                .reduce({ t1 , t2 -> if(t1.value < t2.value) t1 else t2 })
                .toSingle()
    }

    override fun getHeartRateData(day: Calendar, resolution: Int) : Single<List<HeartRateData>> {
        return Single.just(generateTestData(day, resolution))
    }

    fun generateTestData(day: Calendar, resolution: Int): List<HeartRateData> {
        val secondsInADay = 86400
        val samplingInterval = secondsInADay / resolution
        val listOfHeartRateData: MutableList<HeartRateData> = mutableListOf()

        repeat(resolution) { i ->
            day.add(Calendar.SECOND, samplingInterval*i)
            val heartRateData = HeartRateData(timestamp = day.timeInMillis,
                    value = ((Math.random() * (maxHeartRate - minHeartRate)) + minHeartRate).toFloat())
            listOfHeartRateData.add(heartRateData)
        }

        return listOfHeartRateData
    }

}