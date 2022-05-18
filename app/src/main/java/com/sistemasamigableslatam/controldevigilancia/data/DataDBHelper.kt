package com.sistemasamigableslatam.controldevigilancia.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.sistemasamigableslatam.controldevigilancia.Entities.RecordEntity
import com.sistemasamigableslatam.controldevigilancia.Entities.UserEntity

class DataDBHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    private val db: SQLiteDatabase
    private val values: ContentValues

    companion object {
        private val DATABASE_VERSION = 1
        private val DATABASE_NAME = "friendlypos"
    }

    init {
        db = this.writableDatabase
        values = ContentValues()
    }

    override fun onCreate(db: SQLiteDatabase?) {
        db!!.execSQL("CREATE TABLE "+Tables.Users.TABLE_NAME+" (" +
                Tables.Users.COLUMN_UUID +" INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, "+
                Tables.Users.COLUMN_NAME +" TEXT NOT NULL, "+
                Tables.Users.COLUMN_EMAIL +" TEXT NOT NULL, "+
                Tables.Users.COLUMN_CARD +" TEXT NOT NULL ,"+
                Tables.Users.COLUMN_TYPEUSER +" TEXT NOT NULL ,"+
                Tables.Users.COLUMN_EMPLOYEEID +" TEXT NOT NULL"+
                ");")
        db!!.execSQL("CREATE TABLE "+Tables.Records.TABLE_NAME+" ("+Tables.Records.UUID+" INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL ,"+
                Tables.Records.COLUMN_EMPLOYEEID + " INTEGER NOT NULL ,"+
                Tables.Records.COLUMN_COMMENTS +" TEXT NOT NULL, "+
                Tables.Records.COLUMN_DATE +" TEXT NOT NULL, "+
                Tables.Records.COLUMN_ENTRYTIME +" TEXT NOT NULL ,"+
                Tables.Records.COLUMN_OUTTIME + " TEXT NOT NULL "
                +");")
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        TODO("Not yet implemented")
    }

    fun insertUser(user:List<UserEntity>){
        values.put(Tables.Users.COLUMN_NAME,user[0].getName())
        values.put(Tables.Users.COLUMN_EMAIL,user[0].getEmail())
        values.put(Tables.Users.COLUMN_CARD,user[0].getCard())
        values.put(Tables.Users.COLUMN_TYPEUSER,user[0].getTypeUser())
        values.put(Tables.Users.COLUMN_EMPLOYEEID,user[0].getEmployeeId())

        db.insert(Tables.Users.TABLE_NAME,null,values)
    }
    fun consultIdUser(email:String){
        Tables.Users.users.clear()
        val columnas = arrayOf(Tables.Users.COLUMN_UUID,
        Tables.Users.COLUMN_NAME,Tables.Users.COLUMN_CARD,Tables.Users.COLUMN_EMAIL,Tables.Users.COLUMN_TYPEUSER,
        Tables.Users.COLUMN_EMPLOYEEID)
        val i =    db.query(Tables.Users.TABLE_NAME,columnas, "${Tables.Users.COLUMN_EMAIL}=${email}",null,null,null,null)
        if (i.moveToFirst()) {
            do {
                Tables.Users.users.add(
                    UserEntity(i.getColumnIndex(Tables.Users.COLUMN_NAME),i.getColumnIndex(Tables.Users.COLUMN_CARD),
                        i.getColumnIndex(Tables.Users.COLUMN_EMAIL))
                )
            }while (i.moveToNext())
    }
    fun insertRecord(record:List<RecordEntity>){
        values.put(Tables.Records.COLUMN_EMPLOYEEID,record[0].getEmployeeId())
        values.put(Tables.Records.COLUMN_COMMENTS,record[0].getComments())
        values.put(Tables.Records.COLUMN_DATE,record[0].getDate())
        values.put(Tables.Records.COLUMN_ENTRYTIME,record[0].getEntryTime())

        db.insert(Tables.Records.TABLE_NAME,null,values)
    }
}