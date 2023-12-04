package com.capstone.backgroundlocaton1



import android.Manifest
import android.app.ActivityManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.capstone.backgroundlocaton1.databinding.ActivityMainBinding
import com.google.android.material.snackbar.Snackbar

class MainActivity : AppCompatActivity() {


    companion object{
        const val ACTION_START_LOCATION_SERVICE : String = "startLocationService"
        const val ACTION_STOP_LOCATION_SERVICE : String = "stopLocationService"
    }

    private val REQUEST_CODE_LOCATION_PREMISSION = 1
    private lateinit var _binding : ActivityMainBinding
    private val locationReceiver = object  : BroadcastReceiver(){
        override fun onReceive(p0: Context?, intent: Intent?) {
            if (intent!=null && intent.action=="location_update"){
                val latitude = intent.getDoubleExtra("latitude",0.0)
                val longitude = intent.getDoubleExtra("longitude",0.0)
                _binding.latitudeActivityMain.text = "${latitude}"
                _binding.longitudeActivityMain.text = "${longitude}"
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private val permissions = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.POST_NOTIFICATIONS,
        Manifest.permission.FOREGROUND_SERVICE
    )


    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(_binding.root)
        val filter = IntentFilter("location_update")
        registerReceiver(locationReceiver,filter, null, null)
        buttonClicks()
    }


    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun buttonClicks() {
        _binding.startActivityMain.setOnClickListener {
             if (checkPermissions() && isLocationEnabled(this@MainActivity)){
                 startLocationService()
             }
            else{
                 requestPermission()
             }
        }

        _binding.stopActivityMain.setOnClickListener {
            stopLocationService()
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun requestPermission() {
         ActivityCompat.requestPermissions(this@MainActivity,permissions, REQUEST_CODE_LOCATION_PREMISSION)
    }

    private fun isLocationEnabled(context: Context?): Boolean {
        val locManager = context?.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {

        if (requestCode == REQUEST_CODE_LOCATION_PREMISSION) {
            var allPermissionsGranted = true
            for (result in grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allPermissionsGranted = false
                    break
                }
            }
            if (allPermissionsGranted) {
                startLocationService()
            }
            else{
                Snackbar.make(_binding.root,"Permissions Denied",Snackbar.LENGTH_SHORT)
                    .show()
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun checkPermissions() : Boolean{
        for(permission in permissions){
            if (ContextCompat.checkSelfPermission(
                this@MainActivity,
                permission
            )!=PackageManager.PERMISSION_GRANTED
                ){
                return false
            }
        }
        return true
    }


    private fun isLocationServiceRunning() : Boolean{
        val activityManager : ActivityManager = this.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager

        activityManager.getRunningServices(Integer.MAX_VALUE).forEach { service ->
            if(LocationService::class.java.name.equals(service.service.className)){
                if(service.foreground) return true
            }
        }
        return false
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun startLocationService(){
        if (!isLocationServiceRunning() && checkPermissions()) {
            val intent = Intent(this, LocationService::class.java)
            intent.action = ACTION_START_LOCATION_SERVICE
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(intent)
            } else {
                startService(intent)
            }
            Snackbar.make(_binding.root,"Location Service Started",Snackbar.LENGTH_SHORT)
                .show()
        }
    }

    private fun stopLocationService(){
        if(isLocationServiceRunning()){
            val intent = Intent(this@MainActivity,LocationService::class.java)
            intent.action = ACTION_STOP_LOCATION_SERVICE
            startService(intent)
            Snackbar.make(_binding.root,"Location Service Stopped",Snackbar.LENGTH_SHORT)
                .show()
        }
    }



}