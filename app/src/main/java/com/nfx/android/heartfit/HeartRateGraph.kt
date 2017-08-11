package com.nfx.android.heartfit

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.support.v4.content.ContextCompat
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.github.badoualy.datepicker.DatePickerTimeline
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.nfx.android.heartfit.dependancyinjection.BaseActivity
import com.nfx.android.heartfit.dependancyinjection.utils.Time
import com.nfx.android.heartfit.model.HeartRateData
import com.nfx.android.heartfit.network.GoogleFitHeartRateInterface
import com.nfx.android.heartfit.network.HeartRateDataInterface
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject


/**
 * NFX Development
 * Created by nick on 7/27/17.
 */
class HeartRateGraph : BaseActivity(), HeartRateView {
    // View Binding via butterKnife
    val mainLayout: ViewGroup by bindView(R.id.main_layout)
    val averageHeartRate: TextView by bindView(R.id.average_heart_rate)
    val minHeartRate: TextView by bindView(R.id.min_heart_rate)
    val maxHeartRate: TextView by bindView(R.id.max_heart_rate)
    val lineChart: LineChart by bindView(R.id.line_chart)
    val datePicker : DatePickerTimeline by bindView(R.id.date_picker)

    @Inject lateinit var heartRateDataInterface: HeartRateDataInterface
    lateinit var heartRatePresenter: HeartRatePresenter

    // system ui hider
    private val mHideHandler = Handler()
    private val mHideRunnable = Runnable { hide() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        component.inject(this)

        if (heartRateDataInterface is GoogleFitHeartRateInterface) {
            (heartRateDataInterface as GoogleFitHeartRateInterface).connectToManager()
        }

        setContentView(R.layout.activity_heart_rate_graph)

        heartRatePresenter = HeartRatePresenter(this, heartRateDataInterface)

        setupGraphView()
        setupGraphsYAxis()
        setupGraphsXAxis()

        setupDatePicker()
    }

    override fun onStart() {
        super.onStart()

        delayedHide(100)

        val calendar = Calendar.getInstance()
        heartRatePresenter.getHeartRateDataForDate(calendar)
    }

    override fun onStop() {
        super.onStop()

        if (heartRateDataInterface is GoogleFitHeartRateInterface) {
            (heartRateDataInterface as GoogleFitHeartRateInterface).disconnectFromManager()
        }
    }

    private fun setupGraphsXAxis() {
        val x = lineChart.xAxis
        x.textColor = ContextCompat.getColor(this, android.R.color.primary_text_light)
        x.position = XAxis.XAxisPosition.BOTTOM_INSIDE
        x.setDrawGridLines(false)
        x.setDrawAxisLine(false)
        x.labelRotationAngle = 270f
        x.textSize = 16f
        x.granularity = 30f
        x.isGranularityEnabled = true


        val xAxisFormatter = HourAxisValueFormatter(SimpleDateFormat("HH:mm", Locale.getDefault()))
        x.valueFormatter = xAxisFormatter
    }

    private fun setXAxisLimits(x: XAxis, firstTimeStamp: Long, lastTimestamp: Long) {
        val firstTime = Calendar.getInstance()
        firstTime.timeInMillis = firstTimeStamp
        val lastTime = Calendar.getInstance()
        lastTime.timeInMillis = lastTimestamp

        if(firstTime.get(Calendar.MINUTE) < 30) {
            firstTime.set(Calendar.MINUTE, 30)
        } else {
            firstTime.add(Calendar.HOUR_OF_DAY, 1)
            firstTime.set(Calendar.MINUTE , 0)
        }

        if(lastTime.get(Calendar.MINUTE) < 30) {
            lastTime.add(Calendar.HOUR_OF_DAY, -1)
            lastTime.set(Calendar.MINUTE, 0)
        } else {
            lastTime.set(Calendar.MINUTE , 30)
        }

        x.axisMinimum = Time.millisecondsToMinutes(firstTime.timeInMillis).toFloat()
        x.axisMaximum = Time.millisecondsToMinutes(lastTime.timeInMillis).toFloat()

    }

    private fun setupGraphsYAxis() {
        val y = lineChart.axisLeft
        y.textColor = ContextCompat.getColor(this, android.R.color.primary_text_light)
        y.setPosition(YAxis.YAxisLabelPosition.INSIDE_CHART)
        y.setDrawGridLines(false)
        y.gridColor = ContextCompat.getColor(this, android.R.color.tertiary_text_light)
        y.setDrawAxisLine(false)
        y.axisMinimum = 1f
        y.axisMaximum = 199f
        y.labelCount = 6
        y.textSize = 16f
    }

    private fun setupGraphView() {
        lineChart.setDrawGridBackground(false)
        lineChart.description.isEnabled = false
        lineChart.animateY(1000)
        lineChart.axisRight.isEnabled = false
        lineChart.legend.isEnabled = false
        lineChart.isScaleYEnabled = false
        lineChart.setViewPortOffsets(0f, 0f, 0f, 0f)
    }

    override fun updateMinimumHeartRate(heartRate: Int) {
        minHeartRate.text = heartRate.toString()
    }

    override fun updateMaximumHeartRate(heartRate: Int) {
        maxHeartRate.text = heartRate.toString()
    }

    override fun updateAverageHeartRate(heartRate: Int) {
        averageHeartRate.text = heartRate.toString()
    }

    override fun updateGraphData(heartRateData: List<HeartRateData>) {
        if(!heartRateData.isEmpty()) {
            val entryList: MutableList<Entry> = mutableListOf()

            // Scale the axis so all data is on screen
            val firstTimestamp = heartRateData.first().timestamp
            val lastTimestamp = heartRateData.last().timestamp
            setXAxisLimits(lineChart.xAxis, firstTimestamp, lastTimestamp)

            repeat(heartRateData.size) { i ->
                entryList.add(Entry(Time.millisecondsToMinutes(heartRateData[i].timestamp).toFloat(),
                        heartRateData[i].value))
            }

            // Update current data set if present, if not create a new data set
            if (lineChart.data != null && lineChart.data.dataSetCount > 0) {
                val lineDataSet = lineChart.data.getDataSetByIndex(0) as LineDataSet
                lineDataSet.values = entryList
            } else {
                val lineDataSet = setupLineData(entryList)
                val dataSets = mutableListOf<ILineDataSet>(lineDataSet)
                val data = LineData(dataSets)

                lineChart.data = data
                lineChart.data.isHighlightEnabled = false
            }

            val day = Calendar.getInstance()
            day.timeInMillis = heartRateData[0].timestamp

            lineChart.data.notifyDataChanged()
        } else {
            if (lineChart.data != null && lineChart.data.dataSetCount > 0) {
                lineChart.data = null
            }
        }

        lineChart.notifyDataSetChanged()
        lineChart.animateY(1000)
        lineChart.invalidate()
    }

    private fun setupDatePicker() {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_MONTH, 1)
        datePicker.setLastVisibleDate(calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH))

        datePicker.setOnDateSelectedListener { year, month, day, _ ->
            val setCalendar = Calendar.getInstance()
            setCalendar.set(year, month, day)

            heartRatePresenter.getHeartRateDataForDate(setCalendar)
        }
    }

    private fun setupLineData(entryList: MutableList<Entry>): LineDataSet {
        val lineDataSet = LineDataSet(entryList, "DataSet 1")
        lineDataSet.disableDashedLine()
        lineDataSet.setDrawCircles(false)
        lineDataSet.fillColor = ContextCompat.getColor(this, R.color.colorPrimary)
        lineDataSet.fillAlpha = 255
        lineDataSet.color = Color.WHITE
        lineDataSet.setDrawFilled(true)
        lineDataSet.setDrawValues(false)

        lineDataSet.mode = LineDataSet.Mode.CUBIC_BEZIER
        lineDataSet.cubicIntensity = 0.18f
        return lineDataSet
    }

    @SuppressLint("InlinedApi")
    private fun hide() {
        // Hide UI first
        val actionBar = supportActionBar
        actionBar?.hide()

        mainLayout.systemUiVisibility = View.SYSTEM_UI_FLAG_LOW_PROFILE
    }

    private fun delayedHide(delayMillis: Int) {
        mHideHandler.removeCallbacks(mHideRunnable)
        mHideHandler.postDelayed(mHideRunnable, delayMillis.toLong())
    }
}
