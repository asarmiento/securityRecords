package com.sistemasamigableslatam.controldevigilancia.data

import android.content.Context
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.*


class Http(var context: Context, private val url: String) {
    private var method = "GET"
    private var data: String? = null
    var response: String? = null
        private set
    var statusCode = 0
        private set
    private var token = false
    private val localStorage: LocalStorage
    fun setMethod(method: String) {
        this.method = method.uppercase(Locale.getDefault())
    }

    fun setData(data: String?) {
        this.data = data
    }

    fun setToken(token: Boolean) {
        this.token = token
    }

    fun send() {
        try {
            val sUrl = URL(url)
            val connection = sUrl.openConnection() as HttpURLConnection
            connection.requestMethod = method
            connection.setRequestProperty("Content-Type", "application/json")
            connection.setRequestProperty("Accept", "application/json")
            if (token) {
                connection.setRequestProperty("Autorization", "Bearer " + localStorage.token)
            }
            if (method != "GET") {
                connection.doOutput = true
            }
            if (data != null) {
                val os = connection.outputStream
                os.write(data!!.toByteArray())
                os.flush()
                os.close()
            }
            statusCode = connection.responseCode
            val isr: InputStreamReader
            isr = if (statusCode >= 200 && statusCode <= 299) {
                InputStreamReader(connection.inputStream)
            } else {
                InputStreamReader(connection.errorStream)
            }
            val br = BufferedReader(isr)
            val sb = StringBuffer()
            var line: String?
            while (br.readLine().also { line = it } != null) {
                sb.append(line)
            }
            br.close()
            response = sb.toString()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    init {
        localStorage = LocalStorage(context)
    }
}