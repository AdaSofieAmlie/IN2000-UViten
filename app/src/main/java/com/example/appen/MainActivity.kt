package com.example.appen

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.example.appen.base.ViewModelMet
import com.example.appen.base.Location
import com.example.appen.databinding.ActivityMainBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.mapbox.android.core.location.LocationEngineProvider
import com.mapbox.search.MapboxSearchSdk

private const val PERMISSION_REQUEST = 10

class MainActivity : AppCompatActivity() {
    //Binding
    private lateinit var binding: ActivityMainBinding
    //ViewModel
    private val viewModelMet: ViewModelMet by viewModels()
    val loc = Location(this)

    private var inst: MainActivity? = null
    //Location Manager
    lateinit var locationMan: LocationManager
    //Nødvendige Permissions
    private var permissions = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)

    override fun onCreate(savedInstanceState: Bundle?) {
        //location manager -> systemets location kontekst
        locationMan = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        //onCreate for aktiviteten
        super.onCreate(savedInstanceState)

        //setter temaet (xml) for appen
        setTheme(R.style.Theme_Appen)
        inst = this

        if (checkPermission(permissions)) {
            loc.enableView()
        } else {
            requestPermissions(permissions, PERMISSION_REQUEST)
        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Navigasjonsview som ligger i bunn av alle sidene/skjærmene
        val navView: BottomNavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_activity_main)

        AppBarConfiguration(
            setOf(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications
            )
        )
        navView.setupWithNavController(navController)

        //Mapbox /Accesstoken og initsialisering
        MapboxSearchSdk.initialize(
            application = this.application,
            accessToken = getString(R.string.mapbox_access_token),
            locationEngine = LocationEngineProvider.getBestLocationEngine(this.application)
        )

    }

    //Sjekker etter nødvendige permissions
    private fun checkPermission(permissionArray: Array<String>): Boolean {
        var allSuccess = true
        for (i in permissionArray.indices) {
            if (checkCallingOrSelfPermission(permissionArray[i]) == PackageManager.PERMISSION_DENIED)
                allSuccess = false
        }
        return allSuccess
    }

    //Dobbeltsjekker permissions / godkjent/ikke godkjent
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST) {
            var allSuccess = true
            for (i in permissions.indices) {
                if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                    allSuccess = false
                    val requestAgain = shouldShowRequestPermissionRationale(permissions[i])
                    if (requestAgain) {
                        Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "Go to settings and enable the permission", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            if (allSuccess)
                loc.enableView()
        }
    }
    fun startAct() {
        return startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
    }

    //ViewModelMet
    fun getMet(): ViewModelMet {
        return viewModelMet
    }




}