package com.example.appen

import Pos
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
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

    private val viewModelMet: ViewModelMet by viewModels()


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

        //UV
        viewModelMet.getUvPaaSted().observe(this){
            Log.d("Fra main activity", it.toString())
            if (it != null){
                Log.d("Timeseries",
                    it.properties.timeseries[4].toString()
                )
                Log.d("UV:", it.properties.timeseries[4].data.instant.details.ultraviolet_index_clear_sky.toString())
            }
            for (i in it.properties.timeseries){
                Log.d("tag", i.time)
            }
            Log.d("Noe", it.properties.timeseries[0].time)
            Log.d("Meta", it.properties.meta.updated_at)

        }

    }


}