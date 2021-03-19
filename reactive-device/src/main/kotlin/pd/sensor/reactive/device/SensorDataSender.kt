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
import pd.sensor.reactive.device.prop.ServerProperties
import java.net.URI
import java.time.Instant

@Component
class SensorDataSender(
    private val rsocketBuilder: RSocketRequester.Builder,
    private val serverProperties: ServerProperties
) {

    private val log = LoggerFactory.getLogger(SensorDataSender::class.java)

    @Scheduled(fixedDelayString = "\${sensor.device.interval:5000}")
    fun send() {
        runBlocking {
            val rSocketRequester = rsocketBuilder.websocket(URI(serverProperties.url))

            rSocketRequester.route("api.v1.sensors.stream")
                .dataWithType(flow {
                    emit(SensorData(31, "balcony", Instant.now()))
                })
                .retrieveFlow<Void>()
                .catch { /*log.error("Couldn't send data to the server", it)*/ }
                .collect()
        }
    }
}