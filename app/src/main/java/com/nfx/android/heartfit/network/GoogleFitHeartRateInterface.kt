package com.nfx.android.heartfit.network

import android.app.Activity
import com.google.android.gms.fitness.data.DataType
import com.google.android.gms.fitness.data.Field
import com.google.android.gms.fitness.result.DataReadResult
import com.nfx.android.heartfit.GoogleFitManager.Connection
import com.nfx.android.heartfit.GoogleFitManager.GoogleFitManager
import com.nfx.android.heartfit.model.HeartRateData
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import java.util.*
import java.util.concurrent.TimeUnit


/**
 * NFX Development
 * Created by nick on 7/31/17.
 */
class GoogleFitHeartRateInterface(activity: Activity) : HeartRateDataInterface {
    private val googleFitManager: GoogleFitManager = GoogleFitManager(activity)

    fun getConnectionListener(): PublishSubject<Connection> {
        return googleFitManager.connectionResultPublishSubject
    }

    fun connectToManager() {
        googleFitManager.connect()
    }

    override fun getAverageHeartRate(day: Calendar): Single<Float> {
        return Single.create<Float> {
            googleFitManager.getSummaryHeartRate(day)
                    ?.subscribeOn(Schedulers.io())
                    ?.subscribe({ dataReadResult ->
                        val heartRateData = getFieldFromDataSet(dataReadResult, Field.FIELD_AVERAGE)
                        it.onSuccess(heartRateData.value)}, {throwable -> it.onError(throwable)})
        }
    }

    override fun getHeartRateData(day: Calendar, resolution: Int): Single<List<HeartRateData>> {
        return Single.create<List<HeartRateData>> {
            googleFitManager.getHeartRate(day)
                    ?.subscribeOn(Schedulers.io())
                    ?.subscribe({
                    dataReadResult ->
                var heartRateValues = mutableListOf<HeartRateData>()
                for (dataSet in dataReadResult.dataSets) {
                    for (dataPoint in dataSet.dataPoints) {
                        for (field in dataPoint.dataType.fields) {
                            val heartRateData = dataPoint.getValue(field).asFloat()
                            val timestamp = dataPoint.getStartTime(TimeUnit.MILLISECONDS)
                            heartRateValues.add(HeartRateData(timestamp, heartRateData))
                        }
                    }
                }
                heartRateValues = removeDuplicateStartValues(heartRateValues) as MutableList<HeartRateData>
                heartRateValues = removeDuplicateEndValues(heartRateValues) as MutableList<HeartRateData>

                it.onSuccess(heartRateValues)}, {throwable -> it.onError(throwable)})
        }
    }

    override fun getMaxHeartRate(day: Calendar): Single<HeartRateData> {

        return Single.create<HeartRateData> {
            googleFitManager.getSummaryHeartRate(day)
                    ?.subscribeOn(Schedulers.io())
                    ?.subscribe({ dataReadResult ->
                        val heartRateData = getFieldFromDataSet(dataReadResult, Field.FIELD_MAX)
                        it.onSuccess(heartRateData)}, {throwable -> it.onError(throwable)})
        }
    }

    private fun getFieldFromDataSet(dataReadResult: DataReadResult, fieldType: Field): HeartRateData {
        val heartRateData = HeartRateData(0 , 0f)
        for (buckets in dataReadResult.buckets) {
            for (dataSet in buckets.dataSets) {
                for (dataPoint in dataSet.dataPoints) {
                    if (dataPoint.dataType.name == DataType.AGGREGATE_HEART_RATE_SUMMARY.name) {
                        for (field in dataPoint.dataType.fields) {
                            if (field.name == fieldType.name) {
                                heartRateData.timestamp = dataPoint.getStartTime(TimeUnit.MILLISECONDS)
                                heartRateData.value = dataPoint.getValue(field).asFloat()
                            }
                        }
                    }
                }
            }
        }
        return heartRateData
    }

    override fun getMinHeartRate(day: Calendar): Single<HeartRateData> {
        return Single.create<HeartRateData> {
            googleFitManager.getSummaryHeartRate(day)
                    ?.subscribeOn(Schedulers.io())
                    ?.subscribe({ dataReadResult ->
                        val heartRateData = getFieldFromDataSet(dataReadResult, Field.FIELD_MIN)
                        it.onSuccess(heartRateData)}, {throwable -> it.onError(throwable)})
        }
    }

    private fun removeDuplicateStartValues(heartRateValues : List<HeartRateData>): List<HeartRateData> {
        if(!heartRateValues.isEmpty()) {
            val firstValues = heartRateValues[0].value

            return heartRateValues.dropWhile { heartRateData -> heartRateData.value == firstValues }
        } else {
            return heartRateValues
        }
    }

    private fun removeDuplicateEndValues(heartRateValues : List<HeartRateData>): List<HeartRateData> {
        if(!heartRateValues.isEmpty()) {
            val lastValue = heartRateValues[heartRateValues.size - 1].value

            return heartRateValues.dropLastWhile { heartRateData -> heartRateData.value == lastValue }
        } else {
            return heartRateValues
        }
    }
}