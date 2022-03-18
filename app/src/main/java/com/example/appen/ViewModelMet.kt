package com.example.appen

import Pos
import Uv
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

    private val uvPaaSted: MutableLiveData<Uv> by lazy {
        MutableLiveData<Uv>().also {
            loadUv()
        }
    }

    fun getUvPaaSted(): LiveData<Uv> {
        return uvPaaSted
    }

    private fun loadUv() {
        // Do an asynchronous operation to fetch users.
        val nyPos = Pos(0, 60.391262, 5.322054)
        setUrl(nyPos)
        CoroutineScope(Dispatchers.IO).launch {
            uvPaaSted.postValue(dataSourceMet.getJson(fullUrl))
        }
    }

    private fun setUrl(pos: Pos) {
        fullUrl = baseUrl.plus("altitude=".plus(pos.alt.toString())).plus("&lat=").plus(pos.lat.toString()).plus("&lon=").plus(pos.lon.toString())
        Log.d("LINK", fullUrl)
    }
}