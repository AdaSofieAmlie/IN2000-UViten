package com.example.appen

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import Uv
import kotlinx.coroutines.runBlocking

internal class DatasourceMetTest {
    @Test
    fun getJsonTest() {
        val dataSource = DatasourceMet()
        var uvObjekt: Uv? = null

        runBlocking {
            uvObjekt = dataSource.getJson("https://api.met.no/weatherapi/locationforecast/2.0/complete.json?altitude=0&lat=59.9406&lon=10.7231")!!
            assertEquals(uvObjekt!!.properties.timeseries[0].data.instant.details.ultraviolet_index_clear_sky.toFloat(), 4.5F)
        }
    }
}