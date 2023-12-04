package com.capstone.backgroundlocaton1

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority

class LocationService : Service() {

    companion object{
        const val LOCATION_SERVICE_ID : Int = 175
        const val ACTION_START_LOCATION_SERVICE : String = "startLocationService"
        const val ACTION_STOP_LOCATION_SERVICE : String = "stopLocationService"
    }

    val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            super.onLocationResult(result)
            if(result!=null && result.lastLocation!=null){
                val latitude : Double = result.lastLocation!!.latitude
                val longitude : Double = result.lastLocation!!.longitude
                Log.d("LOCATION RESULT","${latitude} & ${longitude}")
                val intent = Intent("location_update")
                intent.putExtra("latitude",latitude)
                intent.putExtra("longitude",longitude)
                sendBroadcast(intent)
            }
        }
    }

    @Override
    override fun onBind(intent: Intent?): IBinder? {
        throw  UnsupportedOperationException("NOT YET IMPLEMENTED")
    }

    private fun startLocationService(){
        try{
            val CHANNEL_ID = "location_notification_channel"

            val notificationManager : NotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            var pendingIntent : PendingIntent? = null
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S){
                pendingIntent = PendingIntent.getActivity(this,
                    0, Intent(this, MainActivity::class.java).addFlags(
                    Intent.FLAG_ACTIVITY_SINGLE_TOP),
                PendingIntent.FLAG_MUTABLE)
            }else {
                pendingIntent = PendingIntent.getActivity(this,
                    0, Intent(this, MainActivity::class.java).addFlags(
                    Intent.FLAG_ACTIVITY_SINGLE_TOP),PendingIntent.FLAG_UPDATE_CURRENT)
            }

            val builder : NotificationCompat.Builder = NotificationCompat.Builder(
                applicationContext,
                CHANNEL_ID
            )

            builder.setSmallIcon(R.mipmap.ic_launcher)
            builder.setContentTitle("Location Service")
            builder.setDefaults(NotificationCompat.DEFAULT_ALL)
            builder.setContentText("Running")
            builder.setContentIntent(pendingIntent)
            builder.setAutoCancel(false)
            builder.priority = NotificationCompat.PRIORITY_MAX

            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                if(notificationManager!=null
                    && notificationManager.getNotificationChannel(CHANNEL_ID)==null){
                    val notificationChannel : NotificationChannel = NotificationChannel(
                        CHANNEL_ID,
                        "Location Service",
                        NotificationManager.IMPORTANCE_HIGH
                    )
                    notificationChannel.description = "This channel is used by location service"
                    notificationManager.createNotificationChannel(notificationChannel)

                }
            }

            val locationRequest = LocationRequest.Builder(
                Priority.PRIORITY_HIGH_ACCURACY,
                1000
            )
                .setWaitForAccurateLocation(false)
                .setMinUpdateIntervalMillis(400)
                .setMaxUpdateDelayMillis(800)
                .build()

            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
            LocationServices.getFusedLocationProviderClient(this)
                .requestLocationUpdates(locationRequest,locationCallback, Looper.getMainLooper())

            startForeground(LOCATION_SERVICE_ID, builder.build())
        }
        catch (e : Exception){
            e.printStackTrace()
        }
    }


     private fun stopLocationService(){
         LocationServices.getFusedLocationProviderClient(this)
             .removeLocationUpdates(locationCallback);
         stopForeground(true);
         stopSelf()
     }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if(intent!=null){
            val action : String? = intent.action
            if(action!=null){
                if(action.equals(ACTION_START_LOCATION_SERVICE)){
                    startLocationService()
                }
                else if(action.equals(ACTION_STOP_LOCATION_SERVICE)){
                    stopLocationService()
                }
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }


}
