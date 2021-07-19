package pd.sensor.reactive.device

import kotlinx.coroutines.flow.*
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.messaging.rsocket.RSocketRequester
import org.springframework.messaging.rsocket.dataWithType
import org.springframework.messaging.rsocket.retrieveFlow
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import pd.sensor.domain.SensorData
import pd.sensor.reactive.device.prop.DeviceProperties
import pd.sensor.reactive.device.prop.ServerProperties
import java.math.BigDecimal
import java.math.RoundingMode
import java.net.URI
import java.time.Instant
import kotlin.random.Random

@Component
class SensorDataSender(
    private val rsocketBuilder: RSocketRequester.Builder,
    serverProperties: ServerProperties,
    private val deviceProperties: DeviceProperties
) {
    private val log = LoggerFactory.getLogger(SensorDataSender::class.java)

    private val serverUrl = "ws://${serverProperties.host}:${serverProperties.port}/rsocket"
    private var location = generateLocation()
    private var randomTempBase: Double = Random.nextDouble(50.0)

    private fun generateLocation(): String {
        var location = deviceProperties.location
        if (location.isBlank()) {
            // generate random name, if not specified
            location = "device-" + Random.nextInt(1000)
        }
        log.info("Connecting to $serverUrl from $location")
        return location
    }

    @Scheduled(fixedDelayString = "\${sensor.device.interval:5000}")
    fun send() {
        runBlocking {
            val rSocketRequester = rsocketBuilder.websocket(URI(serverUrl))

            rSocketRequester.route("api.v2.sensors.stream")
                .dataWithType(flow {
                    emit(SensorData(randomTemperature(), location, Instant.now()))
                })
                .retrieveFlow<Void>()
                .catch { log.debug("Couldn't send data to the server", it) }
                .collect()
        }
    }

    private fun randomTemperature(): Double {
        val temp = randomTempBase + System.currentTimeMillis() % (2 * Math.PI)
        return BigDecimal(temp).setScale(4, RoundingMode.HALF_UP).toDouble()
    }
}