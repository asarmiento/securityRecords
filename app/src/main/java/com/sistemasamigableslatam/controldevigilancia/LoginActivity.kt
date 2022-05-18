package com.sistemasamigableslatam.controldevigilancia

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.sistemasamigableslatam.controldevigilancia.Entities.UserEntity
import com.sistemasamigableslatam.controldevigilancia.data.DataDBHelper
import com.sistemasamigableslatam.controldevigilancia.data.Http
import com.sistemasamigableslatam.controldevigilancia.data.LocalStorage
import org.json.JSONException
import org.json.JSONObject
import java.util.ArrayList

class LoginActivity : AppCompatActivity() {
    lateinit var txtEmailAddress:EditText
    lateinit var txtPassword:EditText
    private var listUsers: MutableList<UserEntity> = ArrayList()
    private var dbInv: DataDBHelper? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        txtEmailAddress= findViewById(R.id.TxtEmailAddress)
        txtPassword= findViewById(R.id.TxtPassword)
        dbInv = DataDBHelper(this)

    }

    fun validateInput():Boolean{
        if(txtEmailAddress.text.toString() == ""){
            txtEmailAddress.error= "Debe llenar este campo"
            return false
        }

        if (txtPassword.text.toString()==""){
            txtPassword.error = "Debe llenar este campo"
            return false
        }
        return true
    }

    fun btnLoginSesion(view: View){




    }
     fun sendlogin(view: View) {
         if(validateInput()) {
             val dlLoading = LayoutInflater.from(this).inflate(R.layout.layout_loading, null)
             val dlBuilder = AlertDialog.Builder(this)
             dlBuilder.setView(dlLoading).show()
             val params = JSONObject()
             try {
                 params.put("email", txtEmailAddress.text.toString())
                 params.put("password", txtPassword.text.toString())
             } catch (e: JSONException) {
                 e.printStackTrace()
             }
             Log.i("UserPassword: ", params.toString())
             val data = params.toString()
             val url = getString(R.string.api_serve) + "/login-web"
             Log.i("Url: ", url)
             Thread {
                 val http = Http(this@LoginActivity, url)
                 http.setMethod("POST")
                 http.setData(data)
                 http.send()
                 Log.i("Http: ", http.response.toString())
                 runOnUiThread {
                     val code = http.statusCode
                     if (code == 201 || code == 200) {
                         val response: JSONObject =  JSONObject(http.response)
                         val localStorage= LocalStorage(this)
                         localStorage.setToken(response.getString("token"))
                         listUsers.add(UserEntity(response.getString("name").toString(),
                             response.getString("email").toString(),
                             response.getString("card").toString(),
                             response.getString("type_user").toString(),
                             response.getString("employee_id").toInt())
                         )
                         val user = dbInv?.consultIdUser(response.getString("card").toString())
                         if(user.isNullOrEmpty()) {
                             dbInv?.insertUser(listUsers);
                         }
                         Log.i("Token: ", response.getString("token"))
                         val intent = Intent(this,MainActivity::class.java)
                         startActivity(intent)
                     } else if (code == 422) {
                         try {
                             val response = JSONObject(http.response)
                             val msg = response.getString("message")
                         } catch (e: JSONException) {
                             e.printStackTrace()
                         }
                     } else {
                         Toast.makeText(this@LoginActivity, "Error $code", Toast.LENGTH_LONG).show()
                     }
                 }
             }.start()
         }
    }

}