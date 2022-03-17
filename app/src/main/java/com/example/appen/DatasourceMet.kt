package com.example.appen

import Uv
//import com.github.kittinunf.fuel.Fuel

class DatasourceMet {

    suspend fun getJson(url: String): List<Uv> {
        var tilObjekt: List<Uv> = emptyList()
        try {
            //val returnString = Fuel.get(url).awaitString()
            //tilObjekt = gson.fromJson(returnString, Array<Uv>::class.java).toList()
        }
        catch (exception: Exception) {
            println("A network request exception was thrown: ${exception.message}")
        }
        return tilObjekt
    }
}