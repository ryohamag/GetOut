package com.websarva.wings.getout

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class NotificationActivity : AppCompatActivity() {
    //データベースヘルパーオブジェクトを作成
    private val _helper = DatabaseHelper(this@NotificationActivity)
    private lateinit var numberInput1: EditText
    private lateinit var numberInput2: EditText
    private lateinit var resultText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        val hoge=getTime("2023-3-8")
        Log.i("TAG", "$hoge")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notification)

        numberInput1 = findViewById(R.id.numberInput1)
        numberInput2 = findViewById(R.id.numberInput2)
        resultText = findViewById(R.id.resultText)

        val button: Button = findViewById(R.id.completionbutton)
        button.setOnClickListener {
            addNumbers()

        }
    }

    private fun addNumbers() {
        val hoursText = numberInput1.text.toString()
        val minutesText = numberInput2.text.toString()
        val hours = hoursText.toInt()
        val minutes = minutesText.toInt()
        val result = 60 * hours + minutes


        if (hoursText.isNotEmpty() && minutesText.isNotEmpty()) {


            // Calculate something or perform an action here



            // Display a Toast message with the calculated result
            Toast.makeText(this, "目標時間が設定されました", Toast.LENGTH_SHORT).show()
        }
        // MainActivity に計算結果を伝える
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("result", result) // 計算結果を追加
        startActivity(intent)
    }

    fun addNumbers(view: View) {
        val num1 = numberInput1.text.toString().toIntOrNull() ?: 0
        val num2 = numberInput2.text.toString().toIntOrNull() ?: 0

        val sum = 60*num1 + num2
        resultText.text = "Result: $sum"
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
