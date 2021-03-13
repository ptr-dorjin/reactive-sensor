package pd.reactive.sensor.server

import pd.reactive.sensor.server.repository.SensorData
import java.time.Instant
import java.time.temporal.ChronoUnit

fun Instant.noMillis(): Instant = truncatedTo(ChronoUnit.MILLIS)

fun SensorData.testVersion() = copy(id = null, instant = instant.noMillis())
