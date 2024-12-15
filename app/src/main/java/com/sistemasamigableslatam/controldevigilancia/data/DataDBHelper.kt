package com.sistemasamigableslatam.controldevigilancia.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import androidx.core.database.getBlobOrNull
import com.sistemasamigableslatam.controldevigilancia.Entities.RecordEntity
import com.sistemasamigableslatam.controldevigilancia.Entities.UserEntity

class DataDBHelper(context: Context) :
    SQLiteOpenHelper(context.applicationContext, DATABASE_NAME, null, DATABASE_VERSION) {

    private lateinit var db: SQLiteDatabase
    private lateinit var values: ContentValues

    companion object {
        private const val DATABASE_VERSION = 1
        private const val DATABASE_NAME = "friendlypos"
    }

    init {
        try {
            context.applicationContext.getDatabasePath(DATABASE_NAME).parentFile?.apply {
                if (!exists()) {
                    mkdirs()
                }
            }
            db = writableDatabase
            values = ContentValues()
        } catch (e: Exception) {
            Log.e("DataDBHelper", "Error initializing database", e)
        }
    }

    override fun onCreate(db: SQLiteDatabase) {
        try {
            db.execSQL("""
                CREATE TABLE IF NOT EXISTS ${Tables.Users.TABLE_NAME} (
                    ${Tables.Users.COLUMN_UUID} INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    ${Tables.Users.COLUMN_NAME} TEXT NOT NULL,
                    ${Tables.Users.COLUMN_EMAIL} TEXT NOT NULL,
                    ${Tables.Users.COLUMN_CARD} TEXT NOT NULL,
                    ${Tables.Users.COLUMN_TYPEUSER} TEXT NOT NULL,
                    ${Tables.Users.COLUMN_EMPLOYEEID} TEXT NOT NULL
                )
            """.trimIndent())
            
            db.execSQL("""
                CREATE TABLE IF NOT EXISTS ${Tables.Records.TABLE_NAME} (
                    ${Tables.Records.COLUMN_UUID} INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    ${Tables.Records.COLUMN_EMPLOYEEID} INTEGER NOT NULL,
                    ${Tables.Records.COLUMN_COMMENTS} TEXT NOT NULL,
                    ${Tables.Records.COLUMN_DATE} TEXT NOT NULL,
                    ${Tables.Records.COLUMN_TIME} TEXT NOT NULL,
                    ${Tables.Records.COLUMN_LATITUD} REAL NOT NULL,
                    ${Tables.Records.COLUMN_LONGITUD} REAL NOT NULL,
                    ${Tables.Records.COLUMN_TYPE} TEXT NOT NULL,
                    ${Tables.Records.COLUMN_STATUS} BOOLEAN
                )
            """.trimIndent())
        } catch (e: Exception) {
            Log.e("DataDBHelper", "Error creating database tables", e)
        }
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        TODO("Not yet implemented")
    }

    fun insertUser(user: List<UserEntity>) {
        values.put(Tables.Users.COLUMN_NAME, user[0].getName())
        values.put(Tables.Users.COLUMN_EMAIL, user[0].getEmail())
        values.put(Tables.Users.COLUMN_CARD, user[0].getCard())
        values.put(Tables.Users.COLUMN_TYPEUSER, user[0].getTypeUser())
        values.put(Tables.Users.COLUMN_EMPLOYEEID, user[0].getEmployeeId())

        db.insert(Tables.Users.TABLE_NAME, null, values)
    }

    fun consultIdUser(email: String): MutableList<UserEntity> {
        Tables.Users.users.clear()
        val columnas = arrayOf(
            Tables.Users.COLUMN_UUID,
            Tables.Users.COLUMN_NAME,
            Tables.Users.COLUMN_CARD,
            Tables.Users.COLUMN_EMAIL,
            Tables.Users.COLUMN_TYPEUSER,
            Tables.Users.COLUMN_EMPLOYEEID
        )
        val i = db.query(
            Tables.Users.TABLE_NAME,
            columnas,
            "${Tables.Users.COLUMN_CARD}=${email}",
            null,
            null,
            null,
            null
        )
        if (i.moveToFirst()) {
            do {
                Tables.Users.users.add(
                    UserEntity(
                        i.getString(0), i.getString(1),
                        i.getString(2), i.getString(3), i.getInt(4)
                    )
                )
            } while (i.moveToNext())

        }
        return Tables.Users.users
    }

    fun consultUser(): MutableList<UserEntity> {
        Tables.Users.users.clear()
        val columnas = arrayOf(
            Tables.Users.COLUMN_NAME,
            Tables.Users.COLUMN_CARD,
            Tables.Users.COLUMN_EMAIL,
            Tables.Users.COLUMN_TYPEUSER,
            Tables.Users.COLUMN_EMPLOYEEID
        )
        val i = db.query(
            Tables.Users.TABLE_NAME,
            columnas,
            null,
            null,
            null,
            null,
            null
        )
        if (i.moveToFirst()) {
            do {
                Tables.Users.users.add(
                    UserEntity(
                        i.getString(0), i.getString(1),
                        i.getString(2), i.getString(3), i.getInt(4)
                    )
                )
            } while (i.moveToNext())

        }
        return Tables.Users.users
    }

    fun consultRecord(): MutableList<RecordEntity> {
        Tables.Records.records.clear()
        val columnas = arrayOf(
            Tables.Records.COLUMN_UUID,
            Tables.Records.COLUMN_EMPLOYEEID,
            Tables.Records.COLUMN_COMMENTS,
            Tables.Records.COLUMN_DATE,
            Tables.Records.COLUMN_TIME,
            Tables.Records.COLUMN_LATITUD,
            Tables.Records.COLUMN_LONGITUD,
            Tables.Records.COLUMN_STATUS,
            Tables.Records.COLUMN_TYPE
        )
        val i = db.query(
            Tables.Records.TABLE_NAME,
            columnas,
            null,
            null,
            null,
            null,
            null
        )
        if (i.moveToFirst()) {
            do {
                Tables.Records.records.add(
                    RecordEntity(
                        i.getInt(0),
                        i.getInt(1),
                        i.getString(2),
                        i.getString(3),
                        i.getString(4),
                        i.getDouble(5),
                        i.getDouble(6),
                        false,
                        i.getString(7)
                    )
                )
            } while (i.moveToNext())

        }
        return Tables.Records.records
    }

    fun consultSendRecord(): MutableList<RecordEntity> {
        Tables.Records.records.clear()
        val columnas = arrayOf(
            Tables.Records.COLUMN_UUID,
            Tables.Records.COLUMN_EMPLOYEEID,
            Tables.Records.COLUMN_COMMENTS,
            Tables.Records.COLUMN_DATE,
            Tables.Records.COLUMN_TIME,
            Tables.Records.COLUMN_LATITUD,
            Tables.Records.COLUMN_LONGITUD,
            Tables.Records.COLUMN_TYPE,
            Tables.Records.COLUMN_STATUS
        )
        val i = db.query(
            Tables.Records.TABLE_NAME,
            columnas,
            "${Tables.Records.COLUMN_STATUS}=0",
            null,
            null,
            null,
            null
        )
        if (i.moveToFirst()) {
            do {
                Tables.Records.records.add(
                    RecordEntity(
                        i.getInt(0),
                        i.getInt(1),
                        i.getString(2),
                        i.getString(3),
                        i.getString(4),
                        i.getDouble(5),
                        i.getDouble(6),
                       false,
                        i.getString(7)
                    )
                )
            } while (i.moveToNext())

        }
        return Tables.Records.records
    }

    fun consultOutRecord(): Int {
        Tables.Records.records.clear()
        val columnas = arrayOf(
            Tables.Records.COLUMN_UUID,
            Tables.Records.COLUMN_EMPLOYEEID,
            Tables.Records.COLUMN_COMMENTS,
            Tables.Records.COLUMN_DATE,
            Tables.Records.COLUMN_TIME,
            Tables.Records.COLUMN_LATITUD,
            Tables.Records.COLUMN_LONGITUD,
            Tables.Records.COLUMN_TYPE,
            Tables.Records.COLUMN_STATUS
        )
        val i = db.query(
            Tables.Records.TABLE_NAME,
            columnas,
            "${Tables.Records.COLUMN_STATUS}=${0}",
            null,
            null,
            null,
            null
        )
        var idC: Int = 0
        if (i.moveToFirst()) {
            do {
                idC = i.getInt(0)
            } while (i.moveToNext())

        }
        return idC.toInt()
    }

    fun insertRecord(record: List<RecordEntity>) {
        Tables.Records.records.clear()
        values.put(Tables.Records.COLUMN_EMPLOYEEID, record[0].getEmployeeId())
        values.put(Tables.Records.COLUMN_COMMENTS, record[0].getComments())
        values.put(Tables.Records.COLUMN_DATE, record[0].getDate())
        values.put(Tables.Records.COLUMN_TIME, record[0].getTime())
        values.put(Tables.Records.COLUMN_STATUS, record[0].getStatus())
        values.put(Tables.Records.COLUMN_LATITUD, record[0].getLatitud())
        values.put(Tables.Records.COLUMN_LONGITUD, record[0].getLongitud())
        values.put(Tables.Records.COLUMN_TYPE, record[0].getType())

        db.insert(Tables.Records.TABLE_NAME, null, values)
    }


    fun updateSendRecord() {
        Tables.Records.records.clear()
        values.put(Tables.Records.COLUMN_STATUS, true)

        db.update(Tables.Records.TABLE_NAME, values, "${Tables.Records.COLUMN_STATUS}=${0}", null)
    }

    fun clearUsers() {
        val db = this.writableDatabase
      //  db.delete(TABLE_USERS, null, null)
        db.close()
    }
}