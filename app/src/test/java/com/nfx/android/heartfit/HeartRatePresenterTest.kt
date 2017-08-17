package com.nfx.android.heartfit

import com.google.android.gms.common.api.Status
import com.nfx.android.heartfit.GoogleFitManager.FitApiException
import com.nfx.android.heartfit.model.HeartRateData
import com.nfx.android.heartfit.network.GoogleFitHeartRateInterface
import com.nfx.android.heartfit.network.HeartRateDataInterface
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.never
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import io.reactivex.Single
import io.reactivex.rxkotlin.toSingle
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito
import java.util.Calendar
import kotlin.collections.ArrayList

/**
 * NFX Development
 * Created by nick on 8/17/17.
 */
class HeartRatePresenterTest {
    @Suppress("unused")
    @get:Rule
    val trampolineScheduleRule = TrampolineSchedulerRule()

    private lateinit var heartRateDataInterface: HeartRateDataInterface
    private lateinit var heartRatePresenter: HeartRatePresenter
    private lateinit var heartRateView: HeartRateView

    @Before
    fun setUp() {
        heartRateDataInterface = Mockito.mock(GoogleFitHeartRateInterface::class.java)

        heartRateView = Mockito.mock(HeartRateView::class.java)

        heartRatePresenter = HeartRatePresenter(heartRateView, heartRateDataInterface)

    }

    @Test
    fun testRealDataPassedThroughPresenter() {
        val minValue = 10f
        val maxValue = 100f
        val listOfHeartRateData = listOf(HeartRateData(0, minValue), HeartRateData(1, maxValue))

        val average = setupMockCalls(listOfHeartRateData)

        heartRatePresenter.getHeartRateDataForDate(Calendar.getInstance())

        verify(heartRateView).updateMaximumHeartRate(maxValue.toInt())
        verify(heartRateView).updateMinimumHeartRate(minValue.toInt())
        verify(heartRateView).updateAverageHeartRate(average)
        verify(heartRateView).updateGraphData(listOfHeartRateData)
    }

    @Test
    fun testErrorConditions() {
        val fitApiException = FitApiException(Status(0, "Some type of Error"))
        whenever(heartRateDataInterface.getHeartRateData(any(), any()))
                .thenReturn(Single.error(fitApiException))
        whenever(heartRateDataInterface.getMinHeartRate(any()))
                .thenReturn(Single.error(fitApiException))
        whenever(heartRateDataInterface.getMaxHeartRate(any()))
                .thenReturn(Single.error(fitApiException))
        whenever(heartRateDataInterface.getAverageHeartRate(any()))
                .thenReturn(Single.error(fitApiException))

        heartRatePresenter.getHeartRateDataForDate(Calendar.getInstance())

        verify(heartRateView, never()).updateMaximumHeartRate(any())
        verify(heartRateView, never()).updateMinimumHeartRate(any())
        verify(heartRateView, never()).updateAverageHeartRate(any())
        verify(heartRateView, never()).updateGraphData(any())
    }

    @Test
    fun testLargeSetOfData() {
        val numberOfValues = 10000
        val minValue = 0f
        val maxValue = 200f
        val listOfHeartRateData = ArrayList<HeartRateData>()

        repeat(numberOfValues,
                { i: Int -> listOfHeartRateData.add(i, HeartRateData(i.toLong(), (Math.random() * maxValue).toFloat())) })

        listOfHeartRateData.first().value = minValue
        listOfHeartRateData.last().value = maxValue

        val average = setupMockCalls(listOfHeartRateData)

        heartRatePresenter.getHeartRateDataForDate(Calendar.getInstance())

        verify(heartRateView).updateMaximumHeartRate(maxValue.toInt())
        verify(heartRateView).updateMinimumHeartRate(minValue.toInt())
        verify(heartRateView).updateAverageHeartRate(average)
        verify(heartRateView).updateGraphData(listOfHeartRateData)
    }

    @After
    fun tearDown() {
    }

    private fun setupMockCalls(listOfDataPoints: List<HeartRateData>): Int {
        var average = 0f
        listOfDataPoints.forEach { average += it.value }
        average /= listOfDataPoints.size

        whenever(heartRateDataInterface.getAverageHeartRate(any())).thenReturn(Single.just(average))
        whenever(heartRateDataInterface.getMaxHeartRate(any())).thenReturn(
                Single.just(listOfDataPoints.maxBy { heartRateData -> heartRateData.value }))
        whenever(heartRateDataInterface.getMinHeartRate(any())).thenReturn(
                Single.just(listOfDataPoints.minBy { heartRateData -> heartRateData.value }))

        whenever(heartRateDataInterface.getHeartRateData(any(), any())).thenReturn(listOfDataPoints.toSingle())

        return average.toInt()
    }

}