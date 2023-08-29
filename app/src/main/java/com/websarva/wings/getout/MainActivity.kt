package com.websarva.wings.getout

import android.content.Context
import android.content.ContentValues
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.CalendarView
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {

    //データベースヘルパーオブジェクトを作成
    private val _helper = DatabaseHelper(this@MainActivity)
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val calendarListView = findViewById<ListView>(R.id.calendarListView)

        val startDate = "2023-01-01" // 開始日
        val endDate = "2023-12-31" // 終了日

        val dates = generateDatesInRange(startDate, endDate)
        val adapter = CalendarAdapter(this, dates)

        calendarListView.adapter = adapter

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
        var sql = "SELECT * FROM GetOutTimeLog"
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
            var dataColumnIndex = cursor.getColumnIndex("Date")
            var data = cursor.getString(dataColumnIndex)
            Log.i("DatabaseData", "Data: $data")
            dataColumnIndex = cursor.getColumnIndex("Time")
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


        val sql = "SELECT * FROM GetOutTimeLog "
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
            val sqlDelete = "DELETE FROM GetOutTimeLog"
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
            val sqlInsert = "INSERT INTO GetOutTimeLog (getOutDate, getOutHour, getOutMin) VALUES (?, ?, ?)"
            stmt = db.compileStatement(sqlInsert)
            //　変数のバインド
            stmt.bindString(1, date.toString())
            stmt.bindString(2, hour.toString())
            stmt.bindString(3, min.toString())

            stmt.executeInsert()
        }
    }
    // 帰宅ボタンを押したときの処理
    fun onGetHomeButtonClick(view:View){
        val db = _helper.writableDatabase

        // 外出時刻を所得
        val sql = "SELECT * FROM GetOutTimeLog "
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
            val sqlDelete = "DELETE FROM GetOutTimeLog"
            var stmt = db.compileStatement(sqlDelete)
            stmt.executeUpdateDelete()

            //　nullをデータベースに記述（帰宅したので再び外出できるようにする）
            val sqlInsert = "INSERT INTO GetOutTimeLog (getOutDate, getOutHour, getOutMin) VALUES (null, null, null)"
            stmt = db.compileStatement(sqlInsert)
            stmt.executeInsert()

            // 現在日時を所得
            val dfDate = SimpleDateFormat("yyyy-M-d")
            val dfHour = SimpleDateFormat("HH")
            val dfMin = SimpleDateFormat("mm")
            val getHomeDate = dfDate.format(Date())
            val getHomeHour = dfHour.format(Date())
            val getHomeMin = dfMin.format(Date())

            // 帰宅ボタンの上に所得した現在時刻を表示
            val output = findViewById<TextView>(R.id.tvGetHomeTime)
            output.text = getHomeHour.toString() + getHomeMin.toString()

            // 今日の日付に外出時間を加算
            addTime(getHomeDate, getTimeDeference(getOutHour, getOutMin, getHomeHour, getHomeMin))

//            // TimeSumLogからデータを所得
//            val sql = "SELECT * FROM TimeSumLog WHERE TimeSumDate = ?"
//            val selectionArgs = arrayOf(date)
//            val cursor = db.rawQuery(sql, selectionArgs)
//
//            while(cursor.moveToNext()) {
//                val timeIdxNote = cursor.getColumnIndex("TimeSumTime")
//                val timeData = cursor.getString(timeIdxNote)
//                var timeDataInt = timeData.toInt()
//                val timeDeference = getTimeDeference(getOutHour, getOutMin, hour, min)
//                val timeDeferenceInt = timeDeference.toInt()
//
//                // 外出時間を加算
//                Log.i("TAG", "timeDataInt = $timeDataInt, timeDeferenceInt = $timeDeferenceInt")
//                timeDataInt += timeDeferenceInt
//                Log.i("TAG", "timeDataInt = ${timeDataInt}")
//
//                //　DBを加算したデータで書き換える
//                val sqlUpdate = "UPDATE TimeSumLog SET TimeSumTime = ? WHERE TimeSumDate = ?"
//                stmt = db.compileStatement(sqlUpdate)
//                //　変数のバインド
//                stmt.bindString(1, timeDataInt.toString())
//                stmt.bindString(2, date)
//                stmt.execute()
//            }
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

    // dateの外出時間にtimeを加算する
    fun addTime(date: String, time: String){
        val db = _helper.writableDatabase


        // TimeSumLogからデータを所得
        val sql = "SELECT * FROM TimeSumLog WHERE Date = ?"
        val selectionArgs = arrayOf(date)
        val cursor = db.rawQuery(sql, selectionArgs)

        while(cursor.moveToNext()) {
            val timeIdxNote = cursor.getColumnIndex("Time")
            val timeData = cursor.getString(timeIdxNote)
            var timeDataInt = timeData.toInt()
            val timeInt = time.toInt()
            // 外出時間を加算
            Log.i("TAG", "timeDataInt = $timeDataInt, timeDeferenceInt = $timeInt")
            timeDataInt += timeInt
            Log.i("TAG", "timeDataInt = ${timeDataInt}")

            //　DBを加算したデータで書き換える
            val sqlUpdate = "UPDATE TimeSumLog SET Time = ? WHERE Date = ?"
            var stmt = db.compileStatement(sqlUpdate)
            //　変数のバインド
            stmt.bindString(1, timeDataInt.toString())
            stmt.bindString(2, date)
            stmt.execute()
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
        return timeData
    }

    // ex)　入力：97　→　出力：1時間37分
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

        val outSql = "SELECT * FROM GetOutTimeLog"
        val homeSql = "SELECT * FROM GetHomeTimeLog"
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
            val sql = "SELECT * FROM TimeSumLog WHERE Date = ?"
            val selectionArgs = arrayOf(selectedDate)
            val cursor = db.rawQuery(sql, selectionArgs)

            while(cursor.moveToNext()) {
                var timeIdxNote = cursor.getColumnIndex("Date")
                var timeData = cursor.getString(timeIdxNote)
                Log.i("TAG", "$timeData")
                timeIdxNote = cursor.getColumnIndex("Time")
                timeData = cursor.getString(timeIdxNote)
                Log.i("TAG", "$timeData")
                Toast.makeText(applicationContext, "${minToHour(timeData)}", Toast.LENGTH_LONG).show()
            }
            db.close()
        }
    }


}

private fun generateDatesInRange(startDate: String, endDate: String): List<DateStatus> {
    val datesWithStatus = mutableListOf<DateStatus>()
    val dateFormat = SimpleDateFormat("yyyy-M-d", Locale.getDefault())

    val calendarStart = Calendar.getInstance()
    calendarStart.time = dateFormat.parse(startDate)

    val calendarEnd = Calendar.getInstance()
    calendarEnd.time = dateFormat.parse(endDate)

    val currentDate = calendarStart.clone() as Calendar

    while (currentDate <= calendarEnd) {
        val date = dateFormat.format(currentDate.time)
        val isWeekend = (currentDate.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY) ||
                (currentDate.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY)
        val status = isWeekend


        val dateStatus = DateStatus(date, status)
        datesWithStatus.add(dateStatus)

        currentDate.add(Calendar.DAY_OF_MONTH, 1)
    }

    return datesWithStatus
}
class CalendarAdapter(private val context: Context, private val datesWithStatus: List<DateStatus>) : BaseAdapter() {

    override fun getCount(): Int {
        return datesWithStatus.size
    }

    override fun getItem(position: Int): DateStatus {
        return datesWithStatus[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        var convertView = convertView
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.list_item_calendar, parent, false)
        }

        val dateStatus = getItem(position)
        val dateTextView = convertView!!.findViewById<TextView>(R.id.dateTextView)
        val dateStatusTextView = convertView.findViewById<TextView>(R.id.dateStatusTextView)
        dateTextView.text = dateStatus.date
        dateStatusTextView.text = if (dateStatus.status) "◯" else "×"
        return convertView
    }
}

data class DateStatus(val date: String, val status: Boolean)


