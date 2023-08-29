package com.websarva.wings.getout

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class DatabaseHelper(context: Context):SQLiteOpenHelper(context,DATABASE_NAME,null,DATABASE_VERSION)
{
    companion object{
        private const val DATABASE_NAME = "timeLog.db"
        private const val DATABASE_VERSION = 1
    }

    override fun onCreate(db: SQLiteDatabase) {
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


//        //　帰宅日時を記録するデータベースを作成
//        val sbGetHomeLogs = StringBuilder()
//        sbGetHomeLogs.append("CREATE TABLE GetHomeTimeLogs(")
//        sbGetHomeLogs.append("getHomeDate TEXT,")
//        sbGetHomeLogs.append("getHomeHour TEXT,")
//        sbGetHomeLogs.append("getHomeMin TEXT")
//        sbGetHomeLogs.append(");")
//        val sqlGH = sbGetHomeLogs.toString()
//        db.execSQL(sqlGH)

        // 合計外出時間を記録するデータベースを作成
        val sbTimeSumLog = StringBuilder()
        sbTimeSumLog.append("CREATE TABLE TimeSumLog(")
        sbTimeSumLog.append("Date TEXT,")
        sbTimeSumLog.append("Time TEXT")
        sbTimeSumLog.append(");")
        val sqlTS = sbTimeSumLog.toString()
        db.execSQL(sqlTS)



        // 期間の開始と終了日を指定
        val startDate = "2023-1-1"
        val endDate = "2023-12-31"

        // 外出時間サンプルのフォーマットを指定
        val dfTime = DateTimeFormatter.ofPattern("Md")

        // 日付のフォーマットを指定
        val dfDate = DateTimeFormatter.ofPattern("yyyy-M-d")

        val parsedStartDate = LocalDate.parse(startDate, dfDate)
        val parsedEndDate = LocalDate.parse(endDate, dfDate)

        // 参照する日付
        var currentDate = parsedStartDate

        // 日付が終了日より前の間
        while (!currentDate.isAfter(parsedEndDate)) {
            val values = ContentValues()
            values.put("Date", currentDate.format(dfDate))
            values.put("Time", currentDate.format(dfTime))

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
