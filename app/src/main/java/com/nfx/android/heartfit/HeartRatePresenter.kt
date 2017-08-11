package com.nfx.android.heartfit

import android.util.Log
import com.nfx.android.heartfit.GoogleFitManager.FitApiException
import com.nfx.android.heartfit.network.HeartRateDataInterface
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.util.*

/**
 * NFX Development
 * Created by nick on 8/8/17.
 */
class HeartRatePresenter(private val heartRateView: HeartRateView,
                         private val heartRateDataInterface: HeartRateDataInterface) {

    private val TAG = HeartRatePresenter::class.java.name

    fun getHeartRateDataForDate(calendar: Calendar) {
        heartRateDataInterface.getHeartRateData(calendar, 24)
                .observeOn(Schedulers.io())
                .flatMapObservable { t -> Observable.fromIterable(t)}
                .toList()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ heartRateView.updateGraphData(it) } , {t ->
                    if(t is FitApiException) {
                        Log.e(TAG, t.message)
                    }
                })

        heartRateDataInterface.getAverageHeartRate(calendar)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe( { heartRateView.updateAverageHeartRate(it.toInt()) } , {t ->
                    if(t is FitApiException) {
                        Log.e(TAG, t.message)
                    }
                })
        heartRateDataInterface.getMaxHeartRate(calendar)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ heartRateData ->
                    heartRateView.updateMaximumHeartRate(heartRateData.value.toInt()) } , {t ->
                    if(t is FitApiException) {
                        Log.e(TAG, t.message)
                    }
                })
        heartRateDataInterface.getMinHeartRate(calendar)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ heartRateView.updateMinimumHeartRate(it.value.toInt()) } , {t ->
                    if(t is FitApiException) {
                        Log.e(TAG, t.message)
                    }
                })
    }
}