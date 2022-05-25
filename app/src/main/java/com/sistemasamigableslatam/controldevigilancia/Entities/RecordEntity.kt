package com.sistemasamigableslatam.controldevigilancia.Entities


import androidx.room.Entity

@Entity(tableName="records")
class RecordEntity {

    private var uuid:Int=0
    private var employeeId:Int
    private var comments:String
    private var date:String
    private var latitud:Double
    private var longitud:Double
    private var time:String
    private var status:Boolean= false
    private var type:String

    constructor(
        uuid: Int,
        employeeId: Int,
        comments: String,
        date: String,
        time: String,
        latitud: Double,
        longitud: Double,
        status: Boolean,
        type: String
    ){
        this.uuid=uuid
        this.employeeId=employeeId
        this.comments=comments
        this.date=date
        this.time=time
        this.status=status
        this.latitud=latitud
        this.longitud=longitud
        this.type=type
    }

    fun getUuid():Int{
        return uuid
    }
    fun getStatus():Boolean{
        return status
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

    fun getTime():String{
        return time
    }

    fun getLatitud():Double{
        return latitud
    }
    fun getLongitud():Double{
        return longitud
    }
    fun getType():String{
        return type
    }
}