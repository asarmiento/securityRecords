package com.sistemasamigableslatam.controldevigilancia.Entities


import androidx.room.Entity

@Entity(tableName="records")
class RecordEntity {

    private var uuid:Int=0
    private var employeeId:Int=0
    private var comments:String=""
    private var date:String=""
    private var entryTime:String=""
    private var outTime:String=""

    constructor(uuid:Int,employeeId:Int,comments:String,date:String,entryTime:String,outTime:String){
        this.uuid=uuid
        this.employeeId=employeeId
        this.comments=comments
        this.date=date
        this.entryTime=entryTime
        this.outTime=outTime
    }

    fun getUuid():Int{
        return uuid
    }

    fun getEmployeeId():Int{
        return employeeId
    }
    fun getComments():String{
        return comments
    }
    fun getDate():String{
        return date
    }
    fun getEntryTime():String{
        return entryTime
    }

    fun getOutTime():String{
        return outTime
    }
}