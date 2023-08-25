package com.websarva.wings.getout

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.util.Date

class MainActivity : AppCompatActivity() {

    //データベースヘルパーオブジェクトを作成
    private val _helper = DatabaseHelper(this@MainActivity)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onDestroy() {
        _helper.close()
        super.onDestroy()
    }

    fun onGetOutButtonClick(view: View){

        // 現在日時を表示
//        val date1 = LocalDateTime.now()
        val df = SimpleDateFormat("yyyy年MM月dd日 HH:mm")
        val date = df.format(Date())

        Log.i("test", date)
    }
}
