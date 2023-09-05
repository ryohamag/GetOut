package com.websarva.wings.getout

import android.annotation.SuppressLint
import android.app.Notification.CarExtender
import android.content.ContentValues.TAG
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.LimitLine
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet
import java.text.SimpleDateFormat
import java.time.DayOfWeek
import java.time.Instant
import java.util.*
import java.util.Calendar
import java.util.Date
import java.util.Locale
import com.websarva.wings.getout.MainActivity // MainActivity.ktのパッケージ名を適切に指定
import java.time.Duration
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class ChartWeekActivity : AppCompatActivity() {
    //データベースヘルパーオブジェクトを作成
    private val _helper = DatabaseHelper(this@ChartWeekActivity)

    // 参照する日付を格納する変数
    var referencedDate = Calendar.getInstance()
    val dayOfWeek = referencedDate.get(Calendar.DAY_OF_WEEK)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chart_week)

        val barChart: BarChart = findViewById(R.id.barChart)

        referencedDate.add(Calendar.DAY_OF_YEAR, getDayOfWeekAsString(dayOfWeek))

        var Sun = referencedDate.toInstant()
        var Mon = Sun.plusSeconds(86400)
        var Tue = Sun.plusSeconds(86400*2)
        var Wed = Sun.plusSeconds(86400*3)
        var Thu = Sun.plusSeconds(86400*4)
        var Fri = Sun.plusSeconds(86400*5)
        var Sat = Sun.plusSeconds(86400*6)

        val dF = SimpleDateFormat("yyyy-M-d", Locale.getDefault())
        val formattedSunDate = dF.format(referencedDate.time)

        // X 軸ごとの Y 軸
        val entries: MutableList<String> = mutableListOf()

        val formatter = DateTimeFormatter.ofPattern("yyyy-M-d")

        var SunData = LocalDate.parse(formattedSunDate, formatter)
        var MonData = SunData.plusDays(1)
        var TueData = SunData.plusDays(2)
        var WedData = SunData.plusDays(3)
        var ThuData = SunData.plusDays(4)
        var FriData = SunData.plusDays(5)
        var SatData = SunData.plusDays(6)

        val sunFormatted = SunData.format(DateTimeFormatter.ofPattern("yyyy-M-d"))
        val monFormatted = MonData.format(DateTimeFormatter.ofPattern("yyyy-M-d"))
        val tueFormatted = TueData.format(DateTimeFormatter.ofPattern("yyyy-M-d"))
        val wedFormatted = WedData.format(DateTimeFormatter.ofPattern("yyyy-M-d"))
        val thuFormatted = ThuData.format(DateTimeFormatter.ofPattern("yyyy-M-d"))
        val friFormatted = FriData.format(DateTimeFormatter.ofPattern("yyyy-M-d"))
        val satFormatted = SatData.format(DateTimeFormatter.ofPattern("yyyy-M-d"))

        val dates = listOf(sunFormatted, monFormatted, tueFormatted, wedFormatted, thuFormatted, friFormatted, satFormatted) // それぞれの日付
        for (date in dates) {
            val time = getTime(date.toString()) // date を文字列に変換して getTime 関数を呼び出す
            entries.add(time)
        }

        // X 軸のタイムスタンプ
        val entriesTimestampMills: MutableList<Instant> = mutableListOf(
            Sun,
            Mon,
            Tue,
            Wed,
            Thu,
            Fri,
            Sat
        )

        // グラフに描画するデータの設定
        val entryList = entries.mapIndexed { index, entry ->
            BarEntry(
                index.toFloat(),    // X 軸 ここに渡すのはあくまで 0, 1, 2... という index
                entry.toFloat()     // Y 軸
            )
        }

        val barDataSet = BarDataSet(entryList, "barChart")
        barDataSet.setDrawValues(false)

        barChart.data = BarData(mutableListOf<IBarDataSet>(barDataSet))
        barChart.setDrawGridBackground(false)
        barChart.description.isEnabled = false

        barChart.legend.apply {
            isEnabled = false
        }

        // X 軸のフォーマッター
        val xAxisFormatter = object : ValueFormatter() {
            private var simpleDateFormat: SimpleDateFormat =
                SimpleDateFormat("M/d", Locale.getDefault())

            override fun getFormattedValue(value: Float): String {
                // value には 0, 1, 2... という index が入ってくるので
                // index からタイムスタンプを取得する
                val timestampMills = entriesTimestampMills[value.toInt()]
                val date = Date(timestampMills.toEpochMilli())
                return simpleDateFormat.format(date)
            }
        }

        // X 軸の設定
        barChart.xAxis.apply {
            position = XAxis.XAxisPosition.BOTTOM
            valueFormatter = xAxisFormatter
            setDrawGridLines(false)

            textSize = 12f
        }

        // Y 軸（左）の設定
        barChart.axisLeft.apply {
            setDrawGridLines(true)
            axisMinimum = 0f

            textSize = 12f
        }

        // Y 軸（右）の設定
        barChart.axisRight.apply {
            isEnabled = false
        }

        // 直線を作成
        val limitLine = LimitLine(getGoalTime().toFloat(), "目標時間") // Y軸の位置を100に設定
        limitLine.lineWidth = 2f // 直線の太さを設定
        limitLine.lineColor = Color.RED // 直線の色を設定
        // "目標時間"の文字サイズを変更
        limitLine.textSize = 24f

        // Y軸（左）に直線を追加
        val leftAxis = barChart.axisLeft
        leftAxis.addLimitLine(limitLine)



        // グラフ描画
        barChart.invalidate()

        //リスナクラスのインスタンスを生成。
        val listener = ToWeekListener(this)
        //「先週へ」ボタンであるButtonオブジェクトを取得。
        val btToLastWeek = findViewById<Button>(R.id.btToLastWeek)
        //「先週へ」ボタンにリスナを設定。
        btToLastWeek.setOnClickListener(listener)

        val listenernw = ToWeekListener(this)
        val btToNextWeek = findViewById<Button>(R.id.btToNextWeek)
        btToNextWeek.setOnClickListener(listenernw)
    }


    private val dateFormatter = SimpleDateFormat("M/d", Locale.getDefault())

    private fun getLast7DaysLabels(referencedDate: Calendar): List<String> {
        val labels = ArrayList<String>()
        referencedDate.add(Calendar.DAY_OF_YEAR, -8)

        for (i in 0 .. 6) {
            referencedDate.add(Calendar.DAY_OF_YEAR, 1)
            labels.add(dateFormatter.format(referencedDate.time))
        }
        referencedDate.add(Calendar.DAY_OF_YEAR, -6)

        return labels
    }

    private fun getNext7DaysLabels(referencedDate: Calendar): List<String> {
        val labels = ArrayList<String>()
        referencedDate.add(Calendar.DAY_OF_YEAR, 6)

        for (i in 0 .. 6) {
            referencedDate.add(Calendar.DAY_OF_YEAR, 1)

            labels.add(dateFormatter.format(referencedDate.time))
        }
        referencedDate.add(Calendar.DAY_OF_YEAR, -6)

        return labels
    }

    private fun getDayOfWeekAsString(dayOfWeek: Int): Int {
        return when (dayOfWeek) {
            Calendar.SUNDAY -> 0
            Calendar.MONDAY -> -1
            Calendar.TUESDAY -> -2
            Calendar.WEDNESDAY -> -3
            Calendar.THURSDAY -> -4
            Calendar.FRIDAY -> -5
            Calendar.SATURDAY -> -6
            else -> -7
        }
    }


    private inner class ToWeekListener(private val activity: ChartWeekActivity): View.OnClickListener {
        override fun onClick(view: View){
            val barChart: BarChart = activity.findViewById(R.id.barChart)
            when(view.id){
                R.id.btToLastWeek -> { //先週へボタンが押されたとき
                    val dF = SimpleDateFormat("yyyy-M-d", Locale.getDefault())
                    val formattedSunDate = dF.format(referencedDate.time)

                    val formatter = DateTimeFormatter.ofPattern("yyyy-M-d")

                    var SunData = LocalDate.parse(formattedSunDate, formatter)
                    var MonData = SunData.plusDays(1)
                    var TueData = SunData.plusDays(2)
                    var WedData = SunData.plusDays(3)
                    var ThuData = SunData.plusDays(4)
                    var FriData = SunData.plusDays(5)
                    var SatData = SunData.plusDays(6)

                    // 1週間分戻す
                    SunData = SunData.minusWeeks(1)
                    MonData = MonData.minusWeeks(1)
                    TueData = TueData.minusWeeks(1)
                    WedData = WedData.minusWeeks(1)
                    ThuData = ThuData.minusWeeks(1)
                    FriData = FriData.minusWeeks(1)
                    SatData = SatData.minusWeeks(1)

                    // フォーマットし直す
                    val sunFormatted = SunData.format(DateTimeFormatter.ofPattern("yyyy-M-d"))
                    val monFormatted = MonData.format(DateTimeFormatter.ofPattern("yyyy-M-d"))
                    val tueFormatted = TueData.format(DateTimeFormatter.ofPattern("yyyy-M-d"))
                    val wedFormatted = WedData.format(DateTimeFormatter.ofPattern("yyyy-M-d"))
                    val thuFormatted = ThuData.format(DateTimeFormatter.ofPattern("yyyy-M-d"))
                    val friFormatted = FriData.format(DateTimeFormatter.ofPattern("yyyy-M-d"))
                    val satFormatted = SatData.format(DateTimeFormatter.ofPattern("yyyy-M-d"))

                    val entries: MutableList<String> = mutableListOf()

                    val dates = listOf(sunFormatted, monFormatted, tueFormatted, wedFormatted, thuFormatted, friFormatted, satFormatted) // それぞれの日付
                    for (date in dates) {
                        val time = getTime(date.toString()) // date を文字列に変換して getTime 関数を呼び出す
                        entries.add(time)
                    }
                    val entryList = entries.mapIndexed { index, entry ->
                        BarEntry(
                            index.toFloat(),    // X 軸 ここに渡すのはあくまで 0, 1, 2... という index
                            entry.toFloat()     // Y 軸
                        )
                    }

                    val barDataSet = BarDataSet(entryList, "barChart")
                    barDataSet.setDrawValues(false)

                    barChart.data = BarData(mutableListOf<IBarDataSet>(barDataSet))
                    barChart.setDrawGridBackground(false)
                    barChart.description.isEnabled = false

                    barChart.legend.apply {
                        isEnabled = false
                    }
                    barChart.axisLeft.apply {
                        setDrawGridLines(true)
                        axisMinimum = 0f
                    }

                    // Y 軸（右）の設定
                    barChart.axisRight.apply {
                        isEnabled = false
                    }

                    barChart.xAxis.valueFormatter = IndexAxisValueFormatter(getLast7DaysLabels(referencedDate))

                    // データが変更されたことを通知
                    barChart.data.notifyDataChanged()
                    barChart.notifyDataSetChanged()

                    // グラフ描画
                    barChart.invalidate()
                }
                R.id.btToNextWeek -> {
                    val dF = SimpleDateFormat("yyyy-M-d", Locale.getDefault())
                    val formattedSunDate = dF.format(referencedDate.time)

                    val formatter = DateTimeFormatter.ofPattern("yyyy-M-d")

                    var SunData = LocalDate.parse(formattedSunDate, formatter)
                    var MonData = SunData.plusDays(1)
                    var TueData = SunData.plusDays(2)
                    var WedData = SunData.plusDays(3)
                    var ThuData = SunData.plusDays(4)
                    var FriData = SunData.plusDays(5)
                    var SatData = SunData.plusDays(6)

                    // 1週間分戻す
                    SunData = SunData.plusWeeks(1)
                    MonData = MonData.plusWeeks(1)
                    TueData = TueData.plusWeeks(1)
                    WedData = WedData.plusWeeks(1)
                    ThuData = ThuData.plusWeeks(1)
                    FriData = FriData.plusWeeks(1)
                    SatData = SatData.plusWeeks(1)

                    // フォーマットし直す
                    val sunFormatted = SunData.format(DateTimeFormatter.ofPattern("yyyy-M-d"))
                    val monFormatted = MonData.format(DateTimeFormatter.ofPattern("yyyy-M-d"))
                    val tueFormatted = TueData.format(DateTimeFormatter.ofPattern("yyyy-M-d"))
                    val wedFormatted = WedData.format(DateTimeFormatter.ofPattern("yyyy-M-d"))
                    val thuFormatted = ThuData.format(DateTimeFormatter.ofPattern("yyyy-M-d"))
                    val friFormatted = FriData.format(DateTimeFormatter.ofPattern("yyyy-M-d"))
                    val satFormatted = SatData.format(DateTimeFormatter.ofPattern("yyyy-M-d"))

                    val entries: MutableList<String> = mutableListOf()

                    val dates = listOf(sunFormatted, monFormatted, tueFormatted, wedFormatted, thuFormatted, friFormatted, satFormatted) // それぞれの日付
                    for (date in dates) {
                        val time = getTime(date.toString()) // date を文字列に変換して getTime 関数を呼び出す
                        entries.add(time)
                    }
                    val entryList = entries.mapIndexed { index, entry ->
                        BarEntry(
                            index.toFloat(),    // X 軸 ここに渡すのはあくまで 0, 1, 2... という index
                            entry.toFloat()     // Y 軸
                        )
                    }

                    val barDataSet = BarDataSet(entryList, "barChart")
                    barDataSet.setDrawValues(false)

                    barChart.data = BarData(mutableListOf<IBarDataSet>(barDataSet))
                    barChart.setDrawGridBackground(false)
                    barChart.description.isEnabled = false

                    barChart.legend.apply {
                        isEnabled = false
                    }
                    barChart.axisLeft.apply {
                        setDrawGridLines(true)
                        axisMinimum = 0f
                    }

                    // Y 軸（右）の設定
                    barChart.axisRight.apply {
                        isEnabled = false
                    }

                    barChart.xAxis.valueFormatter = IndexAxisValueFormatter(getNext7DaysLabels(referencedDate))

                    // データが変更されたことを通知
                    barChart.data.notifyDataChanged()
                    barChart.notifyDataSetChanged()

                    // グラフ描画
                    barChart.invalidate()
                }
            }
        }
    }

    // YYYY-M-d形式の日付から総外出時間を所得する
    fun getTime(date: String): String{
        val db = _helper.writableDatabase

        // TimeSumLogからデータを所得
        val sql = "SELECT * FROM TimeSumLog WHERE Date = ?"
        val selectionArgs = arrayOf(date)
        val cursor = db.rawQuery(sql, selectionArgs)

        var timeData = ""
        while(cursor.moveToNext()) {
            val timeIdxNote = cursor.getColumnIndex("Time")
            timeData = cursor.getString(timeIdxNote)
        }
        db.close()
        return if (timeData.isEmpty()) {
            "0"
        } else {
            timeData
        }
    }

    fun getGoalTime(): String{
        val datesWithStatus = mutableListOf<DateStatus>()
        val db = _helper.writableDatabase

        // TimeSumLogからデータを所得
        val sql = "SELECT * FROM GoalTimeLog"
        val cursor = db.rawQuery(sql,null)

        var timeData = ""
        while(cursor.moveToNext()) {
            val timeIdxNote = cursor.getColumnIndex("GoalTimeMin")
            timeData = cursor.getString(timeIdxNote)
        }
        db.close()

        return timeData
    }
}

