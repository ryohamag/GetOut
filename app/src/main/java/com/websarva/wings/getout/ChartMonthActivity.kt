package com.websarva.wings.getout

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter

class ChartMonthActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chart_month)

        //表示用サンプルデータの作成
        val x = listOf<Float>(1f, 2f, 3f, 4f, 6f, 7f, 8f, 9f, 10f, 11f, 12f)//X軸データ
        val y = x.map{it*it}//Y軸データ（X軸の2乗）

        //Entryにデータ格納
        val entryList = mutableListOf<BarEntry>()
        for(i in x.indices){
            entryList.add(
                BarEntry(x[i], y[i])
            )
        }


        //DataSetにデータ格納
        val barDataSet = BarDataSet(entryList, "square")
        //DataSetのフォーマット指定
        barDataSet.color = Color.BLUE


        //BarDataにBarDataSet格納
        val barData = BarData(barDataSet)
        //BarChartにBarData格納
        val barChart = findViewById<BarChart>(R.id.barChartExample)
        barChart.data = barData
        barChart.legend.isEnabled= false
        barChart.description.text = ""
        barChart.setScaleEnabled(false)
        barChart.setPinchZoom(false)
        //Chartのフォーマット指定
        //X軸の設定
        val labels = x.map { it.toInt().toString() }.toTypedArray()
        barChart.xAxis.apply {
            isEnabled = true
            textColor = Color.BLACK
            valueFormatter = IndexAxisValueFormatter(labels)

        }
        //barchart更新
        barChart.invalidate()
    }
}
