package com.example.appen.ui.home

import com.beust.klaxon.*

private val klaxon = Klaxon()
//Geocoding
data class Geocoding (
    val latitude: Double,
    val longitude: Double,
    val continent: String,
    val continentCode: String,
    val localityLanguageRequested: String,
    val city: String,
    val countryName: String,
    val postcode: String,
    val countryCode: String,
    val principalSubdivision: String,
    val principalSubdivisionCode: String,
    val plusCode: String,
    val locality: String,
    val localityInfo: LocalityInfo
) {
    companion object {
        fun fromJson(json: String) = klaxon.parse<Geocoding>(json)
    }
}

data class LocalityInfo (
    val administrative: List<Ative>,
    val informative: List<Ative>
)

data class Ative (
    val name: String,
    val description: String,
    val isoName: String? = null,
    val order: Long,
    val adminLevel: Long? = null,
    val isoCode: String? = null,

    @Json(name = "wikidataId")
    val wikidataID: String,

    @Json(name = "geonameId")
    val geonameID: Long? = null
)
