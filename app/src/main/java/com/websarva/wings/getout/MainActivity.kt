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

    // 帰宅状態：０　外出状態：１を示すフラグ
    var statusFlag = 0

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

        // 外出の方
        var sql = "SELECT * FROM GetOutTimeLogs"
        var cursor = db.rawQuery(sql,null)

        var log =""
        var count = 0
        while (cursor.moveToNext()) {
            val idxNote = cursor.getColumnIndex("getOutMin")
            // 所得したデータをlogに追加
            log += cursor.getString(idxNote)
            log += " "
//            output.text = log
            count++
        }
        Log.i("count", "GetOutCount=" + count)
        // tvGetOutTimeにlogのテキストを設定
        val GetOutOutput = findViewById<TextView>(R.id.tvGetOutTime)
        GetOutOutput.text = log


        // 帰宅の方
        sql = "SELECT * FROM GetHomeTimeLogs"
        cursor = db.rawQuery(sql,null)

        log =""
        count = 0 // index数を数える
        while (cursor.moveToNext()) {
            val idxNote = cursor.getColumnIndex("getHomeMin")
            //　所得したデータをlogに追加
            log += cursor.getString(idxNote)
            log += " "
//            output.text = log
            count++
        }
        Log.i("count", "GetHomeCount = " + count)
        // tvGetHomeTimeにlogのテキストを反映させる。
        val GetHomeOutput = findViewById<TextView>(R.id.tvGetHomeTime)
        GetHomeOutput.text = log
    }
    fun onGetOutButtonClick(view: View){ // 外出ボタンを押したときの処理
        // もし外出状態ならば
        if(statusFlag === 1){
            // 関数を終了する
            return
        }

        // 外出フラグを立てる
        statusFlag = 1

        // 現在日時を所得
        val dfDate = SimpleDateFormat("yyyyMMdd")
        val dfHour = SimpleDateFormat("HH")
        val dfMin = SimpleDateFormat("mm")
        val date = dfDate.format(Date())
        val hour = dfHour.format(Date())
        val min = dfMin.format(Date())
//        val output = findViewById<TextView>(R.id.tvGetOutTime)
//        output.text = date.toString()+time.toString()

        val db = _helper.writableDatabase

        //　現在日時をデータベースに記述
        val sqlInsert = "INSERT INTO GetOutTimeLogs (getOutDate, getOutHour, getOutMin) VALUES (?, ?, ?)"
        var stmt = db.compileStatement(sqlInsert)
        //　変数のバインド
        stmt.bindString(1, date.toString())
        stmt.bindString(2, hour.toString())
        stmt.bindString(3, min.toString())

        stmt.executeInsert()

        Log.i("test", date)
    }

    fun onGetHomeButtonClick(view:View){ // 帰宅ボタンを押したときの処理
        // もし在宅状態ならば
        if(statusFlag === 0){
            // 関数を終了する
            return
        }

        // 外出フラグを立てる
        statusFlag = 0

        // 現在日時を所得
        val dfDate = SimpleDateFormat("yyyyMMdd")
        val dfHour = SimpleDateFormat("HH")
        val dfMin = SimpleDateFormat("mm")
        val date = dfDate.format(Date())
        val hour = dfHour.format(Date())
        val min = dfMin.format(Date())
//        // 現在日時を表示
//        val output = findViewById<TextView>(R.id.tvGetHomeTime)
//        output.text = date.toString() + time.toString()

        val db = _helper.writableDatabase

        //　現在日時をデータベースに記述
        val sqlInsert = "INSERT INTO GetHomeTimeLogs (getHomeDate, getHomeHour, getHomeMin) VALUES (?, ?, ?)"
        var stmt = db.compileStatement(sqlInsert)
        //　変数のバインド
        stmt.bindString(1, date.toString())
        stmt.bindString(2, hour.toString())
        stmt.bindString(3, min.toString())

        stmt.executeInsert()

        Log.i("test", date)
    }

    // 時間データから経過時間を計算する関数
    fun makeTimeLogs(startTime: String, endTime: String){

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