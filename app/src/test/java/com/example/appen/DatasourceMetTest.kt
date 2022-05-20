package com.example.appen

import com.example.appen.base.DatasourceMet
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import com.example.appen.base.Uv
import kotlinx.coroutines.runBlocking

internal class DatasourceMetTest {
    @Test
    fun getJsonTest() {
        val dataSource = DatasourceMet()
        var uvObjekt: Uv?

        runBlocking {
            uvObjekt = dataSource.getJson("https://api.met.no/weatherapi/locationforecast/2.0/complete.json?altitude=0&lat=59.9406&lon=10.7231")!!
            assertEquals(uvObjekt!!.properties.timeseries[0].data.instant.details.ultraviolet_index_clear_sky.toFloat(), 4.5F)
        }
    }
}