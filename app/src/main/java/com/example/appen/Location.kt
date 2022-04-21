package com.example.appen

import Pos
import Uv
import android.annotation.SuppressLint
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.view.View


class Location(act: MainActivity){



    val main = act


    lateinit var locationManager: LocationManager
    private var hasGps = false
    private var hasNetwork = false
    private var locationGps: Location? = null
    private var locationNetwork: Location? = null
    var position: Pos = Pos(0,0.0F,0.0F)


    fun enableView() {
        getLocation()
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

                            Log.d(
                                "CodeAndroidLocation",
                                " GPS Lat : " + locationGps!!.latitude
                            )
                            Log.d(
                                "CodeAndroidLocation",
                                " GPS Long : " + locationGps!!.longitude
                            )
                            Log.d(
                                "CodeAndroidLocation",
                                " GPS alt : " + locationGps!!.altitude
                            )

                            //val uv: Float = main.getUvByTime()
                            position = Pos(locationGps!!.altitude.toInt(),locationGps!!.latitude.toFloat(), locationGps!!.longitude.toFloat())
                            val viewMet = main.getMet()
                            viewMet.updatePositionMet(position)

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
                                Log.d(
                                    "CodeAndroidLocation",
                                    " Network Latitude : " + locationNetwork!!.latitude
                                )
                                Log.d(
                                    "CodeAndroidLocation",
                                    " Network Longitude : " + locationNetwork!!.longitude
                                )
                                position = Pos(locationNetwork!!.altitude.toInt(),locationNetwork!!.latitude.toFloat(), locationNetwork!!.longitude.toFloat())
                                val viewMet = main.getMet()
                                viewMet.updatePositionMet(position)
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
                    Log.d(
                        "CodeAndroidLocation",
                        " Network Latitude : " + locationNetwork!!.latitude
                    )
                    Log.d(
                        "CodeAndroidLocation",
                        " Network Longitude : " + locationNetwork!!.longitude
                    )
                } else {
                    Log.d("CodeAndroidLocation", " GPS Latitude : " + locationGps!!.latitude)
                    Log.d("CodeAndroidLocation", " GPS Longitude : " + locationGps!!.longitude)
                }
            }
        } else {
            main.startAct()
        }

    }

}