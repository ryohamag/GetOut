package com.websarva.wings.getout

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context):SQLiteOpenHelper(context,DATABASE_NAME,null,DATABASE_VERSION)
{
    companion object{
        private const val DATABASE_NAME = "timeLog.db"
        private const val DATABASE_VERSION = 1
    }

    override fun onCreate(db: SQLiteDatabase) {
        //　外出日時を記録するデータベースを作成
        val sbGetOutLogs = StringBuilder()
        sbGetOutLogs.append("CREATE TABLE GetOutTimeLogs(")
//        sbGetOutLogs.append("_id INTEGER PRIMARY KEY,")
        sbGetOutLogs.append("getOutDate TEXT,")
        sbGetOutLogs.append("getOutHour TEXT,")
        sbGetOutLogs.append("getOutMin TEXT")
        sbGetOutLogs.append(");")
        val sqlGO = sbGetOutLogs.toString()
        db.execSQL(sqlGO)

        //　帰宅日時を記録するデータベースを作成
        val sbGetHomeLogs = StringBuilder()
        sbGetHomeLogs.append("CREATE TABLE GetHomeTimeLogs(")
//        sbGetHomeLogs.append("_id INTEGER PRIMARY KEY,")
        sbGetHomeLogs.append("getHomeDate TEXT,")
        sbGetHomeLogs.append("getHomeHour TEXT,")
        sbGetHomeLogs.append("getHomeMin TEXT")
        sbGetHomeLogs.append(");")
        val sqlGH = sbGetHomeLogs.toString()
        db.execSQL(sqlGH)

        // 合計外出時間を記録するデータベースを作成
        val sbTimeSumLogs = StringBuilder()
        sbTimeSumLogs.append("CREATE TABLE TimeSumLogs(")
        sbTimeSumLogs.append("TimeSumDate TEXT,")
        sbTimeSumLogs.append("TimeSumTime TEXT")
        sbTimeSumLogs.append(");")
        val sqlTS = sbTimeSumLogs.toString()
        db.execSQL(sqlTS)


    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {}
}