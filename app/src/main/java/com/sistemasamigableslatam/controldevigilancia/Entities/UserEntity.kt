package com.sistemasamigableslatam.controldevigilancia.Entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity(tableName = "users")
class UserEntity {
    @PrimaryKey(autoGenerate = true)
    private var uuid: Int = 0
    private var name: String = ""
    private var email: String = ""
    private var card: String = ""
    private var typeUser: String = ""
    private var employeeId: Int = 0

    constructor(
        name: String,
        email: String,
        card: String,
        typeUser: String,
        employeeId: Int
    ) {
        this.name = name
        this.email = email
        this.card = card
        this.typeUser = typeUser
        this.employeeId = employeeId

    }

    fun getUuid(): Int {
        return uuid
    }

    fun getName(): String {
        return name
    }

    fun getEmail(): String {
        return email
    }

    fun getCard(): String {
        return card
    }

    fun getTypeUser(): String {
        return typeUser
    }

    fun getEmployeeId(): Int {
        return employeeId
    }
}