package com.nfx.android.heartfit

import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.formatter.IAxisValueFormatter
import com.nfx.android.heartfit.dependancyinjection.utils.Time
import java.text.DateFormat
import java.util.*

/**
* NFX Development
* Created by Nick on 08/06/17.
*/
class HourAxisValueFormatter(private val dateFormat: DateFormat): IAxisValueFormatter {

    override fun getFormattedValue(value: Float, axis: AxisBase?): String {
        val convertedTimestamp = Time.minutesToMilliseconds(value.toLong())
        return getHour(convertedTimestamp)
    }

    private fun getHour(timestamp: Long): String {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timestamp
        val date = calendar.time
        return dateFormat.format(date)
    }
}