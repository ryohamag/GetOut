package com.websarva.wings.getout

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.Date

class NotificationActivity : AppCompatActivity() {
    //データベースヘルパーオブジェクトを作成
    private val _helper = DatabaseHelper(this@NotificationActivity)
    private lateinit var numberInput1: EditText
    private lateinit var numberInput2: EditText
    private lateinit var resultText: TextView

    private lateinit var editTimeYear: EditText
    private lateinit var editTimeMonth: EditText
    private lateinit var editTimeDay: EditText
    private lateinit var editTimeHour:EditText
    private lateinit var editTimeMin: EditText
    private lateinit var editButton: Button
    var settingChangeFlag = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        val db = _helper.writableDatabase
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notification)

        numberInput1 = findViewById(R.id.numberInput1)
        numberInput2 = findViewById(R.id.numberInput2)
        resultText = findViewById(R.id.tvNowGoalTime)

        editTimeYear = findViewById(R.id.editTimeYear)
        editTimeMonth = findViewById(R.id.editTimeMonth)
        editTimeDay = findViewById(R.id.editTimeDay)
        editTimeHour = findViewById(R.id.editTimeHour)
        editTimeMin = findViewById(R.id.editTimeMin)
        editButton = findViewById(R.id.btEditTime)

        reloadGoalTime()

        editButton.setOnClickListener{
            if(editTimeYear.text.isNotEmpty() && editTimeMonth.text.isNotEmpty() && editTimeDay.text.isNotEmpty()
                && editTimeMin.text.isNotEmpty() && editTimeMin.text.isNotEmpty()){

                val hours = editTimeHour.text.toString().toIntOrNull() ?:0
                val minutes = editTimeMin.text.toString().toIntOrNull() ?:0
                val time = hours * 60 + minutes

                val year = editTimeYear.text
                val month = editTimeMonth.text
                val day = editTimeDay.text
                val date = "$year-$month-$day"

                //　DBを編集したデータで書き換える
                val sqlUpdate = "UPDATE TimeSumLog SET Time = ? WHERE Date = ?"
                var stmt = db.compileStatement(sqlUpdate)
                //　変数のバインド
                stmt.bindString(1, time.toString())
                stmt.bindString(2, date)
                stmt.execute()

                // NotificationActivity内でIntentから値を取得
                val intent = intent
                var settingChangeFlag = intent.getIntExtra("settingChangeFlag", 0)

                settingChangeFlag = 1

                // settingChangeFlagの値をMainActivityに返す
                val returnIntent = Intent()
                returnIntent.putExtra("settingChangeFlag", settingChangeFlag)
                setResult(Activity.RESULT_OK, returnIntent)

                // トーストを表示
                Toast.makeText(this, "外出時間が更新されました", Toast.LENGTH_SHORT).show()

                finish()
            }
        }

        val button: Button = findViewById(R.id.completionbutton)
        button.setOnClickListener {
//            // 記入欄がどちらも埋まっていれば
            if (numberInput1.text.isNotEmpty() && numberInput2.text.isNotEmpty()) {
                // 既存の要素を削除する～♪
                val sqlDelete = "DELETE FROM GoalTimeLog"
                var stmt = db.compileStatement(sqlDelete)
                stmt.executeUpdateDelete()

                // 目標時間（分）をgoalMinに代入
                val goalMin = addNumbers()

                //　目標時間をDBに記述
                val sqlInsert = "INSERT INTO GoalTimeLog (GoalTimeMin) VALUES (?)"
                stmt = db.compileStatement(sqlInsert)
                //　変数のバインド
                stmt.bindString(1, goalMin.toString())

                stmt.executeInsert()

                // トーストを表示
                Toast.makeText(this, "目標時間が設定されました", Toast.LENGTH_SHORT).show()
                reloadGoalTime()
            }

        }
    }

    private fun addNumbers():Int {
//        val hoursText = numberInput1.text.toString()
//        val minutesText = numberInput2.text.toString()
        val hours = numberInput1.text.toString().toIntOrNull() ?:0
        val minutes = numberInput2.text.toString().toIntOrNull() ?:0
//        val hours = hoursText.toInt()
//        val minutes = minutesText.toInt()

        val result = 60 * hours + minutes
        return result

//        if (hoursText.isNotEmpty() && minutesText.isNotEmpty()) {
//            Toast.makeText(this, "目標時間が設定されました", Toast.LENGTH_SHORT).show()
//        }
        // MainActivity に計算結果を伝える
//        val intent = Intent(this, MainActivity::class.java)
//        intent.putExtra("result", result) // 計算結果を追加
//        startActivity(intent)
    }

//    fun addNumbers(view: View) {
//        val num1 = numberInput1.text.toString().toIntOrNull() ?: 0
//        val num2 = numberInput2.text.toString().toIntOrNull() ?: 0
//
//        val sum = 60*num1 + num2
//        resultText.text = "Result: $sum"
//    }
    //現在の目標外出時間を更新
    fun reloadGoalTime(){
    val db = _helper.writableDatabase

    // GoalTimeLogからデータを所得
    val sql = "SELECT * FROM GoalTimeLog"
    val cursor = db.rawQuery(sql, null)

    var timeData = ""
    while(cursor.moveToNext()) {
        val timeIdxNote = cursor.getColumnIndex("GoalTimeMin")
        timeData = cursor.getString(timeIdxNote)
    }
    Log.i("TAG", "$timeData")
    val rt = "現在の目標外出時間："
    var output = "$rt ${minToHour(timeData)}"
    resultText.text = output

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
    fun minToHour(min: String): String {
        // StringをIntに変換
        val minInt = min.toInt()
        val outputHour = minInt / 60
        val outputMin = minInt % 60
        val output = "${outputHour}時間${outputMin}分"
        return output
    }
}
