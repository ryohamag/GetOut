package com.websarva.wings.getout

import android.annotation.SuppressLint
import android.app.Notification.CarExtender
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import com.github.mikephil.charting.charts.BarChart
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

class ChartWeekActivity : AppCompatActivity() {

    // 参照する日付を格納する変数
    var referencedDate = Calendar.getInstance()
    val dayOfWeek = referencedDate.get(Calendar.DAY_OF_WEEK)

    val mainActivity = MainActivity()


    var Sun = referencedDate.toInstant()
    var Mon = Sun.plusSeconds(86400)
    var Tue = Sun.plusSeconds(86400*2)
    var Wed = Sun.plusSeconds(86400*3)
    var Thu = Sun.plusSeconds(86400*4)
    var Fri = Sun.plusSeconds(86400*5)
    var Sat = Sun.plusSeconds(86400*6)

    var SunData = 105
    var MonData = 129
    var TueData = 85
    var WedData = 104
    var ThuData = 52
    var FriData = 84
    var SatData = 114

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

        // X 軸ごとの Y 軸
        val entries: MutableList<String> = mutableListOf()

        val dates = listOf(Sun, Mon, Tue, Wed, Thu, Fri, Sat) // それぞれの日付
        for (date in dates) {
            val time = mainActivity.getTime(date.toString()) // date を文字列に変換して getTime 関数を呼び出す
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
        }

        // Y 軸（左）の設定
        barChart.axisLeft.apply {
            setDrawGridLines(true)
            axisMinimum = 0f
        }

        // Y 軸（右）の設定
        barChart.axisRight.apply {
            isEnabled = false
        }

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
                R.id.btToLastWeek -> {
                    // 新しいデータを作成
                    val newEntries: MutableList<Int> = mutableListOf(
                        activity.SunData,
                        activity.MonData,
                        activity.TueData,
                        activity.WedData,
                        activity.ThuData,
                        activity.FriData,
                        activity.SatData
                    )

                    val newEntryList = newEntries.mapIndexed { index, entry ->
                        BarEntry(
                            index.toFloat(),
                            entry.toFloat()
                        )
                    }

                    // データを更新して再描画
                    val newBarDataSet = BarDataSet(newEntryList, "barChart")
                    newBarDataSet.setDrawValues(false)
                    val newBarData = BarData(mutableListOf<IBarDataSet>(newBarDataSet))

                    // 新しいデータを設定
                    barChart.data = newBarData

                    Log.i("TAG", "${referencedDate.time}")
                    barChart.xAxis.valueFormatter = IndexAxisValueFormatter(getLast7DaysLabels(referencedDate))

                    // データが変更されたことを通知
                    barChart.data.notifyDataChanged()
                    barChart.notifyDataSetChanged()

                    // グラフ描画
                    barChart.invalidate()
                }
                R.id.btToNextWeek -> {
                    // 新しいデータを作成
                    val newEntries: MutableList<Int> = mutableListOf(
                        activity.SunData,
                        activity.MonData,
                        activity.TueData,
                        activity.WedData,
                        activity.ThuData,
                        activity.FriData,
                        activity.SatData
                    )

                    val newEntryList = newEntries.mapIndexed { index, entry ->
                        BarEntry(
                            index.toFloat(),
                            entry.toFloat()
                        )
                    }

                    // データを更新して再描画
                    val newBarDataSet = BarDataSet(newEntryList, "barChart")
                    newBarDataSet.setDrawValues(false)
                    val newBarData = BarData(mutableListOf<IBarDataSet>(newBarDataSet))

                    // 新しいデータを設定
                    barChart.data = newBarData
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


}