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
            val UUID= "uuid"
            val TABLE_NAME="records"
            val COLUMN_EMPLOYEEID="employee_id"
            val COLUMN_COMMENTS="comments"
            val COLUMN_DATE="date"
            val COLUMN_ENTRYTIME= "entry_time"
            val COLUMN_OUTTIME= "out_time"
            val records:MutableList<RecordEntity> = ArrayList()
        }
    }
}