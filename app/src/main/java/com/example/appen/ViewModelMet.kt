package com.example.appen

import Pos
import Uv
import android.util.Log
import androidx.activity.viewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class ViewModelMet: ViewModel() {

    private val dataSourceMet = DatasourceMet()


    private val baseUrl: String = "https://api.met.no/weatherapi/locationforecast/2.0/complete.json?"
    var fullUrl: String = ""
    lateinit var location: Location
    var pos: Pos = Pos(0,0.0F,0.0F)

    private val uvPaaSted: MutableLiveData<Uv> by lazy {
        MutableLiveData<Uv>().also {
            loadUv()
        }
    }

    fun getUvPaaSted(): LiveData<Uv> {
        return uvPaaSted
    }

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
        Log.d("LINK", fullUrl)
    }


}