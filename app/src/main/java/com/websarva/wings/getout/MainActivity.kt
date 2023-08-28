package com.websarva.wings.getout

import android.content.ContentValues
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
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar

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

        // 外出の方
        var sql = "SELECT * FROM GetOutTimeLogs"
        var cursor = db.rawQuery(sql,null)

        var log =""
        while (cursor.moveToNext()) {
            val idxNote = cursor.getColumnIndex("getOutMin")
            // 所得したデータをlogに追加
            log += cursor.getString(idxNote)
            log += " "
        }
        Log.i("count", "GetOut=" + log)
        // tvGetOutTimeにlogのテキストを設定
        val GetOutOutput = findViewById<TextView>(R.id.tvGetOutTime)
        GetOutOutput.text = log


        cursor = db.rawQuery("SELECT * FROM TimeSumLog", null)

        while (cursor.moveToNext()) {
            var dataColumnIndex = cursor.getColumnIndex("TimeSumDate")
            var data = cursor.getString(dataColumnIndex)
            Log.i("DatabaseData", "Data: $data")
            dataColumnIndex = cursor.getColumnIndex("TimeSumTime")
            data = cursor.getString(dataColumnIndex)
            Log.i("DatabaseData", "Data: $data")
        }

//        // 帰宅の方
//        sql = "SELECT * FROM GetHomeTimeLogs"
//        cursor = db.rawQuery(sql,null)
//
//        log =""
//        count = 0 // index数を数える
//        while (cursor.moveToNext()) {
//            val idxNote = cursor.getColumnIndex("getHomeMin")
//            //　所得したデータをlogに追加
//            log += cursor.getString(idxNote)
//            log += " "
////            output.text = log
//            count++
//        }
//        Log.i("count", "GetHomeCount = " + count)
//        // tvGetHomeTimeにlogのテキストを反映させる。
//        val GetHomeOutput = findViewById<TextView>(R.id.tvGetHomeTime)
//        GetHomeOutput.text = log
//        makeTimeLogs()

        db.close()
    }
    fun onGetOutButtonClick(view: View){ // 外出ボタンを押したときの処理
        val db = _helper.writableDatabase


        val sql = "SELECT * FROM GetOutTimeLogs "
        val cursor = db.rawQuery(sql, null)

        while(cursor.moveToNext()) {
            val dateIdxNote = cursor.getColumnIndex("getOutDate")
            val hourIdxNote = cursor.getColumnIndex("getOutHour")
            val minIdxNote = cursor.getColumnIndex("getOutMin")
            val getOutDate = cursor.getString(dateIdxNote)
            val getOutHour = cursor.getString(hourIdxNote)
            val getOutMin = cursor.getString(minIdxNote)

            Log.i("TAG", "${getOutDate},${getOutHour},${getOutMin}")

            // DBに日付データが格納されていたら
            if(getOutDate != null){
                Log.i("TAG", "nullCheck")
                // 関数を終了する
                db.close()
                return
            }
            // nullの要素を削除する～♪
            val sqlDelete = "DELETE FROM GetOutTimeLogs"
            var stmt = db.compileStatement(sqlDelete)
            stmt.executeUpdateDelete()


            // 現在日時を所得
            val dfDate = SimpleDateFormat("yyyy-M-d")
            val dfHour = SimpleDateFormat("HH")
            val dfMin = SimpleDateFormat("mm")
            val date = dfDate.format(Date())
            val hour = dfHour.format(Date())
            val min = dfMin.format(Date())

            //　現在日時をデータベースに記述
            val sqlInsert = "INSERT INTO GetOutTimeLogs (getOutDate, getOutHour, getOutMin) VALUES (?, ?, ?)"
            stmt = db.compileStatement(sqlInsert)
            //　変数のバインド
            stmt.bindString(1, date.toString())
            stmt.bindString(2, hour.toString())
            stmt.bindString(3, min.toString())

            stmt.executeInsert()
        }
//
//        // 現在日時を所得
//        val dfDate = SimpleDateFormat("yyyyMMdd")
//        val dfHour = SimpleDateFormat("HH")
//        val dfMin = SimpleDateFormat("mm")
//        val date = dfDate.format(Date())
//        val hour = dfHour.format(Date())
//        val min = dfMin.format(Date())
////        val output = findViewById<TextView>(R.id.tvGetOutTime)
////        output.text = date.toString()+time.toString()
//
//
//        //　現在日時をデータベースに記述
//        val sqlInsert = "INSERT INTO GetOutTimeLogs (getOutDate, getOutHour, getOutMin) VALUES (?, ?, ?)"
//        var stmt = db.compileStatement(sqlInsert)
//        //　変数のバインド
//        stmt.bindString(1, date.toString())
//        stmt.bindString(2, hour.toString())
//        stmt.bindString(3, min.toString())
//
//        stmt.executeInsert()

    }
    // 帰宅ボタンを押したときの処理
    fun onGetHomeButtonClick(view:View){
        val db = _helper.writableDatabase

        // 外出時刻を所得
        val sql = "SELECT * FROM GetOutTimeLogs "
        val cursor = db.rawQuery(sql, null)

        //　GetOutTimeLogは一行しかないからmoveToFirstを使用
        if(cursor.moveToFirst()) {
            // それぞれの要素を所得
            val dateIdxNote = cursor.getColumnIndex("getOutDate")
            val hourIdxNote = cursor.getColumnIndex("getOutHour")
            val minIdxNote = cursor.getColumnIndex("getOutMin")
            val getOutDate = cursor.getString(dateIdxNote)
            val getOutHour = cursor.getString(hourIdxNote)
            val getOutMin = cursor.getString(minIdxNote)

            Log.i("getHome", "${getOutDate},${getOutHour},${getOutMin}")

            // 外出時刻のデータが格納されていなければ（nullか否かで判定します。）
            if (getOutDate == null) {
                Log.i("TAG", "nullCheck")
                // 関数を終了する
                db.close()
                return
            }

            // DBに外出時刻データが格納されていれば日付を削除する～♪
            val sqlDelete = "DELETE FROM GetOutTimeLogs"
            var stmt = db.compileStatement(sqlDelete)
            stmt.executeUpdateDelete()

            //　nullをデータベースに記述（帰宅したので再び外出できるようにする）
            val sqlInsert = "INSERT INTO GetOutTimeLogs (getOutDate, getOutHour, getOutMin) VALUES (null, null, null)"
            stmt = db.compileStatement(sqlInsert)
            stmt.executeInsert()

            // 現在日時を所得
            val dfDate = SimpleDateFormat("yyyy-M-d")
            val dfHour = SimpleDateFormat("HH")
            val dfMin = SimpleDateFormat("mm")
            val date = dfDate.format(Date())
            val hour = dfHour.format(Date())
            val min = dfMin.format(Date())

            // 帰宅ボタンの上に所得した現在時刻を表示（不要）
            val output = findViewById<TextView>(R.id.tvGetHomeTime)
            output.text = hour.toString()+min.toString()

//            // TimeSumLogからデータを所得
//            val sql = "SELECT * FROM TimeSumLog "
//            val cursor = db.rawQuery(sql, null)

            // TimeSumLogからデータを所得
            val sql = "SELECT * FROM TimeSumLog WHERE TimeSumDate = ?"
            val selectionArgs = arrayOf(date)
            val cursor = db.rawQuery(sql, selectionArgs)

            while(cursor.moveToNext()) {
                val timeIdxNote = cursor.getColumnIndex("TimeSumTime")
                val timeData = cursor.getString(timeIdxNote)
                var timeDataInt = timeData.toInt()
                val timeDeference = getTimeDeference(getOutHour, getOutMin, hour, min)
                val timeDeferenceInt = timeDeference.toInt()

                // 外出時間を加算
                Log.i("TAG", "timeDataInt = $timeDataInt, timeDeferenceInt = $timeDeferenceInt")
                timeDataInt += timeDeferenceInt
                Log.i("TAG", "timeDataInt = ${timeDataInt}")

                //　データベースに記述
                val sqlUpdate = "UPDATE TimeSumLog SET TimeSumTime = ? WHERE TimeSumDate = ?"
                stmt = db.compileStatement(sqlUpdate)
                //　変数のバインド
                stmt.bindString(1, timeDataInt.toString())
                stmt.bindString(2, date)
                stmt.execute()


            }
        }
        db.close()
    }

    // 時間データから経過時間を計算する関数、返り値は分
    fun getTimeDeference(startHour: String, startMin: String, endHour: String, endMin: String): String{
        // StringをIntに変換
        val startHourInt = startHour.toInt()
        val startMinInt = startMin.toInt()
        val endHourInt = endHour.toInt()
        val endMinInt = endMin.toInt()

        //　始まりと終わりを分で表現し、差を計算
        val startTime = startHourInt * 60 + startMinInt
        val endTime = endHourInt * 60 + endMinInt
        val timeDeference = endTime - startTime

        //　返り値の単位は分
        return timeDeference.toString()
    }
    // 分を時間と分に変換する関数
    fun minToHour(min: String): String {
        // StringをIntに変換
        val minInt = min.toInt()
        val outputHour = minInt / 60
        val outputMin = minInt % 60
        val output = "${outputHour}時間${outputMin}分"
        return output
    }
    // 外出帰宅時間からその日の総外出時間をDBに格納。外出、帰宅DBの要素数がそろっているときに実行してね♪
    // 日付またぎ未実装
    fun makeTimeLogs(){

        val db = _helper.writableDatabase

        val outSql = "SELECT * FROM GetOutTimeLogs"
        val homeSql = "SELECT * FROM GetHomeTimeLogs"
        val outCursor = db.rawQuery(outSql,null)
        val homeCursor = db.rawQuery(homeSql,null)

        val outDateLog = mutableListOf<String>()
        val outHourLog = mutableListOf<String>()
        val outMinLog = mutableListOf<String>()
        val homeDateLog = mutableListOf<String>()
        val homeHourLog = mutableListOf<String>()
        val homeMinLog = mutableListOf<String>()

        while (outCursor.moveToNext()) {
            // getOutDateとgetOutHourとgetOutMinの添え字を所得
            val dateIdxNote = outCursor.getColumnIndex("getOutDate")
            val hourIdxNote = outCursor.getColumnIndex("getOutHour")
            val minIdxNote = outCursor.getColumnIndex("getOutMin")
            // 添え字を参考に所得したデータをlogに追加
            outDateLog.add(outCursor.getString(dateIdxNote))
            outHourLog.add(outCursor.getString(hourIdxNote))
            outMinLog.add(outCursor.getString(minIdxNote))
        }
        while (homeCursor.moveToNext()) {
            // getHomeDateとgetHomeHourとgetHomeMinの添え字を所得
            val dateIdxNote = homeCursor.getColumnIndex("getHomeDate")
            val hourIdxNote = homeCursor.getColumnIndex("getHomeHour")
            val minIdxNote = homeCursor.getColumnIndex("getHomeMin")
            // 添え字を参考に所得したデータをlogに追加
            homeDateLog.add(homeCursor.getString(dateIdxNote))
            homeHourLog.add(homeCursor.getString(hourIdxNote))
            homeMinLog.add(homeCursor.getString(minIdxNote))
        }
        // 各要素にアクセスし、結果をDBに格納
        for(i in 0..homeDateLog.size - 1){ //　添え字は0からなので-1している。
            //　外出時刻と帰宅時刻から、外にいた時間を計算
            val addTime = getTimeDeference(outHourLog[i], outMinLog[i], homeHourLog[i], homeMinLog[i])
            Log.i("log", "${addTime}")
        }
    }
    // CalendarViewで日にちが選択された時に呼び出されるリスナークラス
    private inner class DateChangeListener : CalendarView.OnDateChangeListener {
        override fun onSelectedDayChange(calendarView: CalendarView, year: Int, month: Int, dayOfMonth: Int) {

            // monthは0起算のため+1します。
            val displayMonth = month + 1
            // DBの日付検索用文字列を作成
            var selectedDate = "$year-$displayMonth-$dayOfMonth"
            Log.i("TAG", "$selectedDate")

            val db = _helper.writableDatabase

            // TimeSumLogからデータを所得
            val sql = "SELECT * FROM TimeSumLog WHERE TimeSumDate = ?"
            val selectionArgs = arrayOf(selectedDate)
            val cursor = db.rawQuery(sql, selectionArgs)

            while(cursor.moveToNext()) {
                var timeIdxNote = cursor.getColumnIndex("TimeSumDate")
                var timeData = cursor.getString(timeIdxNote)
                Log.i("TAG", "$timeData")
                timeIdxNote = cursor.getColumnIndex("TimeSumTime")
                timeData = cursor.getString(timeIdxNote)
                Log.i("TAG", "$timeData")
                Toast.makeText(applicationContext, "${minToHour(timeData)}", Toast.LENGTH_LONG).show()
            }
            db.close()
        }
    }


}