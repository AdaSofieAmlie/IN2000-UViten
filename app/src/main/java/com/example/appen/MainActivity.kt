package com.example.appen

import Pos
import android.os.Bundle
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.appen.databinding.ActivityMainBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    val dataSource = DatasourceMet()

    val baseUrl: String = "https://api.met.no/weatherapi/locationforecast/2.0/complete.json?"
    var fullUrl: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)



        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navView: BottomNavigationView = binding.navView

        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        val nyPos = Pos(0, 60.391262, 5.322054)
        setUrl(nyPos)
        CoroutineScope(Dispatchers.IO).launch {
            dataSource.getJson(fullUrl)
        }


    }

    fun setUrl(pos: Pos) {
        fullUrl = baseUrl.plus("altitude=".plus(pos.alt.toString())).plus("&lat=").plus(pos.lat.toString()).plus("&lon=").plus(pos.lon.toString())
    }
}