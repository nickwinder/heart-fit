package com.nfx.android.heartfit.network

import io.reactivex.functions.Consumer
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.*
import org.junit.Test
import java.util.*

/**
 * NFX Development
 * Created by nick on 7/27/17.
 */
class TestHeartRateDataInterfaceTest {
    private var testHeartRateData: HeartRateDataInterface = TestHeartRateDataInterface()

    @Test
    fun testHeartRateData () {
        val resolution = 24

        testHeartRateData.getHeartRateData(Calendar.getInstance(), resolution)
                .subscribe(Consumer{ assertThat(it.size, `is`(equalTo(resolution))) })
    }

    @Test
    fun testDefaultHeartRateData () {
        testHeartRateData.getHeartRateData(Calendar.getInstance())
                .subscribe(Consumer{
                    assertThat(it.size, `is`(equalTo(1440)))
                    for ((timestamp, value) in it) {
                        assertThat(timestamp, greaterThanOrEqualTo(0L))
                        assertThat(value, greaterThanOrEqualTo(0f))
                        assertThat(value, lessThanOrEqualTo(220f))
                    }

                })
    }

    @Test
    fun testMaxHeartRateData () {
        testHeartRateData.getMinHeartRate(Calendar.getInstance())
                .map { minHeartRate ->
                    testHeartRateData.getMaxHeartRate(Calendar.getInstance())
                        .subscribe { maxHeartRate ->
                            assertThat(maxHeartRate.value, greaterThan(0f))
                            assertThat(maxHeartRate.value, lessThan(220f))
                            assertThat(maxHeartRate.value, greaterThanOrEqualTo(minHeartRate.value))
                        } }
                .subscribe()

    }

    @Test
    fun testMinHeartRateData () {
        testHeartRateData.getMaxHeartRate(Calendar.getInstance())
                .map { maxHeartRate ->
                    testHeartRateData.getMaxHeartRate(Calendar.getInstance())
                            .subscribe { minHeartRate ->
                                assertThat(minHeartRate.value, greaterThan(0f))
                                assertThat(minHeartRate.value, lessThan(220f))
                                assertThat(minHeartRate.value, lessThanOrEqualTo(maxHeartRate.value))
                            } }
                .subscribe()
    }

    @Test
    fun testAverageHeartRateData () {
        testHeartRateData.getAverageHeartRate(Calendar.getInstance())
                .subscribe { averageHeartRate ->
                    assertThat(averageHeartRate, greaterThanOrEqualTo(0f))
                    assertThat(averageHeartRate, lessThanOrEqualTo(220f))
                }
    }
}