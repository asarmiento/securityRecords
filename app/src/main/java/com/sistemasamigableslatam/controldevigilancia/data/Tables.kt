package com.sistemasamigableslatam.controldevigilancia.data

import com.sistemasamigableslatam.controldevigilancia.Entities.RecordEntity
import com.sistemasamigableslatam.controldevigilancia.Entities.UserEntity
import java.util.*
import kotlin.collections.ArrayList

class Tables {

    abstract class Users {
        companion object{
            val COLUMN_UUID= "uuid"
            val TABLE_NAME="users"
            val COLUMN_NAME="name"
            val COLUMN_EMAIL="email"
            val COLUMN_CARD="card"
            val COLUMN_TYPEUSER = "type_user"
            val COLUMN_EMPLOYEEID = "employee_id"
            val users: MutableList<UserEntity> = ArrayList()
        }
    }

    abstract class Records{
        companion object{
            val COLUMN_UUID= "uuid"
            val TABLE_NAME="records"
            val COLUMN_EMPLOYEEID="employee_id"
            val COLUMN_COMMENTS="comments"
            val COLUMN_DATE="date"
            val COLUMN_TIME= "time"
            val COLUMN_LATITUD= "latitud"
            val COLUMN_LONGITUD= "longitud"
            val COLUMN_STATUS= "status"
            val COLUMN_TYPE= "type"
            val records:MutableList<RecordEntity> = ArrayList()
        }
    }
}