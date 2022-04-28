// To parse the JSON, install Klaxon and do:
//
//   val welcome6 = Welcome6.fromJson(jsonString)

package codebeautify

import com.beust.klaxon.*

private val klaxon = Klaxon()

data class Geocoding (
    val type: String,
    val licence: String,
    val features: List<Feature>
) {
    public fun toJson() = klaxon.toJsonString(this)

    companion object {
        public fun fromJson(json: String) = klaxon.parse<Geocoding>(json)
    }
}

data class Feature (
    val type: String,
    val properties: Properties,
    val bbox: List<Double>,
    val geometry: Geometry
)

data class Geometry (
    val type: String,
    val coordinates: List<Double>
)

data class Properties (
    @Json(name = "place_id")
    val placeID: Long,

    @Json(name = "osm_type")
    val osmType: String,

    @Json(name = "osm_id")
    val osmID: Long,

    @Json(name = "place_rank")
    val placeRank: Long,

    val category: String,
    val type: String,
    val importance: Long,
    val addresstype: String,
    val name: Any? = null,

    @Json(name = "display_name")
    val displayName: String,

    val address: Address
)

data class Address (
    @Json(name = "house_number")
    val houseNumber: String,

    val road: String,
    val suburb: String,
    val city: String,
    val county: String,
    val state: String,

    @Json(name = "ISO3166-2-lvl4")
    val iso31662Lvl4: String,

    val postcode: String,
    val country: String,

    @Json(name = "country_code")
    val countryCode: String
)
