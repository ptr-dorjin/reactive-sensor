package pd.sensor.reactive.server.repository

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.time.Instant

@Table("sensor_data")
data class SensorDataEntity(
    val temperature: Int,
    val location: String,
    val instant: Instant,
    @Id var id: String? = null
)
