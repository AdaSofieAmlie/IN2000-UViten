data class Uv(
    val geometry: Geometry,
    val properties: Properties,
    val type: String
)

data class Units(
    val air_pressure_at_sea_level: String,
    val air_temperature: String,
    val air_temperature_max: String,
    val air_temperature_min: String,
    val cloud_area_fraction: String,
    val cloud_area_fraction_high: String,
    val cloud_area_fraction_low: String,
    val cloud_area_fraction_medium: String,
    val dew_point_temperature: String,
    val fog_area_fraction: String,
    val precipitation_amount: String,
    val precipitation_amount_max: String,
    val precipitation_amount_min: String,
    val probability_of_precipitation: String,
    val probability_of_thunder: String,
    val relative_humidity: String,
    val ultraviolet_index_clear_sky_max: String,
    val wind_from_direction: String,
    val wind_speed: String,
    val wind_speed_of_gust: String
)

data class Timesery(
    val `data`: Data,
    val time: String
)

data class Summary(
    val symbol_code: String
)

data class Properties(
    val meta: Meta,
    val timeseries: List<Timesery>
)

data class Next12Hours(
    val details: DetailsX,
    val summary: Summary
)

data class Next6Hours(
    val details: DetailsXXX,
    val summary: Summary
)

data class Next1Hours(
    val details: DetailsXX,
    val summary: Summary
)

data class Meta(
    val units: Units,
    val updated_at: String
)

data class Instant(
    val details: Details
)

data class Geometry(
    val coordinates: List<Double>,
    val type: String
)
data class DetailsXXX(
    val air_temperature_max: Double,
    val air_temperature_min: Double,
    val precipitation_amount: Double,
    val precipitation_amount_max: Double,
    val precipitation_amount_min: Double,
    val probability_of_precipitation: Double,
    val probability_of_thunder: Double,
    val ultraviolet_index_clear_sky_max: Int
)

data class DetailsXX(
    val air_temperature_max: Double,
    val air_temperature_min: Double,
    val precipitation_amount: Double,
    val precipitation_amount_max: Double,
    val precipitation_amount_min: Double,
    val probability_of_precipitation: Double,
    val probability_of_thunder: Double,
    val ultraviolet_index_clear_sky_max: Int
)

data class DetailsX(
    val air_temperature_max: Double,
    val air_temperature_min: Double,
    val precipitation_amount: Double,
    val precipitation_amount_max: Double,
    val precipitation_amount_min: Double,
    val probability_of_precipitation: Double,
    val probability_of_thunder: Double,
    val ultraviolet_index_clear_sky_max: Int
)

data class Details(
    val air_pressure_at_sea_level: Double,
    val air_temperature: Double,
    val cloud_area_fraction: Double,
    val cloud_area_fraction_high: Double,
    val cloud_area_fraction_low: Double,
    val cloud_area_fraction_medium: Double,
    val dew_point_temperature: Double,
    val fog_area_fraction: Double,
    val relative_humidity: Double,
    val wind_from_direction: Double,
    val wind_speed: Double,
    val wind_speed_of_gust: Double
)

data class Data(
    val instant: Instant,
    val next_12_hours: Next12Hours,
    val next_1_hours: Next1Hours,
    val next_6_hours: Next6Hours
)

data class Pos(
    val alt: Int,
    val lat: Double,
    val lon: Double
)