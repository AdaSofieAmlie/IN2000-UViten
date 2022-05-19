package com.example.appen

import Uv
import android.net.http.HttpResponseCache.install
import android.util.Log
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.coroutines.awaitString
import com.google.gson.Gson
import io.ktor.client.*
import io.ktor.client.features.*
import io.ktor.client.features.get
import io.ktor.client.features.json.*
import io.ktor.client.request.*

class DatasourceMet {

    suspend fun getJson(url: String): Uv? {

        try {
            val client = HttpClient() {
                install(JsonFeature)
                install(UserAgent) {
                    agent = "uio.no snorre@wenaas.org"
                }
            }
            val returnObjekt: Uv = client.get(url)
            return returnObjekt
        }
        catch (exception: Exception) {
            println({exception.message})
        }
        return null
    }

}