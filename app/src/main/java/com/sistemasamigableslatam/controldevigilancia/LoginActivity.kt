package com.sistemasamigableslatam.controldevigilancia

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.sistemasamigableslatam.controldevigilancia.Entities.UserEntity
import com.sistemasamigableslatam.controldevigilancia.data.DataDBHelper
import com.sistemasamigableslatam.controldevigilancia.data.Http
import com.sistemasamigableslatam.controldevigilancia.data.LocalStorage
import com.sistemasamigableslatam.controldevigilancia.databinding.ActivityLoginBinding
import org.json.JSONException
import org.json.JSONObject

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private var listUsers: MutableList<UserEntity> = ArrayList()
    private var dbInv: DataDBHelper? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        dbInv = DataDBHelper(this)
    }

    fun validateInput(): Boolean {
        if(binding.etEmail.text.toString().isEmpty()) {
            binding.etEmail.error = "Debe llenar este campo"
            return false
        }

        if (binding.etPassword.text.toString().isEmpty()) {
            binding.etPassword.error = "Debe llenar este campo"
            return false
        }
        return true
    }

    fun sendlogin(view: View) {
        if(validateInput()) {
            val dlLoading = LayoutInflater.from(this).inflate(R.layout.layout_loading, null)
            val dlBuilder = AlertDialog.Builder(this)
            dlBuilder.setView(dlLoading).show()
            
            val params = JSONObject()
            try {
                params.put("email", binding.etEmail.text.toString())
                params.put("password", binding.etPassword.text.toString())
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
                    Log.i("responselogin: ", "$code -- ${http.response}")
                    
                    if (code == 201 || code == 200) {
                        val response = JSONObject(http.response)
                        val localStorage = LocalStorage(this)

                        localStorage.setToken(response.getString("token"))
                        listUsers.add(
                            UserEntity(
                                response.getString("name"),
                                response.getString("email"),
                                response.getString("card"),
                                response.getString("type_user"),
                                response.getString("employee_id").toInt()
                            )
                        )
                        
                        val user = dbInv?.consultIdUser(response.getString("card"))
                        if(user.isNullOrEmpty()) {
                            dbInv?.insertUser(listUsers)
                        }
                        
                        Log.i("Token: ", response.getString("token"))
                        val intent = Intent(this, MainActivity::class.java)
                        startActivity(intent)
                    } else if (code == 422) {
                        try {
                            val response = JSONObject(http.response)
                            val msg = response.getString("message")
                            Toast.makeText(this@LoginActivity, msg, Toast.LENGTH_LONG).show()
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