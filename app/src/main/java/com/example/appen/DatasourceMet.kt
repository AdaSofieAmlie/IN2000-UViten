package com.example.appen

import Uv
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

    private val gson = Gson()

    suspend fun getJson(url: String): Uv? {
        //var tilObjekt: Uv


        try {
            val client = HttpClient() {
                install(JsonFeature)
                install(UserAgent) {
                    agent = "uio.no snorre@wenaas.org"
                }
            }

            val returnObjekt: Uv = client.get(url)
            //val returnString = Fuel.get(url).awaitString()
            //tilObjekt = gson.fromJson(returnString, Uv::class.java)
            //Log.d("hei", tilObjekt.toString())
            Log.d("heiString", returnObjekt.toString())
            return returnObjekt
        }
        catch (exception: Exception) {
            println("A network request exception was thrown: ${exception.message}")
        }
        return null
    }

}