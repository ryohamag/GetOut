package com.websarva.wings.getout

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.Button
import android.util.Log
import android.widget.TextView
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.util.Date
import android.widget.CalendarView
import android.widget.Toast

class MainActivity : AppCompatActivity() {

    //データベースヘルパーオブジェクトを作成
    private val _helper = DatabaseHelper(this@MainActivity)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // CalendarViewに現在日時を設定します。
        val calendarView = findViewById<CalendarView>(R.id.calendarView)
        calendarView.date = System.currentTimeMillis()

        // CalendarViewで日にちが選択された時に呼び出されるリスナー
        val listener = DateChangeListener()
        calendarView.setOnDateChangeListener(listener)

        val btNotification = findViewById<Button>(R.id.btNotification)
        //ボタンクリックのリスナーを設定。
        btNotification.setOnClickListener {
            //インテントオブジェクトを生成。
            val intent2Notification = Intent(this@MainActivity, NotificationActivity::class.java)
            // 設定画面の起動。
            startActivity(intent2Notification)
        }


        val btMonth = findViewById<Button>(R.id.btMonth)
        //ボタンクリックのリスナーを設定。
        btMonth.setOnClickListener {
            //インテントオブジェクトを生成。
            val intent2ChartMonth = Intent(this@MainActivity, ChartMonthActivity::class.java)
            // 月グラフ画面の起動。
            startActivity(intent2ChartMonth)
        }

        val btWeek = findViewById<Button>(R.id.btWeek)
        //ボタンクリックのリスナーを設定。
        btWeek.setOnClickListener {
            //インテントオブジェクトを生成。
            val intent2ChartWeek = Intent(this@MainActivity, ChartWeekActivity::class.java)
            // 月グラフ画面の起動。
            startActivity(intent2ChartWeek)
        }
    }

    override fun onDestroy() {
        _helper.close()
        super.onDestroy()
    }

    fun onReloadButtonClick(view: View){ // サーバー内のデータを確認する
        val db = _helper.writableDatabase
        val sql = "SELECT * FROM GetOutTimeLogs"
        val cursor = db.rawQuery(sql,null)

        var log =""
        var count=0
        while (cursor.moveToNext()) {
            val idxNote = cursor.getColumnIndex("getOutTime")
            log += cursor.getString(idxNote)
            log += " "
//            output.text = log
            count++
        }
        Log.i("count", "count="+count)
        val output = findViewById<TextView>(R.id.tvGetOutTime)
        output.text = log
    }

    fun onGetOutButtonClick(view: View){

        // 現在日時を表示
        val dfDate = SimpleDateFormat("yyyyMMdd")
        val dfTime = SimpleDateFormat("HHmm")
        val date = dfDate.format(Date())
        val time = dfTime.format(Date())
//        val output = findViewById<TextView>(R.id.tvGetOutTime)
//        output.text = date.toString()+time.toString()

        val db = _helper.writableDatabase

        val sqlInsert = "INSERT INTO GetOutTimeLogs (getOutDate, getOutTime) VALUES (?, ?)"
        var stmt = db.compileStatement(sqlInsert)
        //　変数のバインド
        stmt.bindString(1, date.toString())
        stmt.bindString(2, time.toString())

        stmt.executeInsert()

        Log.i("test", date)
    }

    fun onGetHomeButtonClick(view:View){

        // 現在日時を表示
        val df = SimpleDateFormat("yyyyMMdd HH:mm")
        val date = df.format(Date())
        val output = findViewById<TextView>(R.id.tvGetHomeTime)
        output.text = date.toString()



        Log.i("test", date)
    }
    // CalendarViewで日にちが選択された時に呼び出されるリスナークラス
    private inner class DateChangeListener : CalendarView.OnDateChangeListener {
        override fun onSelectedDayChange(calendarView: CalendarView, year: Int, month: Int, dayOfMonth: Int) {
            // monthは0起算のため+1します。
            val displayMonth = month + 1
            Toast.makeText(applicationContext, "$year/$displayMonth/$dayOfMonth", Toast.LENGTH_LONG).show()
        }
    }
}