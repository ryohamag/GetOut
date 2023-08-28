package com.websarva.wings.getout

import android.annotation.SuppressLint
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
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
import java.util.*
import java.util.Calendar
import java.util.Date
import java.util.Locale

class ChartWeekActivity : AppCompatActivity() {
    var Sun: Long = 1693753200000  //2023/09/03 00:00:00
    var Mon: Long = 1693839600000  //2023/09/04 00:00:00
    var Tue: Long = 1693926000000  //2023/09/05 00:00:00
    var Wed: Long = 1694012400000  //2023/09/06 00:00:00
    var Thu: Long = 1694098800000  //2023/09/07 00:00:00
    var Fri: Long = 1694185200000  //2023/09/08 00:00:00
    var Sat: Long = 1694271600000  //2023/09/09 00:00:00

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

        // X 軸ごとの Y 軸
        val entries: MutableList<Int> = mutableListOf(
            SunData,
            MonData,
            TueData,
            WedData,
            ThuData,
            FriData,
            SatData
        )


        // X 軸のタイムスタンプ
        val entriesTimestampMills: MutableList<Long> = mutableListOf(
            Sun,  // 2021/09/03 00:00:00
            Mon,  // 2021/09/04 00:00:00
            Tue,  // 2021/09/05 00:00:00
            Wed,  // 2021/09/06 00:00:00
            Thu,  // 2021/09/07 00:00:00
            Fri,  // 2023/09/08 00:00:00
            Sat   // 2021/09/09 00:00:00
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
                val date = Date(timestampMills)
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


    private val dateFormatter = SimpleDateFormat("MM/dd", Locale.getDefault())

    private fun getLast7DaysLabels(): List<String> {
        val labels = ArrayList<String>()
        val calendar = Calendar.getInstance()

        for (i in 6 downTo 0) {
            calendar.add(Calendar.DAY_OF_YEAR, -1)
            labels.add(dateFormatter.format(calendar.time))
        }

        return labels.reversed()
    }

    private fun getNext7DaysLabels(): List<String> {
        val labels = ArrayList<String>()
        val calendar = Calendar.getInstance()

        for (i in 1 ..7) {
            calendar.add(Calendar.DAY_OF_YEAR, 1)
            labels.add(dateFormatter.format(calendar.time))
        }

        return labels
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

                    barChart.xAxis.valueFormatter = IndexAxisValueFormatter(getLast7DaysLabels())

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

                    barChart.xAxis.valueFormatter = IndexAxisValueFormatter(getNext7DaysLabels())

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