package pd.reactive.sensor.server.repository

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.time.Instant

@Table("sensor_data")
data class SensorData(
    val temperature: Int,
    val location: String,
    val instant: Instant,
    @Id var id: String? = null
)
