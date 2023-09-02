package com.websarva.wings.getout

import android.annotation.SuppressLint
import android.content.Context
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
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Date
import java.util.Locale


data class DateStatus(val date: String, val status: Boolean, val time: String)


class MainActivity : AppCompatActivity() {

    //データベースヘルパーオブジェクトを作成
    private val _helper = DatabaseHelper(this@MainActivity)

    // 現在日時を所得
    val dfDate = SimpleDateFormat("yyyy-M-d")
    val date = dfDate.format(Date())
    val endDate = "2023-01-01" // 開始日
    val startDate = date // 終了日
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val calendarListView = findViewById<ListView>(R.id.calendarListView)


        val dates = generateDatesInRange(startDate, endDate)
        val adapter = CalendarAdapter(this, dates)

        calendarListView.adapter = adapter

        cheakButton()
//        val btGetOut = findViewById<Button>(R.id.btGetOut)
//        val btGetHome = findViewById<Button>(R.id.btGetHome)
//
//        if(homeOrOut() == 0){//　在宅中ならば
//            // 外出ボタンのみ押せる
//            btGetOut.isEnabled = true
//            btGetHome.isEnabled = false
//        }else{//　外出中ならば
//            // 帰宅ボタンのみ押せる
//            btGetOut.isEnabled = false
//            btGetHome.isEnabled = true
//        }

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

    // 在宅中か否かを知らせる。返り値：０→在宅、１→外出
    fun homeOrOut():Int{
        val db = _helper.writableDatabase

        val sql = "SELECT * FROM GetOutTimeLog "
        val cursor = db.rawQuery(sql, null)
        var returnNum = 0

        while(cursor.moveToNext()) {
            val dateIdxNote = cursor.getColumnIndex("getOutDate")
            val getOutDate = cursor.getString(dateIdxNote)

            // DBに日付データが格納されていたら
            if (getOutDate != null) {
                // 外出中だから１を返す
                returnNum = 1
            }
        }
        return returnNum
    }
    fun onGetOutButtonClick(view: View){ // 外出ボタンを押したときの処理
        val db = _helper.writableDatabase

        // DBに日付データが格納されていたら
        if(homeOrOut()==1){
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
        // 押せるボタンの確認
        cheakButton()
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

            // 外出時刻のデータが格納されていなければ（nullか否かで判定します。）
            if (getOutDate == null) {
//                Log.i("TAG", "nullCheck")
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

            addDateTime(getOutDate,getOutHour,getOutMin,getHomeDate,getHomeHour,getHomeMin)

            generateDatesInRange(startDate, endDate)
        }
        db.close()
        cheakButton()
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
            timeDataInt += timeInt

            //　DBを加算したデータで書き換える
            val sqlUpdate = "UPDATE TimeSumLog SET Time = ? WHERE Date = ?"
            var stmt = db.compileStatement(sqlUpdate)
            //　変数のバインド
            stmt.bindString(1, timeDataInt.toString())
            stmt.bindString(2, date)
            stmt.execute()
        }
    }

    fun addDateTime(startDate: String,startHour: String,startMin: String,endDate: String,endHour: String,endMin: String){
        if (startDate == endDate){
            // 今日の日付に外出時間を加算
            addTime(startDate, getTimeDeference(startHour, startMin, endHour, endMin))
        }
        else{
            // 日付のフォーマットを指定
            val dfDate = DateTimeFormatter.ofPattern("yyyy-M-d")
            var nextDate = LocalDate.parse(startDate, dfDate)
            nextDate = nextDate.plusDays(1)

            // 入力を二つに分割する
            addDateTime(startDate,startHour,startMin,startDate,"24","0")
            addDateTime(nextDate.format(dfDate),"0","0",endDate,endHour,endMin)
        }
    }

    // YYYY-M-d形式の日付から総外出時間を所得する
    fun getTime(date: String): String{
        val datesWithStatus = mutableListOf<DateStatus>()
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
        // 外出時間と表示用のステータスを取得してリストに追加
        val isWeekend = false // ここでは週末の判定を行わないため false としています
        val dateStatus = DateStatus(date, isWeekend, minToHour(timeData))
        datesWithStatus.add(dateStatus)

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

    // 押せるボタンの確認
    fun cheakButton(){
        val btGetOut = findViewById<Button>(R.id.btGetOut)
        val btGetHome = findViewById<Button>(R.id.btGetHome)

        if(homeOrOut() == 0){//　在宅中ならば
            // 外出ボタンのみ押せる
            btGetOut.isEnabled = true
            btGetHome.isEnabled = false
        }else{//　外出中ならば
            // 帰宅ボタンのみ押せる
            btGetOut.isEnabled = false
            btGetHome.isEnabled = true
        }
    }

    // CalendarViewで日にちが選択された時に呼び出されるリスナークラス
    private inner class DateChangeListener : CalendarView.OnDateChangeListener {
        override fun onSelectedDayChange(calendarView: CalendarView, year: Int, month: Int, dayOfMonth: Int) {

            // monthは0起算のため+1します。
            val displayMonth = month + 1
            // DBの日付検索用文字列を作成
            var selectedDate = "$year-$displayMonth-$dayOfMonth"
//            Log.i("TAG", "$selectedDate")

            val db = _helper.writableDatabase

            // TimeSumLogからデータを所得
            val sql = "SELECT * FROM TimeSumLog WHERE Date = ?"
            val selectionArgs = arrayOf(selectedDate)
            val cursor = db.rawQuery(sql, selectionArgs)

            while(cursor.moveToNext()) {
                var timeIdxNote = cursor.getColumnIndex("Date")
                var timeData = cursor.getString(timeIdxNote)
//                Log.i("TAG", "$timeData")
                timeIdxNote = cursor.getColumnIndex("Time")
                timeData = cursor.getString(timeIdxNote)
//                Log.i("TAG", "$timeData")
                Toast.makeText(applicationContext, "${minToHour(timeData)}", Toast.LENGTH_LONG).show()
            }
            db.close()
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

        while (currentDate >= calendarEnd) {
            val date = dateFormat.format(currentDate.time)
            val isWeekend = (currentDate.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY) ||
                    (currentDate.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY)
            val status = isWeekend

            // ここで外出時間を計算し、分から時間に変換
            val timeData = calculateTime(date)
            val timeInHours = minToHour(timeData)

            val dateStatus = DateStatus(date, status, timeInHours)
            datesWithStatus.add(dateStatus)

            currentDate.add(Calendar.DAY_OF_MONTH, -1)
        }

        return datesWithStatus
    }

    // 日付から外出時間を計算する関数を再実装
    private fun calculateTime(date: String): String {
        val db = _helper.writableDatabase

        val sql = "SELECT * FROM TimeSumLog WHERE Date = ?"
        val selectionArgs = arrayOf(date)
        val cursor = db.rawQuery(sql, selectionArgs)

        var timeData = ""
        while (cursor.moveToNext()) {
            val timeIdxNote = cursor.getColumnIndex("Time")
            timeData = cursor.getString(timeIdxNote)
        }
        db.close()

        return timeData
    }


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
        val timeTextView = convertView.findViewById<TextView>(R.id.timeTextView) // 追加

        dateTextView.text = dateStatus.date
        dateStatusTextView.text = if (dateStatus.status) "◯" else "×"
        timeTextView.text = dateStatus.time // 追加

        return convertView
    }
}



