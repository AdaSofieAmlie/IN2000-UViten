package com.example.appen

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Context.LOCATION_SERVICE
import android.content.Intent
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.content.ContextCompat.startActivity


class Location(act: MainActivity){



    val main = act

    lateinit var locationManager: LocationManager
    private var hasGps = false
    private var hasNetwork = false
    private var locationGps: Location? = null
    private var locationNetwork: Location? = null

    fun enableView() {
        getLocation()
        //Toast.makeText(this, "Done", Toast.LENGTH_SHORT).show()
    }

    @SuppressLint("MissingPermission")
    private fun getLocation() {

        locationManager = main.locationMan
        hasGps = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        hasNetwork = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        if (hasGps || hasNetwork) {

            if (hasGps) {
                Log.d("CodeAndroidLocation", "hasGps")
                locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    5000,
                    0F,
                    object :
                        LocationListener {
                        override fun onLocationChanged(p0: Location) {
                            locationGps = p0
                            //tv_result.append("\nGPS ")
                            //tv_result.append("\nLatitude : " + locationGps!!.latitude)
                            //tv_result.append("\nLongitude : " + locationGps!!.longitude)
                            Log.d(
                                "CodeAndroidLocation",
                                " GPS Latitude : " + locationGps!!.latitude
                            )
                            Log.d(
                                "CodeAndroidLocation",
                                " GPS Longitude : " + locationGps!!.longitude
                            )

                        }


                        override fun onStatusChanged(
                            provider: String?,
                            status: Int,
                            extras: Bundle?
                        ) {

                        }


                    })

                val localGpsLocation =
                    locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                if (localGpsLocation != null)
                    locationGps = localGpsLocation
            }
            if (hasNetwork) {
                Log.d("CodeAndroidLocation", "hasGps")
                locationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER,
                    5000,
                    0F,
                    object :
                        LocationListener {
                        override fun onLocationChanged(location: Location) {
                            if (location != null) {
                                locationNetwork = location
                                //tv_result.append("\nNetwork ")
                                //tv_result.append("\nLatitude : " + locationNetwork!!.latitude)
                                //tv_result.append("\nLongitude : " + locationNetwork!!.longitude)
                                Log.d(
                                    "CodeAndroidLocation",
                                    " Network Latitude : " + locationNetwork!!.latitude
                                )
                                Log.d(
                                    "CodeAndroidLocation",
                                    " Network Longitude : " + locationNetwork!!.longitude
                                )
                            }
                        }

                        override fun onStatusChanged(
                            provider: String?,
                            status: Int,
                            extras: Bundle?
                        ) {

                        }


                    })

                val localNetworkLocation =
                    locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                if (localNetworkLocation != null)
                    locationNetwork = localNetworkLocation
            }

            if (locationGps != null && locationNetwork != null) {
                if (locationGps!!.accuracy > locationNetwork!!.accuracy) {
                    //tv_result.append("\nNetwork ")
                    //tv_result.append("\nLatitude : " + locationNetwork!!.latitude)
                    //tv_result.append("\nLongitude : " + locationNetwork!!.longitude)
                    Log.d(
                        "CodeAndroidLocation",
                        " Network Latitude : " + locationNetwork!!.latitude
                    )
                    Log.d(
                        "CodeAndroidLocation",
                        " Network Longitude : " + locationNetwork!!.longitude
                    )
                } else {
                    //tv_result.append("\nGPS ")
                    //tv_result.append("\nLatitude : " + locationGps!!.latitude)
                    //tv_result.append("\nLongitude : " + locationGps!!.longitude)
                    Log.d("CodeAndroidLocation", " GPS Latitude : " + locationGps!!.latitude)
                    Log.d("CodeAndroidLocation", " GPS Longitude : " + locationGps!!.longitude)
                }
            }

        } else {
            main.startAct()
        }

    }
}