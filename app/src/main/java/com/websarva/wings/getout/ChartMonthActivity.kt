package com.websarva.wings.getout

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet
import com.github.mikephil.charting.charts.BarChart

class ChartMonthActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chart_month)

        //表示用サンプルデータの作成
        val x = listOf<Float>(1f, 2f, 3f, 4f, 6f, 7f, 8f, 9f)//X軸データ
        val y = x.map{it*it}//Y軸データ（X軸の2乗）

        //Entryにデータ格納
        val entryList = mutableListOf<BarEntry>()
        for(i in x.indices){
            entryList.add(
                BarEntry(x[i], y[i])
            )
        }

        //BarDataSetのリスト
        val barDataSets = mutableListOf<IBarDataSet>()
        //DataSetにデータ格納
        val barDataSet = BarDataSet(entryList, "square")
        //DataSetのフォーマット指定
        barDataSet.color = Color.BLUE
        //リストに格納
        barDataSets.add(barDataSet)

        //BarDataにBarDataSet格納
        val barData = BarData(barDataSets)
        //BarChartにBarData格納
        val barChart = findViewById<BarChart>(R.id.barChartExample)
        barChart.data = barData
        //Chartのフォーマット指定
        //X軸の設定
        barChart.xAxis.apply {
            isEnabled = true
            textColor = Color.BLACK
        }
        //barchart更新
        barChart.invalidate()
    }
}
