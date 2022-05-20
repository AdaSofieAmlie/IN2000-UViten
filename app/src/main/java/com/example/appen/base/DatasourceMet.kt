package com.example.appen.base

import io.ktor.client.*
import io.ktor.client.features.*
import io.ktor.client.features.json.*
import io.ktor.client.request.*

class DatasourceMet {

    suspend fun getJson(url: String): Uv? {

        try {
            val client = HttpClient {
                install(JsonFeature)
                install(UserAgent) {
                    agent = "uio.no snorre@wenaas.org"
                }
            }
            return client.get<Uv>(url)
        } catch (exception: Exception) {
            println({ exception.message })
        }
        return null
    }

}