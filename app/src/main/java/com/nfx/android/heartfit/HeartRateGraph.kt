package com.nfx.android.heartfit

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.support.v4.content.ContextCompat
import android.support.v4.view.GestureDetectorCompat
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import com.github.badoualy.datepicker.DatePickerTimeline
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.github.mikephil.charting.listener.ChartTouchListener
import com.github.mikephil.charting.listener.OnChartGestureListener
import com.nfx.android.heartfit.datainterface.GoogleFitHeartRateInterface
import com.nfx.android.heartfit.datainterface.HeartRateDataInterface
import com.nfx.android.heartfit.dependancyinjection.BaseActivity
import com.nfx.android.heartfit.dependancyinjection.utils.Time
import com.nfx.android.heartfit.model.HeartRateData
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject


/**
 * NFX Development
 * Created by nick on 7/27/17.
 */
class HeartRateGraph : BaseActivity(), HeartRateView {
    // View Binding via butterKnife
    private val mainLayout: ViewGroup by bindView(R.id.main_layout)
    private val averageHeartRate: TextView by bindView(R.id.average_heart_rate)
    private val minHeartRate: TextView by bindView(R.id.min_heart_rate)
    private val maxHeartRate: TextView by bindView(R.id.max_heart_rate)
    private val datePicker: DatePickerTimeline by bindView(R.id.date_picker)
    private val fetchInProgress: ProgressBar by bindView(R.id.fetch_in_progress)
    private val heartRateSummary: ViewGroup by bindView(R.id.heart_rate_summary)
    val lineChart: LineChart by bindView(R.id.line_chart)

    @Inject lateinit var heartRateDataInterface: HeartRateDataInterface
    private lateinit var heartRatePresenter: HeartRatePresenter
    private lateinit var detector: GestureDetectorCompat

    // system ui hider
    private val mHideHandler = Handler()
    private val mHideRunnable = Runnable { hide() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        component.inject(this)

        setContentView(R.layout.activity_heart_rate_graph)

        heartRatePresenter = HeartRatePresenter(this, heartRateDataInterface)

        setupGraphView()
        setupGraphsYAxis()
        setupGraphsXAxis()

        setupDatePicker()

        setupSwipeListener()

        if (heartRateDataInterface is GoogleFitHeartRateInterface) {
            (heartRateDataInterface as GoogleFitHeartRateInterface).connectToManager()
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        detector.onTouchEvent(event)
        return super.onTouchEvent(event)
    }

    private fun setupSwipeListener() {
        detector = GestureDetectorCompat(this, object : GestureDetector.OnGestureListener {
            override fun onShowPress(p0: MotionEvent?) {
            }

            override fun onSingleTapUp(p0: MotionEvent?): Boolean {
                return false
            }

            override fun onDown(p0: MotionEvent?): Boolean {
                return false
            }

            override fun onFling(p0: MotionEvent?, p1: MotionEvent?, p2: Float, p3: Float): Boolean {
                if (p0 != null && p1 != null) {
                    if (p0.x > p1.x) {
                        selectNextDay()
                    } else {
                        selectPreviousDay()
                    }
                    return true
                }
                return false
            }

            override fun onScroll(p0: MotionEvent?, p1: MotionEvent?, p2: Float, p3: Float): Boolean {
                return false
            }

            override fun onLongPress(p0: MotionEvent?) {

            }
        })
    }

    override fun onStart() {
        super.onStart()

        delayedHide(100)
    }

    override fun onDestroy() {
        super.onDestroy()

        if (heartRateDataInterface is GoogleFitHeartRateInterface) {
            (heartRateDataInterface as GoogleFitHeartRateInterface).disconnectFromManager()
        }
    }

    private fun setupGraphsXAxis() {
        val x = lineChart.xAxis
        x.textColor = ContextCompat.getColor(this, R.color.textDarkSecondary)
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

        // If less than 2 hours there is no enough data to display
        if (TimeUnit.MILLISECONDS.toHours(Math.abs(lastTimestamp - firstTimeStamp)) > 2) {
            if (firstTime.get(Calendar.MINUTE) < 30) {
                firstTime.set(Calendar.MINUTE, 30)
            } else {
                firstTime.add(Calendar.HOUR_OF_DAY, 1)
                firstTime.set(Calendar.MINUTE, 0)
            }

            if (lastTime.get(Calendar.MINUTE) < 30) {
                lastTime.add(Calendar.HOUR_OF_DAY, -1)
                lastTime.set(Calendar.MINUTE, 0)
            } else {
                lastTime.set(Calendar.MINUTE, 30)
            }
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
        y.textColor = ContextCompat.getColor(this, R.color.textDarkSecondary)
    }

    private fun setupGraphView() {
        lineChart.setDrawGridBackground(false)
        lineChart.description.isEnabled = false
        lineChart.animateY(1000)
        lineChart.axisRight.isEnabled = false
        lineChart.legend.isEnabled = false
        lineChart.isScaleYEnabled = false
        lineChart.setViewPortOffsets(0f, 0f, 0f, 0f)

        lineChart.onChartGestureListener = object : OnChartGestureListener {
            override fun onChartGestureEnd(me: MotionEvent?, lastPerformedGesture: ChartTouchListener.ChartGesture?) {
            }

            override fun onChartFling(me1: MotionEvent?, me2: MotionEvent?, velocityX: Float, velocityY: Float) {
                if (lineChart.isFullyZoomedOut) {
                    if (me1 != null && me2 != null) {
                        if (me1.x > me2.x) {
                            selectNextDay()
                        } else {
                            selectPreviousDay()
                        }
                    }
                }
            }

            override fun onChartSingleTapped(me: MotionEvent?) {
            }

            override fun onChartGestureStart(me: MotionEvent?, lastPerformedGesture: ChartTouchListener.ChartGesture?) {
            }

            override fun onChartScale(me: MotionEvent?, scaleX: Float, scaleY: Float) {
            }

            override fun onChartLongPressed(me: MotionEvent?) {
            }

            override fun onChartDoubleTapped(me: MotionEvent?) {
            }

            override fun onChartTranslate(me: MotionEvent?, dX: Float, dY: Float) {
            }
        }

        lineChart.setNoDataText(getString(R.string.no_data_to_display))
        lineChart.setNoDataTextColor(ContextCompat.getColor(this, R.color.textDarkSecondary))
    }

    override fun updateMinimumHeartRate(heartRate: Int) {
        minHeartRate.text = heartRate.toString()
        setFetchFinished()
    }

    override fun updateMaximumHeartRate(heartRate: Int) {
        maxHeartRate.text = heartRate.toString()
        setFetchFinished()
    }

    override fun updateAverageHeartRate(heartRate: Int) {
        averageHeartRate.text = heartRate.toString()
        setFetchFinished()
    }

    override fun updateGraphData(heartRateData: List<HeartRateData>) {
        if(!heartRateData.isEmpty()) {
            setFetchFinished()

            heartRateData.sortedBy { (timestamp) -> timestamp }

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

            fetchHeartRateData(setCalendar)
        }

        // Set initial value
        val setCalendar = Calendar.getInstance()
        setDate(setCalendar)

        fetchHeartRateData(setCalendar)
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

    private fun setFetchInProgress() {
        fetchInProgress.visibility = View.VISIBLE
        heartRateSummary.visibility = View.GONE
        lineChart.visibility = View.GONE
    }

    private fun setFetchFinished() {
        fetchInProgress.visibility = View.GONE
        heartRateSummary.visibility = View.VISIBLE
        lineChart.visibility = View.VISIBLE
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

    private fun fetchHeartRateData(calendar: Calendar) {
        setFetchInProgress()

        heartRatePresenter.getHeartRateDataForDate(calendar)
    }

    private fun selectNextDay() {
        val tomorrow = Calendar.getInstance()
        tomorrow.add(Calendar.DAY_OF_MONTH, 1)
        val nextDay = Calendar.getInstance()
        nextDay.set(datePicker.selectedYear, datePicker.selectedMonth, datePicker.selectedDay)
        nextDay.add(Calendar.DAY_OF_MONTH, 1)

        if (nextDay.before(tomorrow)) {
            setDate(nextDay)
        }
    }

    private fun selectPreviousDay() {
        val previousDay = Calendar.getInstance()
        previousDay.set(datePicker.selectedYear, datePicker.selectedMonth, datePicker.selectedDay)
        previousDay.add(Calendar.DAY_OF_MONTH, -1)

        setDate(previousDay)
    }

    private fun setDate(calendar: Calendar) {
        datePicker.setSelectedDate(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH))
    }
}
