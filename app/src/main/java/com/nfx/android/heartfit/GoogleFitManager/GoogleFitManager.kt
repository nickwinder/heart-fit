package com.nfx.android.heartfit.GoogleFitManager

import android.app.Activity
import android.os.Bundle
import com.google.android.gms.common.Scopes
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.api.Scope
import com.google.android.gms.fitness.Fitness
import com.google.android.gms.fitness.data.DataType
import com.google.android.gms.fitness.request.DataReadRequest
import com.google.android.gms.fitness.result.DataReadResult
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import java.util.*
import java.util.concurrent.TimeUnit


/**
 * NFX Development
 * Created by nick on 7/31/17.
 */
class GoogleFitManager(activity: Activity) {

    private lateinit var fitApiClient: GoogleApiClient
    val connectionResultPublishSubject: PublishSubject<Connection> = PublishSubject.create()

    private fun buildClient(activity: Activity) {
        fitApiClient = GoogleApiClient.Builder(activity)
                .addApi(Fitness.HISTORY_API)
                .addScope(Scope(Scopes.FITNESS_BODY_READ))
                .useDefaultAccount()
                .addConnectionCallbacks(object : GoogleApiClient.ConnectionCallbacks {
                    override fun onConnected(initialBundle: Bundle?) {
                        connectionResultPublishSubject.onNext(ConnectionUpdate.onConnected())
                    }

                    override fun onConnectionSuspended(cause: Int) {
                        connectionResultPublishSubject.onNext(ConnectionUpdate.onSuspended(cause))
                    }
                })
                .addOnConnectionFailedListener { result ->
                    connectionResultPublishSubject.onNext(ConnectionUpdate.onFailed(result)) }
                .build()
        fitApiClient.connect()
    }

    fun getHeartRate(day: Calendar): Observable<DataReadResult>? {
        val startTime = getStartOfDay(day).timeInMillis
        val endTime = getEndOfDay(day).timeInMillis

        return Observable.create({
            val readRequest = DataReadRequest.Builder()
                    .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                    .read(DataType.TYPE_HEART_RATE_BPM)
                    .enableServerQueries()
                    .build()

            val result = Fitness.HistoryApi.readData(fitApiClient, readRequest)

            result.setResultCallback({
                resultData ->
                it.onNext(resultData)
                it.onComplete()
            }, 30, TimeUnit.SECONDS)
        })
    }

    fun getSummaryHeartRate(day: Calendar): Observable<DataReadResult>? {
        val startTime = getStartOfDay(day).timeInMillis
        val endTime = getEndOfDay(day).timeInMillis

        return Observable.create({
            val readRequest = DataReadRequest.Builder()
                    .aggregate(DataType.TYPE_HEART_RATE_BPM, DataType.AGGREGATE_HEART_RATE_SUMMARY)
                    .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                    .bucketByTime(1, TimeUnit.DAYS)
                    .enableServerQueries()
                    .build()


            val result = Fitness.HistoryApi.readData(fitApiClient, readRequest)

            result.setResultCallback({
                resultData ->
                it.onNext(resultData)
                it.onComplete()
            }, 30, TimeUnit.SECONDS)
        })
    }

    private fun getStartOfDay(day: Calendar): Calendar {
        day.set(Calendar.HOUR_OF_DAY, 0)
        day.set(Calendar.MINUTE, 0)
        day.set(Calendar.SECOND, 0)
        day.set(Calendar.MILLISECOND, 0)
        return day
    }

    private fun getEndOfDay(day: Calendar): Calendar {
        day.set(Calendar.HOUR_OF_DAY, 23)
        day.set(Calendar.MINUTE, 59)
        day.set(Calendar.SECOND, 59)
        day.set(Calendar.MILLISECOND, 999)
        return day
    }

    init {
        buildClient(activity)
    }

    fun connect() {
        if( !fitApiClient.isConnecting && !fitApiClient.isConnected) {
            fitApiClient.connect()
        }
    }

    fun disconnect() {
        if (fitApiClient.isConnected) {
            fitApiClient.disconnect()
        }
    }
}