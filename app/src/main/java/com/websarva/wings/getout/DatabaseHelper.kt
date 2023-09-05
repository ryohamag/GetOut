package com.websarva.wings.getout

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Random
import kotlin.math.log

class DatabaseHelper(context: Context):SQLiteOpenHelper(context,DATABASE_NAME,null,DATABASE_VERSION)
{
    companion object{
        private const val DATABASE_NAME = "timeLog.db"
        private const val DATABASE_VERSION = 1
    }

    override fun onCreate(db: SQLiteDatabase) {
        Log.i("onCreate", "onCreate: ")
        //　外出日時を記録するデータベースを作成
        val sbGetOutLog = StringBuilder()
        sbGetOutLog.append("CREATE TABLE GetOutTimeLog(")
        sbGetOutLog.append("getOutDate TEXT,")
        sbGetOutLog.append("getOutHour TEXT,")
        sbGetOutLog.append("getOutMin TEXT")
        sbGetOutLog.append(");")
        val sqlGO = sbGetOutLog.toString()
        db.execSQL(sqlGO)
        // 要素がnullのみの行を挿入
        val sqlInsertNullRow = "INSERT INTO GetOutTimeLog (getOutDate, getOutHour, getOutMin) VALUES (null, null, null);"
        db.execSQL(sqlInsertNullRow)


        val goalTimeLo = StringBuilder()
        goalTimeLo.append("CREATE TABLE a(")
        goalTimeLo.append("b TEXT,")
        goalTimeLo.append("c TEXT")
        goalTimeLo.append(");")
        val sq = goalTimeLo.toString()
        db.execSQL(sq)

        //　目標外出時間を記録するデータベースを作成
        val goalTimeLog = StringBuilder()
        goalTimeLog.append("CREATE TABLE GoalTimeLog(")
        goalTimeLog.append("GoalTimeMin TEXT")
        goalTimeLog.append(");")
        val sql = goalTimeLog.toString()
        db.execSQL(sql)
        val sqlInsertNaturalRow = "INSERT INTO GoalTimeLog (GoalTimeMin) VALUES ('360');"
        db.execSQL(sqlInsertNaturalRow)

        // 合計外出時間を記録するデータベースを作成
        val sbTimeSumLog = StringBuilder()
        sbTimeSumLog.append("CREATE TABLE TimeSumLog(")
        sbTimeSumLog.append("Date TEXT,")
        sbTimeSumLog.append("Time TEXT")
        sbTimeSumLog.append(");")
        val sqlTS = sbTimeSumLog.toString()
        db.execSQL(sqlTS)



        // 期間の開始と終了日を指定
        val startDate = "2020-1-1"
        val endDate = "2025-12-31"
        // 現在日時を所得
        val dfDate = SimpleDateFormat("yyyy-M-d")
        val date = dfDate.format(Date())
        Log.i("DB", "$date")

//        // 外出時間サンプルのフォーマットを指定
//        val dfTime = DateTimeFormatter.ofPattern("Md")

        // 日付のフォーマットを指定
        val df = DateTimeFormatter.ofPattern("yyyy-M-d")

        val today = LocalDate.parse(date, df)
        val parsedStartDate = LocalDate.parse(startDate, df)
        val parsedEndDate = LocalDate.parse(endDate, df)

        // 参照する日付
        var currentDate = parsedStartDate

        Log.i("DB", "onCreate: ")
        // 日付が終了日より前の間
        while (!currentDate.isAfter(parsedEndDate)) {
            val values = ContentValues()
            values.put("Date", currentDate.format(df))
//            values.put("Time", currentDate.format(dfTime))
//            values.put("Time", "1")
            val minValue = 180
            val maxValue = 720

            val random = Random()
            val randomValue = random.nextInt(maxValue - minValue + 1) + minValue
            if(!currentDate.isAfter(today)) {
                values.put("Time",randomValue.toString())
                Log.i("DB", "$randomValue")
            }
            else{
                values.put("Time","0")
            }
            //　日付と外出時間＝０を挿入
            db.insert("TimeSumLog", null, values)

            //　日にちを一日進める
            currentDate = currentDate.plusDays(1)
        }
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {}

    //　TimeSumLogデータベースの初期値を設定する。
//    fun timeSumLogSetUp(){
//
//        // 期間の開始と終了日を指定
//        val startDate = "20230101"
//        val endDate = "20231231"
//
//        // 日付のフォーマットを指定
//        val dfDate = DateTimeFormatter.ofPattern("yyyyMMdd")
//
//
//        val parsedStartDate = LocalDate.parse(startDate, dfDate)
//        val parsedEndDate = LocalDate.parse(endDate, dfDate)
//
//        val sqlInsert = "INSERT INTO TimeSumLog (TimeSumDate, TimeSumTime) VALUES (?, ?)"
//
//        var currentDate = parsedStartDate
//        while (!currentDate.isAfter(parsedEndDate)) {
//            Log.i("calender",currentDate.format(dfDate))
//            currentDate = currentDate.plusDays(1)
//
//            var stmt = db.compileStatement(sqlInsert)
//            //　変数のバインド
//            stmt.bindString(1, currentDate.format((dfDate)))
//            stmt.bindString(2, "0")
//
//            stmt.executeInsert()
//        }
//
//
//        db.close()
//    }
}
