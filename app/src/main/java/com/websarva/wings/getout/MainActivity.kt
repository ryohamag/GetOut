package com.websarva.wings.getout

import android.annotation.SuppressLint
import android.app.Activity
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
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.Random

data class DateStatus(val date: String, val status: Boolean, val dayOfWeek: String, val time: String) {    val formattedDate: String
        get() {
            return date.replaceFirst("^\\d{0}-".toRegex(), "")
        }
    }
class MainActivity : AppCompatActivity() {

    //データベースヘルパーオブジェクトを作成
    private val _helper = DatabaseHelper(this@MainActivity)
    // カレンダーリストを作成
    val datesWithStatus = mutableListOf<DateStatus>()

    // 現在日時を所得
    val dfDate = SimpleDateFormat("yyyy-M-d")
    val date = dfDate.format(Date())
    val startDate = date // 開始日
    val endDate = "2022-1-1" // 終了日

    var settingChangeFlag = 0

    lateinit var adapter :CalendarAdapter
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val calendarListView = findViewById<ListView>(R.id.calendarListView)
        generateDatesInRange(startDate, endDate)
        adapter = CalendarAdapter(this, datesWithStatus)
        calendarListView.adapter = adapter

        cheakButton()
        cheakInformation()

//        val tvOutTime = findViewById<TextView>(R.id.tvOutTime)
//        tvOutTime.text = "今日の外出時間：${minToHour(getTime(date))}"
        reloadOutTime()


//        val db = _helper.writableDatabase
//
//        Log.i("TAG", "o")
//        val sqlI = "INSERT INTO GoalTimeLog (num, GoalTimeMin) VALUES (?, ?)"
//        Log.i("TAG", "on")
//        val stm = db.compileStatement(sqlI)
//        //　変数のバインド
//        Log.i("TAG", "onCreate")
//        stm.bindString(1, "2")
//        stm.bindString(2, "333")
//        Log.i("TAG", "onCreate: ")
//
//        stm.executeInsert()
        val btNotification = findViewById<Button>(R.id.btNotification)
        //ボタンクリックのリスナーを設定。
        btNotification.setOnClickListener {
            //インテントオブジェクトを生成。
            val intent2Notification = Intent(this@MainActivity, NotificationActivity::class.java)
            intent2Notification.putExtra("settingChangeFlag",settingChangeFlag)
            // 設定画面の起動。
            startActivityForResult(intent2Notification,1000)
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

    override fun onResume() {
        super.onResume()
        val tvGoalTime = findViewById<TextView>(R.id.tvGoalTime)
        tvGoalTime.text = "目標外出時間：${minToHour(getGoalTime())}"
        reloadOutTime()
        cheakInformation()
        if (settingChangeFlag == 1){
            listUpdate()
            adapter.notifyDataSetChanged() // アダプターに変更を通知
        }
//        listUpdate()
//        adapter.getView()
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
    fun onGetOutButtonClick(view: View) {
        val db = _helper.writableDatabase

        // DBに日付データが格納されていたら
        if (homeOrOut() == 1) {
            // 関数を終了する
            db.close()
            return
        }

        // 確認ダイアログを表示
        showConfirmationDialog()
    }

    // 確認ダイアログを表示する関数
    private fun showConfirmationDialog() {
        val db = _helper.writableDatabase
        val alertDialogBuilder = AlertDialog.Builder(this)
        alertDialogBuilder.setTitle("外出確認")
        alertDialogBuilder.setMessage("外出中になりますが、よろしいですか？")

        alertDialogBuilder.setPositiveButton("OK") { _, _ ->
            // ダイアログのOKボタンがクリックされた場合の処理
            proceedWithGetOut()
        }

        alertDialogBuilder.setNegativeButton("キャンセル") { _, _ ->
            // ダイアログのキャンセルボタンがクリックされた場合の処理
            // 何もせずにダイアログを閉じる
            db.close()
        }

        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }

    // 実際の外出処理を行う関数
    private fun proceedWithGetOut() {
        val db = _helper.writableDatabase
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

        // 現在日時をデータベースに記述
        val sqlInsert = "INSERT INTO GetOutTimeLog (getOutDate, getOutHour, getOutMin) VALUES (?, ?, ?)"
        stmt = db.compileStatement(sqlInsert)
        // 変数のバインド
        stmt.bindString(1, date.toString())
        stmt.bindString(2, hour.toString())
        stmt.bindString(3, min.toString())

        stmt.executeInsert()
        // 押せるボタンの確認
        cheakButton()
        cheakInformation()
    }
    // 帰宅ボタンを押したときの処理
    fun onGetHomeButtonClick(view: View) {
        val db = _helper.writableDatabase

        // 外出時刻を所得
        val sql = "SELECT * FROM GetOutTimeLog "
        val cursor = db.rawQuery(sql, null)

        // GetOutTimeLogは一行しかないからmoveToFirstを使用
        if (cursor.moveToFirst()) {
            // それぞれの要素を所得
            val dateIdxNote = cursor.getColumnIndex("getOutDate")
            val hourIdxNote = cursor.getColumnIndex("getOutHour")
            val minIdxNote = cursor.getColumnIndex("getOutMin")
            val getOutDate = cursor.getString(dateIdxNote)
            val getOutHour = cursor.getString(hourIdxNote)
            val getOutMin = cursor.getString(minIdxNote)

            // 外出時刻のデータが格納されていなければ（nullか否かで判定します。）
            if (getOutDate == null) {
                // 関数を終了する
                db.close()
                return
            }

            // 確認ダイアログを表示
            showReturnHomeConfirmationDialog(getOutDate, getOutHour, getOutMin)
        }
    }

    // 帰宅確認ダイアログを表示する関数
    private fun showReturnHomeConfirmationDialog(getOutDate: String, getOutHour: String, getOutMin: String) {
        val db = _helper.writableDatabase
        val alertDialogBuilder = AlertDialog.Builder(this)
        alertDialogBuilder.setTitle("帰宅確認")
        alertDialogBuilder.setMessage("帰宅を完了し、外出データを記録しますか？")

        alertDialogBuilder.setPositiveButton("OK") { _, _ ->
            // ダイアログのOKボタンがクリックされた場合の処理
            proceedWithReturnHome(getOutDate, getOutHour, getOutMin)
        }

        alertDialogBuilder.setNegativeButton("キャンセル") { _, _ ->
            // ダイアログのキャンセルボタンがクリックされた場合の処理
            // 何もせずにダイアログを閉じる
            db.close()
        }

        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }

    // 実際の帰宅処理を行う関数
    private fun proceedWithReturnHome(getOutDate: String, getOutHour: String, getOutMin: String) {
        val db = _helper.writableDatabase
        // 外出時刻のデータが格納されていれば日付を削除する～♪
        val sqlDelete = "DELETE FROM GetOutTimeLog"
        var stmt = db.compileStatement(sqlDelete)
        stmt.executeUpdateDelete()

        // nullをデータベースに記述（帰宅したので再び外出できるようにする）
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

        addDateTime(getOutDate, getOutHour, getOutMin, getHomeDate, getHomeHour, getHomeMin)

//        generateDatesInRange(startDate, endDate)

        db.close()
        cheakButton()
//        cheakInformation()
        getHomeMessage(getTimeDeference(getOutHour, getOutMin, getHomeHour, getHomeMin))
        reloadOutTime()
        listUpdate()
        adapter.notifyDataSetChanged() // アダプターに変更を通知
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

        return timeData
    }

    // YYYY-M形式の日付から月の累計外出時間を所得する
    fun getMonthTime(date: String): String{

        val startDate = "$date-1"
        val dateFormat = SimpleDateFormat("yyyy-M-d", Locale.getDefault())
        val calendarStart = Calendar.getInstance()
        calendarStart.time = dateFormat.parse(startDate)


        val calendarEnd = Calendar.getInstance()
        calendarEnd.time = dateFormat.parse(startDate)
        // Calendarを次の月の一日に設定
        calendarEnd.add(Calendar.MONTH, 1)
        calendarEnd.set(Calendar.DAY_OF_MONTH, 1)

        val currentDate = calendarStart.clone() as Calendar
        var timeSum = 0

        while (currentDate.timeInMillis < calendarEnd.timeInMillis) {
            val date = dateFormat.format(currentDate.time)
            val timeInt = getTime(date).toInt()

            timeSum += timeInt

            currentDate.add(Calendar.DAY_OF_MONTH, 1)
        }
        return timeSum.toString()
    }

    // 目標外出時間を所得する
    fun getGoalTime(): String{
        val datesWithStatus = mutableListOf<DateStatus>()
        val db = _helper.writableDatabase

        // TimeSumLogからデータを所得
        val sql = "SELECT * FROM GoalTimeLog"
        val cursor = db.rawQuery(sql,null)

        var timeData = ""
        while(cursor.moveToNext()) {
            val timeIdxNote = cursor.getColumnIndex("GoalTimeMin")
            timeData = cursor.getString(timeIdxNote)
        }
        db.close()

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

    //tvOutTimeを更新
    fun reloadOutTime(){

        val dfDate = SimpleDateFormat("yyyy-M-d")
        val date = dfDate.format(Date())

        val tvOutTime = findViewById<TextView>(R.id.tvOutTime)
        tvOutTime.text = "今日の外出時間：${minToHour(getTime(date))}"

    }

    // tvInformationの帰宅Ver
    fun getHomeMessage(time: String) {
        val messages = listOf(
            "お帰りなさい！おつかれさまです！",
            "ご飯にする？お風呂にする？\nそれとも、お・で・か・け？",
            // 他にもメッセージを追加できます
        )

        // ランダムなインデックスを生成
        val random = Random()
        val randomIndex = random.nextInt(messages.size)

        // ランダムに選択されたメッセージを取得
        val randomMessage = messages[randomIndex]

        // AndroidアプリのUIにランダムなメッセージを表示する
        val tvInformation = findViewById<TextView>(R.id.tvInformation)
        tvInformation.text = randomMessage
        val tvTimeLog = findViewById<TextView>(R.id.tvTimeLog)
        tvTimeLog.text = "今回の外出時間：${minToHour(time)}"
    }

    //tvInformationの内容を変更する。
    fun cheakInformation(){
        val tvInformation = findViewById<TextView>(R.id.tvInformation)
        val tvTimeLog = findViewById<TextView>(R.id.tvTimeLog)
        tvTimeLog.text = ""

        if(homeOrOut() == 0){//　在宅中ならば
            tvInformation.text = "お出かけしませんか？"
        }else{//　外出中ならば
            val db = _helper.writableDatabase

            // 外出時刻を所得
            val sql = "SELECT * FROM GetOutTimeLog "
            val cursor = db.rawQuery(sql, null)

            // GetOutTimeLogは一行しかないからmoveToFirstを使用
            if (cursor.moveToFirst()) {
                // それぞれの要素を所得
//                val dateIdxNote = cursor.getColumnIndex("getOutDate")
                val hourIdxNote = cursor.getColumnIndex("getOutHour")
                val minIdxNote = cursor.getColumnIndex("getOutMin")
//                val getOutDate = cursor.getString(dateIdxNote)
                val getOutHour = cursor.getString(hourIdxNote)
                val getOutMin = cursor.getString(minIdxNote)

                tvInformation.text = "${getOutHour}時${getOutMin}分より外出中"
            }
        }
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 1000 && resultCode == Activity.RESULT_OK) {
            // NotificationActivityからの返り値を受け取る
            settingChangeFlag = data?.getIntExtra("settingChangeFlag", 0) ?: 0

        }
    }

    // CalendarViewで日にちが選択された時に呼び出されるリスナークラス
    private inner class DateChangeListener : CalendarView.OnDateChangeListener {
        override fun onSelectedDayChange(calendarView: CalendarView, year: Int, month: Int, dayOfMonth: Int) {

            // monthは0起算のため+1します。
            val displayMonth = month + 1
            // DBの日付検索用文字列を作成
            var selectedDate = "$displayMonth/$dayOfMonth"
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

    // カレンダーリストの中身を更新
    fun listUpdate(){
        Log.i("list", "$startDate")
        Log.i("list", "$endDate")
        val dateFormat = SimpleDateFormat("yyyy-M-d", Locale.getDefault())

        val calendarStart = Calendar.getInstance()
        calendarStart.time = dateFormat.parse(startDate)

        val calendarEnd = Calendar.getInstance()
        calendarEnd.time = dateFormat.parse(endDate)

        val currentDate = calendarStart.clone() as Calendar

        while (currentDate.timeInMillis >= calendarEnd.timeInMillis) {
            val date = dateFormat.format(currentDate.time)
            val dayOfWeek = when (currentDate.get(Calendar.DAY_OF_WEEK)) {
                Calendar.SUNDAY -> "日"
                Calendar.MONDAY -> "月"
                Calendar.TUESDAY -> "火"
                Calendar.WEDNESDAY -> "水"
                Calendar.THURSDAY -> "木"
                Calendar.FRIDAY -> "金"
                Calendar.SATURDAY -> "土"
                else -> ""
            }
            val timeInt = getTime(date).toInt()
            val goalInt = getGoalTime().toInt()
            val status = timeInt >= goalInt


            // ここで外出時間を計算し、分から時間に変換
            val timeData = getTime(date)
            val timeInHours = minToHour(timeData)

            val dateStatus = DateStatus(date, status, dayOfWeek, timeInHours)
            // リスト内の要素を日付で検索
            val index = datesWithStatus.indexOfFirst { it.date == date }

            if (index != -1) {
                // 日付が見つかった場合、該当する要素を更新
                datesWithStatus[index] = dateStatus
//                datesWithStatus[index].timeInHours = newTimeInHours
                Log.i("dateStatus", "$dateStatus")
            }

            currentDate.add(Calendar.DAY_OF_MONTH, -1)
        }


    }



    private fun generateDatesInRange(startDate: String, endDate: String) {
//        val datesWithStatus = mutableListOf<DateStatus>()
        val dateFormat = SimpleDateFormat("yyyy-M-d", Locale.getDefault())

        val calendarStart = Calendar.getInstance()
        calendarStart.time = dateFormat.parse(startDate)

        val calendarEnd = Calendar.getInstance()
        calendarEnd.time = dateFormat.parse(endDate)

        val currentDate = calendarStart.clone() as Calendar

        while (currentDate.timeInMillis >= calendarEnd.timeInMillis) {
            val date = dateFormat.format(currentDate.time)
            val dayOfWeek = when (currentDate.get(Calendar.DAY_OF_WEEK)) {
                Calendar.SUNDAY -> "日"
                Calendar.MONDAY -> "月"
                Calendar.TUESDAY -> "火"
                Calendar.WEDNESDAY -> "水"
                Calendar.THURSDAY -> "木"
                Calendar.FRIDAY -> "金"
                Calendar.SATURDAY -> "土"
                else -> ""
            }
//            val isWeekend = (currentDate.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY) ||
//                    (currentDate.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY)
            val timeInt = getTime(date).toInt()
            val goalInt = getGoalTime().toInt()
            val status = timeInt >= goalInt


            // ここで外出時間を計算し、分から時間に変換
            val timeData = getTime(date)
            val timeInHours = minToHour(timeData)

            val dateStatus = DateStatus(date, status, dayOfWeek, timeInHours)
            datesWithStatus.add(dateStatus)

            currentDate.add(Calendar.DAY_OF_MONTH, -1)
        }

//        return datesWithStatus
    }

    private fun calculateTime(date: String): String {
        val db = _helper.writableDatabase

        val sql = "SELECT * FROM TimeSumLog WHERE Date = ?"
        val selectionArgs = arrayOf(date)
        val cursor = db.rawQuery(sql, selectionArgs)

        var timeData = ""
        while (cursor.moveToNext()) {
            val timeIdxNote = cursor.getColumnIndex("Time")
            timeData = cursor.getString(timeIdxNote)

            // デバッグ用ログ出力
            Log.d("CalculateTime", "Date: $date, TimeData: $timeData")
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
        dateTextView.text = dateStatus.formattedDate // formattedDateを使用する
        dateStatusTextView.text = if (dateStatus.status) "◯" else "×"
        timeTextView.text = dateStatus.time // 追加
        val dateDate = convertView!!.findViewById<TextView>(R.id.dateDate)
        dateDate.text = dateStatus.dayOfWeek


        return convertView
    }
//    fun setTime(position: Int, newTime: String) {
//        if (position >= 0 && position < datesWithStatus.size) {
//            datesWithStatus[position] = datesWithStatus[position].copy(time = newTime)
//            notifyDataSetChanged() // データセットの変更を通知
//        }
//    }
}



