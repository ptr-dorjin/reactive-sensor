package pd.sensor.reactive.server.repository

import pd.sensor.domain.SensorData
import java.time.Instant

const val TEMPERATURE = "temperature"
const val LOCATION = "location"
const val INSTANT = "instant"

fun SensorData.toMap(): Map<String, String> {
    return mapOf(
        TEMPERATURE to temperature.toString(),
        LOCATION to location,
        INSTANT to instant.toString()
    )
}

fun Map<String, String>.fromMap(): SensorData {
    return SensorData(
        this[TEMPERATURE]!!.toDouble(),
        this[LOCATION]!!,
        Instant.parse(this[INSTANT])
    )
}