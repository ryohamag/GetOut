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
        sbGetOutLogs.append("getOutTime TEXT")
        sbGetOutLogs.append(");")
        val sqlGO = sbGetOutLogs.toString()
        db.execSQL(sqlGO)

        //　帰宅日時を記録するデータベースを作成
        val sbGetHomeLogs = StringBuilder()
        sbGetHomeLogs.append("CREATE TABLE GetHomeTimeLogs(")
//        sbGetHomeLogs.append("_id INTEGER PRIMARY KEY,")
        sbGetHomeLogs.append("getHomeDate TEXT,")
        sbGetHomeLogs.append("getHomeTime TEXT")
        sbGetHomeLogs.append(");")
        val sqlGH = sbGetHomeLogs.toString()
        db.execSQL(sqlGH)

    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {}
}