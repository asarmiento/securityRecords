package com.sistemasamigableslatam.controldevigilancia.data

import android.content.Context
import android.content.SharedPreferences

class LocalStorage(var context: Context) {
    var sharedPreserences: SharedPreferences
    var editor: SharedPreferences.Editor
    var token: String? = null

    @JvmName("getToken1")
    fun getToken(): String? {
        token = sharedPreserences.getString("TOKEN", "")
        return token
    }

    @JvmName("setToken1")
    fun setToken(token: String?) {
        editor.putString("TOKEN", token)
        editor.commit()
        this.token = token
    }

    init {
        sharedPreserences = context.getSharedPreferences("STOREAGE_LOGIN_API", Context.MODE_PRIVATE)
        editor = sharedPreserences.edit()
    }

    fun clearToken() {
        sharedPreserences.edit().remove("token").apply()
    }
}