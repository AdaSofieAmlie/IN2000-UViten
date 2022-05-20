package com.example.appen.base

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class ViewModelMet: ViewModel() {

    private val dataSourceMet = DatasourceMet()
    private val baseUrl: String = "https://api.met.no/weatherapi/locationforecast/2.0/complete.json?"
    private var fullUrl: String = ""
    private var pos: Pos = Pos(0,59.9F,10.7F)

    private val uvPaaSted: MutableLiveData<Uv> by lazy {
        MutableLiveData<Uv>().also {
            loadUv()
        }
    }

    fun getUvPaaSted(): LiveData<Uv> {
        return uvPaaSted
    }

    //Obs!!! Denne blir kalt fra Location.kt
    fun updatePositionMet(posInn: Pos){
        pos = posInn
        loadUv()
    }

    private fun loadUv() {
        //Lytter ettter endring i POS
        setUrl(pos)
        CoroutineScope(Dispatchers.IO).launch {
            uvPaaSted.postValue(dataSourceMet.getJson(fullUrl))
        }

    }

    private fun setUrl(pos: Pos) {
        fullUrl = baseUrl.plus("altitude=".plus(pos.alt.toString())).plus("&lat=").plus(pos.lat.toString()).plus("&lon=").plus(pos.lon.toString())
        Log.d("Test ViewModelMet setUrl() URL", fullUrl)
    }


}