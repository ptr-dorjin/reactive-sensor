package pd.sensor.reactive.server

import pd.sensor.domain.SensorData
import pd.sensor.reactive.server.repository.SensorDataEntity
import java.time.Instant
import java.time.temporal.ChronoUnit

fun Instant.noMillis(): Instant = truncatedTo(ChronoUnit.MILLIS)

fun SensorDataEntity.testVersion() = copy(id = null, instant = instant.noMillis())
fun SensorData.testVersion() = copy(id = null, instant = instant.noMillis())
