package pd.sensor.domain

import java.time.Instant

data class SensorData (
    val temperature: Double,
    val location: String,
    val instant: Instant,
    var id: String? = null
)