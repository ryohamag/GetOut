package com.websarva.wings.getout

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet
import org.w3c.dom.Text
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.*

class ChartMonthActivity : AppCompatActivity() {
    //データベースヘルパーオブジェクトを作成
    private val _helper = DatabaseHelper(this@ChartMonthActivity)

    // 現在の日付を取得
    var referencedLastYear = LocalDate.now()
    var referencedNextYear = LocalDate.now()
    var referencedCurrentYear = LocalDate.now()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chart_month)

        var toLastYearText = findViewById<Button>(R.id.btToLastYear)
        var toNextYearText = findViewById<Button>(R.id.btToNextYear)
        var CurrentYearText = findViewById<TextView>(R.id.tvCurrentYear)

        // 一年前の日付を計算
        referencedLastYear = referencedLastYear.minusYears(1)
        //一年後の日付を計算
        referencedNextYear = referencedNextYear.plusYears(1)

        // 年の部分を抽出
        val lastYear = referencedLastYear.year
        val nextYear = referencedNextYear.year
        val currentYear = referencedCurrentYear.year

        toLastYearText.text = "$lastYear 年へ"
        toNextYearText.text = "$nextYear 年へ"
        CurrentYearText.text = "$currentYear 年"

        val barChart: BarChart = findViewById(R.id.barChart)
        // X 軸ごとの Y 軸
        val entries: MutableList<Int> = mutableListOf(
            105,
            129,
            85,
            104,
            52,
            84,
            114,
            87,
            99,
            108,
            112,
            129
        )

        // X 軸のタイムスタンプ
        val entriesTimestampMills: MutableList<String> = mutableListOf(
            "Jan.",
            "Feb.",
            "Mar.",
            "Apr.",
            "May.",
            "Jun.",
            "Jul.",
            "Aug.",
            "Sep.",
            "Oct.",
            "Nov.",
            "Dec."
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
            override fun getFormattedValue(value: Float): String {
                val index = value.toInt()
                if (index >= 0 && index < entriesTimestampMills.size) {
                    return entriesTimestampMills[index]
                }
                return "" // リストの範囲外の場合は空文字列を返すか、適切なデフォルト値を返すことも考慮できます
            }
        }


        // X 軸の設定
        barChart.xAxis.apply {
            position = XAxis.XAxisPosition.BOTTOM
            valueFormatter = xAxisFormatter
            setDrawGridLines(false)

            // X 軸のラベル数を月の数に設定
            labelCount = entriesTimestampMills.size

            // ラベルの間隔を調整（1.0fは全ての月を表示するための間隔）
            granularity = 1.0f
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
        val listener = ToYearListener()
        //「先週へ」ボタンであるButtonオブジェクトを取得。
        val btToLastYear = findViewById<Button>(R.id.btToLastYear)
        //「先週へ」ボタンにリスナを設定。
        btToLastYear.setOnClickListener(listener)

        val listenerny = ToYearListener()
        val btToNextWeek = findViewById<Button>(R.id.btToNextYear)
        btToNextWeek.setOnClickListener(listenerny)
    }

    //ボタンをクリックしたときのリスナクラス
    private inner class ToYearListener : View.OnClickListener {
        @SuppressLint("SetTextI18n")
        override fun onClick(view: View) {
            val barChart: BarChart = findViewById(R.id.barChart)
            when(view.id){
                R.id.btToLastYear -> {//去年へボタンが押されたとき
                    var toLastYearText = findViewById<Button>(R.id.btToLastYear)
                    var toNextYearText = findViewById<Button>(R.id.btToNextYear)
                    var CurrentYearText = findViewById<TextView>(R.id.tvCurrentYear)

                    // 一年前の日付を計算
                    referencedLastYear = referencedLastYear.minusYears(1)
                    referencedNextYear = referencedNextYear.minusYears(1)
                    referencedCurrentYear = referencedCurrentYear.minusYears(1)

                    // 年の部分を抽出
                    val lastYear = referencedLastYear.year
                    val nextYear = referencedNextYear.year
                    val CurrentYear = referencedCurrentYear.year

                    toLastYearText.text = "$lastYear 年へ"
                    toNextYearText.text = "$nextYear 年へ"
                    CurrentYearText.text = "$CurrentYear 年"
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
        return timeData
    }
}

