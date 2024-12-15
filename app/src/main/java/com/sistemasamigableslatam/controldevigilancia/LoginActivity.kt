package com.sistemasamigableslatam.controldevigilancia

import android.content.Intent
import android.content.res.Configuration
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
import java.util.*

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private var listUsers: MutableList<UserEntity> = ArrayList()
    private var dbInv: DataDBHelper? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Primero verificamos si hay un usuario registrado
        dbInv = DataDBHelper(this)
        val existingUsers = dbInv?.consultUser()
        
        if (!existingUsers.isNullOrEmpty()) {
            // Si hay usuario registrado, vamos directamente a MainActivity
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }
        
        // Si no hay usuario, mostramos la pantalla de login
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Configurar el click listener del botón
        binding.btnLogin.setOnClickListener {
            sendlogin()
        }
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

    private fun sendlogin() {
        if(validateInput()) {
            val dlLoading = LayoutInflater.from(this).inflate(R.layout.layout_loading, null)
            val loadingDialog = AlertDialog.Builder(this)
                .setView(dlLoading)
                .setCancelable(false)
                .create()
            loadingDialog.show()
            
            val params = JSONObject()
            try {
                params.put("email", binding.etEmail.text.toString())
                params.put("password", binding.etPassword.text.toString())
            } catch (e: JSONException) {
                e.printStackTrace()
            }
            
            Log.i("UserPassword: ", params.toString())
            val data = params.toString()
            val url = getString(R.string.api_serve) + "/login"
            Log.i("Url: ", url)
            
            Thread {
                val http = Http(this@LoginActivity, url)
                http.setMethod("POST")
                http.setData(data)
                http.send()
                
                Log.i("Http: ", http.response.toString())
                runOnUiThread {
                    loadingDialog.dismiss()
                    
                    val code = http.statusCode
                    Log.i("responselogin: ", "$code -- ${http.response}")
                    
                    try {
                        val response = JSONObject(http.response)
                        when (code) {
                            200, 201 -> {
                                val user = response.getJSONObject("user")
                                val employee = user.getJSONObject("employee")
                                val localStorage = LocalStorage(this)

                                localStorage.setToken(response.optString("token", ""))
                                listUsers.add(
                                    UserEntity(
                                        user.optString("name", ""),
                                        user.optString("email", ""),
                                        employee.optString("card", ""),
                                        user.optString("type", ""),
                                        employee.optInt("id", 0)
                                    )
                                )
                                
                                val existingUser = dbInv?.consultIdUser(employee.getString("card").orEmpty())
                                if(existingUser.isNullOrEmpty()) {
                                    dbInv?.insertUser(listUsers)
                                }
                                
                                Log.i("Token: ", response.getString("token"))
                                val intent = Intent(this, MainActivity::class.java)
                                startActivity(intent)
                                finish() // Cerramos LoginActivity después de login exitoso
                            }
                            401 -> {
                                Toast.makeText(
                                    this@LoginActivity, 
                                    getString(R.string.invalid_credentials),
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                            422 -> {
                                val msg = response.getString("message")
                                Toast.makeText(this@LoginActivity, msg, Toast.LENGTH_LONG).show()
                            }
                            else -> {
                                Toast.makeText(
                                    this@LoginActivity,
                                    getString(R.string.error_login),
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                    } catch (e: JSONException) {
                        e.printStackTrace()
                        Toast.makeText(
                            this@LoginActivity,
                            getString(R.string.error_login),
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }.start()
        }
    }

    private fun setLocale(languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        
        val config = resources.configuration.apply {
            setLocale(locale)
        }
        
        createConfigurationContext(config)
        resources.displayMetrics.setTo(resources.displayMetrics)
    }

    // Para cambiar al inglés:
    // setLocale("en")
    // Para cambiar al español:
    // setLocale("es")

    // Método para cerrar sesión (agrégalo a MainActivity)
    fun logout() {
        // Limpiar la base de datos local
        dbInv?.clearUsers()
        
        // Limpiar el token
        val localStorage = LocalStorage(this)
        localStorage.clearToken()
        
        // Volver a LoginActivity
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}