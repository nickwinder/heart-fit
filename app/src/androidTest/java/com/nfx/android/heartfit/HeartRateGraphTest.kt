package com.nfx.android.heartfit

import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.espresso.matcher.ViewMatchers.*
import android.support.test.rule.ActivityTestRule
import android.view.View
import com.nfx.android.heartfit.dependancyinjection.components.DaggerActivityComponent
import com.nfx.android.heartfit.dependancyinjection.module.ActivityModule
import com.nfx.android.heartfit.dependancyinjection.module.TestNetworkModule
import org.hamcrest.Matchers.allOf
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * NFX Development
 * Created by nick on 8/17/17.
 */
class HeartRateGraphTest {
    @Suppress("unused")
    @get:Rule
    private val mActivityRule: ActivityTestRule<HeartRateGraph> = ActivityTestRule(HeartRateGraph::class.java)
    private val testNetworkModule = TestNetworkModule()

    @Before
    fun setUp() {
        mActivityRule.activity.component = DaggerActivityComponent
                .builder()
                .activityModule(ActivityModule(mActivityRule.activity))
                .networkModule(testNetworkModule)
                .build()
    }

    @Test
    fun initialScreen() {
        try {
            Thread.sleep(1000)
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
        onView(allOf<View>(withId(R.id.average_heart_rate), isDisplayed()))
                .check(matches(withText(testNetworkModule.average.toString())))

        onView(allOf<View>(withId(R.id.max_heart_rate), isDisplayed()))
                .check(matches(withText(testNetworkModule.maxValue.toString())))

        onView(allOf<View>(withId(R.id.min_heart_rate), isDisplayed()))
                .check(matches(withText(testNetworkModule.minValue.toString())))

        onView(allOf<View>(withId(R.id.line_chart)))
                .check(matches(isDisplayed()))
    }

}