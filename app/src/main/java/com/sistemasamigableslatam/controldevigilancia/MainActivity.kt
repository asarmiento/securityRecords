package com.sistemasamigableslatam.controldevigilancia

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
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
        dbInv = DataDBHelper(this)

        userData = ArrayList(dbInv?.consultUser())

        tvHello.text = "Bienvenido (a): ${userData[0].getName().toString()}"
        if (allPermissionsGranteGPS()) {
            mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
            leerubicacionactual()
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
    }

    private fun allPermissionsGranteGPS() = REQUIRED_PERMISSIONS_GPS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    fun clickButton(view: View) {
        if (validateComment()) {
            btnEntry.isVisible = false
            btnOut.isVisible = true

            if (allPermissionsGranteGPS()) {
                mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
                leerubicacionactual()
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
        var comments: String = "n/a"
        if (txtComment?.text.toString() != "") {
            comments = txtComment?.text.toString()
        }
        lbllatitud = findViewById(R.id.tvLbllatitud)
        lbllongitud = findViewById(R.id.tvLbllongitud)
        leerubicacionactual()
        Log.i("comentario: ", comments)
        recordData.add(
            RecordEntity(
                0,
                userData[0].getEmployeeId(),
                comments,
                date,
                timeEntry,
                lbllatitud.text.toString().toDouble(),
                lbllongitud.text.toString().toDouble(),
                false,
                "1"
            )
        )

        dbInv?.insertRecord(recordData)
        txtComment?.text = null

    }

    fun sendInfoOut(view: View) {
        if (validateComment()) {
            var comments: String = "n/a"
            if (txtComment?.text.toString() != "") {
                comments = txtComment?.text.toString()
            }
            lbllatitud = findViewById(R.id.tvLbllatitud)
            lbllongitud = findViewById(R.id.tvLbllongitud)

            Log.i("comentario: ", comments)

            if (allPermissionsGranteGPS()) {
                mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
                leerubicacionactual()

                recordData.add(
                    RecordEntity(
                        0,
                        userData[0].getEmployeeId(),
                        comments,
                        date,
                        timeEntry,
                        lbllatitud.text.toString().toDouble(),
                        lbllongitud.text.toString().toDouble(),
                        false,
                        "0"
                    )
                )

                dbInv?.insertRecord(recordData)
                txtComment?.text = ""
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

    fun validateComment(): Boolean {

        txtComment = findViewById(R.id.txtComment)
        if (txtComment.text.toString() == "") {
            Toast.makeText(this, "Debe escribir algún comentario", Toast.LENGTH_LONG).show()
            return false
        }
        return true
    }

    fun sendRecords() {

        var params = sendParams()
        Log.i("UserPassword: ", params.toString())
        val data = params.toString()
        val url = getString(R.string.api_serve) + "/store-records"
        Log.i("Url: ", url)
        Thread {
            val http = Http(this@MainActivity, url)
            http.setMethod("POST")
            http.setData(data)
            http.send()
            // Log.i("Http: ", http.response.toString())
            runOnUiThread {
                val code = http.statusCode
                if (code == 201 || code == 200) {
                    //  val response: JSONObject =  JSONObject(http.response)
                    dbInv?.updateSendRecord()
                    Toast.makeText(
                        this@MainActivity,
                        "Se guardó con éxito los registros",
                        Toast.LENGTH_LONG
                    ).show()

                } else if (code == 422) {
                    try {
                        val response = JSONObject(http.response)
                        val msg = response.getString("message")
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                } else {
                    Toast.makeText(this@MainActivity, "Error $code", Toast.LENGTH_LONG).show()
                }
            }
        }.start()

    }

    private fun leerubicacionactual() {
        if (checkPermissions()) {
            if (isLocationEnabled()) {
                if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    mFusedLocationClient.lastLocation.addOnCompleteListener(this) { task ->
                        var location: Location? = task.result
                        if (location == null) {
                            requestNewLocationData()
                        } else {
                            lbllatitud.text = location.latitude.toString()
                            lbllongitud.text = location.longitude.toString()
                            Log.i(
                                "ubicación1: ",
                                "LATITUD = " + location.latitude.toString() + " LONGITUD = " + location.longitude.toString()
                            )
                        }
                    }
                }
            } else {
                Toast.makeText(this, "Activar ubicación", Toast.LENGTH_SHORT).show()
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
                this.finish()
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

    @SuppressLint("MissingPermission")
    private fun requestNewLocationData() {
        var mLocationRequest = LocationRequest()
        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        mLocationRequest.interval = 0
        mLocationRequest.fastestInterval = 0
        mLocationRequest.numUpdates = 1
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        mFusedLocationClient.requestLocationUpdates(
            mLocationRequest,
            mLocationCallBack,
            Looper.myLooper()!!
        )
    }

    private val mLocationCallBack = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            var mLastLocation: Location = locationResult.lastLocation
            lbllatitud.text = mLastLocation.latitude.toString()
            lbllongitud.text = mLastLocation.longitude.toString()
            Log.i(
                "ubicación2: ",
                "LATITUD = " + mLastLocation.latitude.toString() + " LONGITUD = " + mLastLocation.longitude.toString()
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

    companion object {
        private val REQUIRED_PERMISSIONS_GPS = arrayOf(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    }

}