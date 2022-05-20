package com.example.appen.base

import android.annotation.SuppressLint
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import com.example.appen.MainActivity

class Location(act: MainActivity){
    //referanse til main activity
    val main = act

    private lateinit var locationManager: LocationManager
    private var hasGps = false
    private var hasNetwork = false
    private var locationGps: Location? = null
    private var locationNetwork: Location? = null
    var position: Pos = Pos(0,59.911491F,10.757933F)

    //Kaller bare på getLocation
    fun enableView() {
        getLocation()
    }

    //LocationManager
    //Oppdaterer posisjonsobjektet basert på bevegelse
    //Fungerer med både GPS og Netverks lokasjon
    //Oppdateres maks hvert 5 sekund
    @SuppressLint("MissingPermission")
    private fun getLocation() {

        locationManager = main.locationMan
        hasGps = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        hasNetwork = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        if (hasGps || hasNetwork) {
            //Gjelder for GPS
            if (hasGps) {
                Log.d("Test Location getLocation() GPS", "HasGps & uses it")
                locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    5000,
                    0F,
                    object :
                        LocationListener {
                        override fun onLocationChanged(p0: Location) {
                            locationGps = p0

                            Log.d(
                                "Test Location getLocation() GPS",
                                " GPS Lat : " + locationGps!!.latitude
                            )
                            Log.d(
                                "Test Location getLocation() GPS",
                                " GPS Long : " + locationGps!!.longitude
                            )
                            Log.d(
                                "Test Location getLocation() GPS",
                                " GPS alt : " + locationGps!!.altitude
                            )
                            position = Pos(locationGps!!.altitude.toInt(),locationGps!!.latitude.toFloat(), locationGps!!.longitude.toFloat())
                            val viewMet = main.getMet()
                            viewMet.updatePositionMet(position)
                        }
                        override fun onStatusChanged(
                            provider: String?,
                            status: Int,
                            extras: Bundle?
                        ) {}
                    })

                val localGpsLocation =
                    locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                if (localGpsLocation != null)
                    locationGps = localGpsLocation
            }
            //Gjelder for netverkslokasjon
            if (hasNetwork) {
                Log.d("Test Location getLocation() GPSNetwork", "hasNetwork & uses it")
                locationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER,
                    5000,
                    0F,
                    object :
                        LocationListener {
                        override fun onLocationChanged(location: Location) {
                            locationNetwork = location
                            Log.d(
                                "Test Location getLocation() Network",
                                " Network Latitude : " + locationNetwork!!.latitude
                            )
                            Log.d(
                                "Test Location getLocation() Network",
                                " Network Longitude : " + locationNetwork!!.longitude
                            )
                            position = Pos(locationNetwork!!.altitude.toInt(),locationNetwork!!.latitude.toFloat(), locationNetwork!!.longitude.toFloat())
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

                val localNetworkLocation =
                    locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                if (localNetworkLocation != null)
                    locationNetwork = localNetworkLocation
            }

            if (locationGps != null && locationNetwork != null) {
                if (locationGps!!.accuracy > locationNetwork!!.accuracy) {
                    Log.d(
                        "Test Location getLocation() Network",
                        " Network Latitude : " + locationNetwork!!.latitude
                    )
                    Log.d(
                        "Test Location getLocation() Network",
                        " Network Longitude : " + locationNetwork!!.longitude
                    )
                } else {
                    Log.d("Test Location getLocation() GPS", " GPS Latitude : " + locationGps!!.latitude)
                    Log.d("Test Location getLocation() GPS", " GPS Longitude : " + locationGps!!.longitude)
                }
            }
        } else {
            main.startAct()
        }

    }

}