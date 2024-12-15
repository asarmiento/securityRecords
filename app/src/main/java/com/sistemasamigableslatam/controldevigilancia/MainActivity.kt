package com.sistemasamigableslatam.controldevigilancia

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.google.android.gms.location.*
import com.sistemasamigableslatam.controldevigilancia.Entities.RecordEntity
import com.sistemasamigableslatam.controldevigilancia.Entities.UserEntity
import com.sistemasamigableslatam.controldevigilancia.data.DataDBHelper
import com.sistemasamigableslatam.controldevigilancia.data.Http
import org.json.JSONException
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity() {
    private lateinit var mFusedLocationClient: FusedLocationProviderClient
    val PERMISSION_ID = 42
    lateinit var lbllatitud: TextView
    lateinit var lbllongitud: TextView
    lateinit var tvHello: TextView
    lateinit var tvNote: TextView
    lateinit var txtComment: TextView
    lateinit var date: String
    lateinit var timeEntry: String
    private var dbInv: DataDBHelper? = null
    private var userData: MutableList<UserEntity> = ArrayList()
    private var recordData: MutableList<RecordEntity> = ArrayList()
    lateinit var btnEntry: Button
    lateinit var btnOut: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        // Verificar permisos de almacenamiento
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
            if (checkStoragePermissions()) {
                initializeDatabase()
            } else {
                requestStoragePermissions()
            }
        } else {
            initializeDatabase()
        }
        
        val dateInv = getCurrentDateTime()
        date = dateInv.toString("yyyy-MM-dd")
        val dateInvTime = getCurrentDateTime()
        timeEntry = dateInvTime.toString("HH:mm:ss")

        tvHello = findViewById(R.id.tvHello)
        txtComment = findViewById(R.id.txtComment)
        btnEntry = findViewById(R.id.btndetectar)
        btnOut = findViewById(R.id.btnOut)
        tvNote = findViewById(R.id.tvNote)
        lbllatitud = findViewById(R.id.tvLbllatitud)
        lbllongitud = findViewById(R.id.tvLbllongitud)
        tvNote.text = ""

        userData = dbInv?.consultUser() ?: mutableListOf()

        tvHello.text = "Bienvenido (a): ${userData[0].getName().toString()}"
        if (allPermissionsGranteGPS()) {
            mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
            leerubicacionactual("a")
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ),
                PERMISSION_ID
            )
        }
        var idAdd: Int? = dbInv?.consultOutRecord()
        if (idAdd!! > 0) {
            btnEntry.isVisible = false
            btnOut.isVisible = true
        }

        // Configurar click listeners
        btnEntry.setOnClickListener {
            clickButton()
        }
        
        btnOut.setOnClickListener {
            sendInfoOut()
        }
    }

    private fun allPermissionsGranteGPS() = REQUIRED_PERMISSIONS_GPS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    fun clickButton() {
        if (validateComment()) {
            btnEntry.isVisible = false
            btnOut.isVisible = true

            if (allPermissionsGranteGPS()) {
                mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
                leerubicacionactual("a")
                sendInfoEntry()
            } else {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ),
                    PERMISSION_ID
                )
            }
        }
    }

    private fun sendInfoEntry() {
        try {
            val comments = txtComment.text.toString().takeIf { it.isNotEmpty() } ?: "n/a"
            Log.i("comentario: ", comments)

            if (allPermissionsGranteGPS()) {
                lbllatitud = findViewById(R.id.tvLbllatitud)
                lbllongitud = findViewById(R.id.tvLbllongitud)
                leerubicacionactual("1")
            }

            txtComment.text = null
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    fun sendInfoOut() {
        if (validateComment()) {
            val comments = txtComment.text.toString().takeIf { it.isNotEmpty() } ?: "n/a"
            
            Log.i("comentario: ", comments)

            if (allPermissionsGranteGPS()) {

                lbllatitud = findViewById(R.id.tvLbllatitud)
                lbllongitud = findViewById(R.id.tvLbllongitud)

                leerubicacionactual("0")
                mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)


                txtComment.text = null
                btnEntry.isVisible = true
                btnOut.isVisible = false
                sendRecords()
            } else {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ),
                    PERMISSION_ID
                )
            }

        }


    }

    fun sendParams(): ArrayList<Any> {
        val params = ArrayList<Any>()
        var records = dbInv?.consultSendRecord()
        try {
            var i = 0
            for (record in records!!) {

                var param = JSONObject()
                param.put("comments", record.getComments())
                param.put("date", record.getDate())
                param.put("time", record.getTime())
                param.put("latitud", record.getLatitud())
                param.put("longitud", record.getLongitud())
                param.put("employee_id", record.getEmployeeId())
                param.put("type", record.getType())

                params.add(param)
                i++
            }

        } catch (e: JSONException) {
            e.printStackTrace()
        }
        return params
    }

    private fun validateComment(): Boolean {
        if (txtComment.text.toString().isEmpty()) {
            Toast.makeText(this, "Debe escribir algún comentario", Toast.LENGTH_LONG).show()
            return false
        }
        return true
    }

    private fun sendRecords() {
        val params = sendParams()
        Log.i("UserPassword: ", params.toString())
        val data = params.toString()
        val url = getString(R.string.api_serve) + "/store-records"
        Log.i("Url: ", url)
        
        Thread {
            val http = Http(this@MainActivity, url)
            http.setMethod("POST")
            http.setData(data)
            http.send()
            
            runOnUiThread {
                val code = http.statusCode
                if (code == 201 || code == 200) {
                    dbInv?.updateSendRecord()
                    Toast.makeText(
                        this@MainActivity,
                        "Se guardó con éxito los registros",
                        Toast.LENGTH_LONG
                    ).show()
                } else if (code == 422) {
                    try {
                        val response = JSONObject(http.response)
                        val message = response.optString("message", getString(R.string.error_login))
                        Toast.makeText(this@MainActivity, message, Toast.LENGTH_LONG).show()
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                } else {
                    Toast.makeText(this@MainActivity, "Error $code", Toast.LENGTH_LONG).show()
                }
            }
        }.start()
    }

    @SuppressLint("MissingPermission")
    private fun leerubicacionactual(type: String) {
        val comments = txtComment.text.toString().takeIf { it.isNotEmpty() } ?: "n/a"
        
        if (checkPermissions()) {
            if (isLocationEnabled()) {
                try {
                    val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000L)
                        .setWaitForAccurateLocation(true)
                        .setMinUpdateIntervalMillis(2000L)
                        .setMaxUpdateDelayMillis(10000L)
                        .setMaxUpdates(1)
                        .build()

                    val locationCallback = object : LocationCallback() {
                        override fun onLocationResult(result: LocationResult) {
                            result.lastLocation?.let { location ->
                                try {
                                    if (location.accuracy <= 20f) {
                                        lbllatitud.text = location.latitude.toString()
                                        lbllongitud.text = location.longitude.toString()
                                        
                                        if (type == "0" || type == "1") {
                                            recordData.add(
                                                RecordEntity(
                                                    0,
                                                    userData[0].getEmployeeId(),
                                                    comments,
                                                    date,
                                                    timeEntry,
                                                    location.latitude,
                                                    location.longitude,
                                                    false,
                                                    type
                                                )
                                            )
                                            dbInv?.insertRecord(recordData)
                                        }
                                        
                                        Log.i(
                                            "ubicación1",
                                            "LATITUD = ${location.latitude} LONGITUD = ${location.longitude} PRECISIÓN = ${location.accuracy}m"
                                        )
                                    } else {
                                        Log.w("MainActivity", "Ubicación no es lo suficientemente precisa: ${location.accuracy}m")
                                        Toast.makeText(
                                            this@MainActivity,
                                            "Esperando mejor precisión de ubicación...",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        return
                                    }
                                } catch (e: Exception) {
                                    Log.e("MainActivity", "Error processing location", e)
                                    Toast.makeText(
                                        this@MainActivity,
                                        "Error al procesar la ubicación",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } finally {
                                    mFusedLocationClient.removeLocationUpdates(this)
                                }
                            } ?: run {
                                Log.e("MainActivity", "Location is null")
                                Toast.makeText(
                                    this@MainActivity,
                                    getString(R.string.location_not_available),
                                    Toast.LENGTH_SHORT
                                ).show()
                                mFusedLocationClient.removeLocationUpdates(this)
                            }
                        }

                        override fun onLocationAvailability(availability: LocationAvailability) {
                            if (!availability.isLocationAvailable) {
                                Log.w("MainActivity", "Location is not available")
                                Toast.makeText(
                                    this@MainActivity,
                                    getString(R.string.location_not_available),
                                    Toast.LENGTH_SHORT
                                ).show()
                                mFusedLocationClient.removeLocationUpdates(this)
                            }
                        }
                    }

                    mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
                    mFusedLocationClient.requestLocationUpdates(
                        locationRequest,
                        locationCallback,
                        Looper.getMainLooper()
                    )
                } catch (e: Exception) {
                    Log.e("MainActivity", "Error requesting location updates", e)
                    Toast.makeText(
                        this,
                        getString(R.string.location_error),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                Toast.makeText(this, getString(R.string.activate_location), Toast.LENGTH_SHORT).show()
                startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
            }
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ),
                PERMISSION_ID
            )
        }
    }

    fun Date.toString(format: String, locale: Locale = Locale.getDefault()): String {
        val formatter = SimpleDateFormat(format, locale)
        return formatter.format(this)
    }

    fun getCurrentDateTime(): Date {
        return Calendar.getInstance().time
    }

    private fun isLocationEnabled(): Boolean {
        var locationManager: LocationManager =
            getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }

    private fun checkPermissions(): Boolean {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            return true
        }
        return false
    }

    private fun checkStoragePermissions(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestStoragePermissions() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ),
            STORAGE_PERMISSION_CODE
        )
    }

    private fun initializeDatabase() {
        try {
            dbInv = DataDBHelper(applicationContext)
            userData = dbInv?.consultUser() ?: mutableListOf()
            // ... resto de la inicialización ...
        } catch (e: Exception) {
            Log.e("MainActivity", "Error initializing database", e)
            Toast.makeText(this, "Error al inicializar la base de datos", Toast.LENGTH_LONG).show()
        }
    }

    companion object {
        private val REQUIRED_PERMISSIONS_GPS = arrayOf(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
        private const val STORAGE_PERMISSION_CODE = 1001
    }

}